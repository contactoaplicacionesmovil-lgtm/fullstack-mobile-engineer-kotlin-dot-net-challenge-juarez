using System.Data;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using Microsoft.IdentityModel.Tokens;

namespace FieldOps.Api.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private const string SecretKey = "F13ld0ps-2019-clave-para-firmar-tokens-app-movil";

        private readonly IConfiguration _config;

        public AuthController(IConfiguration config)
        {
            _config = config;
        }

        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginRequest request)
        {
            try
            {
                var cn = new SqlConnection(_config.GetConnectionString("FieldOps"));
                cn.Open();

                var sql = "SELECT * FROM Users WHERE Username = '" + request.Username + "' AND Password = '" + request.Password + "'";

                var da = new SqlDataAdapter(sql, cn);
                var dt = new DataTable();
                da.Fill(dt);

                if (dt.Rows.Count == 0)
                {
                    return Ok(new { ok = false, message = "Usuario o clave incorrectos" });
                }

                var row = dt.Rows[0];

                var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(SecretKey));
                var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

                var claims = new[]
                {
                    new Claim("userId", row["Id"].ToString()),
                    new Claim("username", row["Username"].ToString()),
                    new Claim("role", row["Role"].ToString())
                };

                // la app movil no debe pedir login a cada rato (pedido de operaciones)
                var jwt = new JwtSecurityToken(claims: claims, signingCredentials: credentials);
                var token = new JwtSecurityTokenHandler().WriteToken(jwt);

                return Ok(new { ok = true, token = token, user = dt });
            }
            catch (Exception ex)
            {
                return Ok(new { ok = false, message = ex.ToString() });
            }
        }
    }

    public class LoginRequest
    {
        public string Username { get; set; }
        public string Password { get; set; }
    }
}
