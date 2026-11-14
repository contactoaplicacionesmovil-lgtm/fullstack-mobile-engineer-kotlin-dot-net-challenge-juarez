#!/bin/bash
# Ejecuta, en orden alfabetico, todos los scripts .sql de esta carpeta.
# Lo invoca el servicio db-init de docker-compose.yml.
set -euo pipefail

SQLCMD=/opt/mssql-tools18/bin/sqlcmd

for script in /db/*.sql; do
    echo ">> Ejecutando $(basename "$script")"
    "$SQLCMD" -S "${DB_HOST:-sqlserver}" -U sa -P "$MSSQL_SA_PASSWORD" -C -b -f 65001 -i "$script"
done

echo ">> Base de datos lista"
