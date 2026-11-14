using System.Data;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;

namespace FieldOps.Api.Controllers
{
    [ApiController]
    [Route("api/workorders")]
    public class WorkOrdersController : ControllerBase
    {
        private readonly IConfiguration _config;

        public WorkOrdersController(IConfiguration config)
        {
            _config = config;
        }

        // GET api/workorders?technicianId=1&status=PENDING
        [HttpGet]
        public IActionResult GetOrders(string technicianId, string status)
        {
            if (!IsLogged())
            {
                return Unauthorized();
            }

            try
            {
                var cn = new SqlConnection(_config.GetConnectionString("FieldOps"));
                cn.Open();

                var sql = "SELECT * FROM WorkOrders WHERE TechnicianId = " + technicianId;

                // 2020-03: filtro por estado para la pantalla nueva de la app
                if (!string.IsNullOrEmpty(status))
                {
                    sql += " AND Status = '" + status + "'";
                }

                sql += " ORDER BY ScheduledAt";

                var cmd = new SqlCommand(sql, cn);
                var dt = new DataTable();
                dt.Load(cmd.ExecuteReader());

                return Ok(dt);
            }
            catch (Exception ex)
            {
                return Ok(new { ok = false, message = ex.ToString() });
            }
        }

        // PUT api/workorders/5/status
        [HttpPut("{id}/status")]
        public IActionResult UpdateStatus(string id, [FromBody] UpdateStatusRequest request)
        {
            if (!IsLogged())
            {
                return Unauthorized();
            }

            try
            {
                var cn = new SqlConnection(_config.GetConnectionString("FieldOps"));
                cn.Open();

                var sql = "UPDATE WorkOrders SET Status = '" + request.Status + "', UpdatedAt = GETDATE() WHERE Id = " + id;

                var cmd = new SqlCommand(sql, cn);
                cmd.ExecuteNonQuery();

                return Ok(new { ok = true });
            }
            catch (Exception ex)
            {
                return Ok(new { ok = false, message = ex.ToString() });
            }
        }

        private bool IsLogged()
        {
            var header = Request.Headers["Authorization"].ToString();
            return !string.IsNullOrEmpty(header) && header.StartsWith("Bearer ");
        }
    }

    public class UpdateStatusRequest
    {
        public string Status { get; set; }
    }
}
