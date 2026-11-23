using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using FieldOps.Api.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;

namespace FieldOps.Api.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly AuthDataAccess _authDataAccess;
        private readonly SigningCredentials _signingCredentials;
        private readonly string _issuer;
        private readonly string _audience;
        private readonly int _accessTokenMinutes;

        public AuthController(
            AuthDataAccess authDataAccess,
            SigningCredentials signingCredentials,
            IConfiguration configuration)
        {
            _authDataAccess = authDataAccess;
            _signingCredentials = signingCredentials;
            _issuer = configuration["Jwt:Issuer"]
                ?? throw new InvalidOperationException("Falta Jwt:Issuer.");
            _audience = configuration["Jwt:Audience"]
                ?? throw new InvalidOperationException("Falta Jwt:Audience.");
            _accessTokenMinutes =
                configuration.GetValue<int>("Jwt:AccessTokenMinutes");
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login(
            [FromBody] LoginRequest request,
            CancellationToken cancellationToken)
        {
            var user = await _authDataAccess.LoginAsync(
                request.Username,
                request.Password,
                cancellationToken);

            if (user is null)
            {
                return Unauthorized(new
                {
                    ok = false,
                    message = "Usuario o clave incorrectos"
                });
            }

            var now = DateTime.UtcNow;
            var claims = new[]
            {
                new Claim("userId", user.Id.ToString()),
                new Claim("username", user.Username),
                new Claim("role", user.Role)
            };

            var jwt = new JwtSecurityToken(
                issuer: _issuer,
                audience: _audience,
                claims: claims,
                notBefore: now,
                expires: now.AddMinutes(_accessTokenMinutes),
                signingCredentials: _signingCredentials);

            var token = new JwtSecurityTokenHandler().WriteToken(jwt);

            return Ok(new
            {
                ok = true,
                token,
                user = new[]
                {
                    new
                    {
                        id = user.Id,
                        username = user.Username,
                        fullName = user.FullName,
                        role = user.Role
                    }
                }
            });
        }
    }

    public class LoginRequest
    {
        public string Username { get; set; }
        public string Password { get; set; }
    }
}