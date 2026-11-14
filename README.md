# FieldOps: prueba técnica Android + .NET

Gracias por tu tiempo. Esta prueba se parece al trabajo real del puesto: no vas a construir algo desde cero, vas a recibir código heredado que funciona y lo vas a mejorar sin romperlo.

| | |
|---|---|
| Esfuerzo esperado | 3 a 4 horas de trabajo efectivo |
| Plazo de entrega | 48 horas desde que recibes este repositorio |
| Siguiente etapa | Defensa presencial de tu entrega (45 a 60 minutos) |

No esperamos que todo quede perfecto. Si el tiempo no te alcanza, prioriza y explica en `DECISIONES.md` qué dejaste fuera y por qué. Un alcance menor bien resuelto vale más que todo a medias.

## Contexto

FieldOps es una empresa de servicios técnicos. Sus técnicos usan una app Android para ver las órdenes de trabajo que tienen asignadas y cambiarles el estado. La app consume una API en ASP.NET Core que guarda los datos en SQL Server.

Las dos piezas se escribieron hace años. Funcionan, pero el equipo ya no puede mantenerlas con confianza. Te sumas al equipo que las va a modernizar.

## Qué hay en el repositorio

```
docker-compose.yml        SQL Server 2022, inicialización de la BD y la API
backend/
  FieldOps.sln
  src/FieldOps.Api/       API en ASP.NET Core (.NET 10)
  db/                     scripts SQL, se ejecutan en orden alfabético
  requests.http           llamadas de ejemplo
android/                  app Android (Java), se abre con Android Studio
DECISIONES.md             plantilla que debes completar
```

## Cómo levantar el entorno

Necesitas:

- Docker con Compose. El contenedor de SQL Server usa unos 2 GB de RAM. En Mac con Apple Silicon activa la emulación con Rosetta en Docker Desktop, porque la imagen de SQL Server solo existe para x86-64.
- SDK de .NET 10.
- Android Studio en su versión estable más reciente, con un emulador. El proyecto usa Android Gradle Plugin 9.5.1, Gradle 9.6, JDK 17 y `compileSdk 36`.

### Backend

```bash
docker compose up --build
```

Esto levanta SQL Server en `localhost:1433`, crea la base `FieldOps` con datos de prueba y publica la API en `http://localhost:5080`.

Si prefieres correr la API desde tu IDE:

```bash
docker compose up -d sqlserver db-init
cd backend/src/FieldOps.Api
dotnet run
```

Otros comandos útiles:

| Para | Comando |
|---|---|
| Volver a ejecutar los scripts de `backend/db/` | `docker compose run --rm db-init` |
| Borrar la base y empezar de cero | `docker compose down -v` |

Credenciales de SQL Server para desarrollo: usuario `sa`, clave `FieldOps_Dev_2024`.

### Android

Abre la carpeta `android/` en Android Studio y ejecuta la app en un emulador. La app apunta a `http://10.0.2.2:5080`, que desde el emulador es el `localhost` de tu máquina. Si usas un dispositivo físico, cambia la URL por la IP de tu equipo.

Usuarios de prueba:

| Usuario | Clave |
|---|---|
| `jperez` | `123456` |
| `mquispe` | `123456` |
| `lramos` | `fieldops2024` |

El build ya está preparado para Kotlin (viene integrado en AGP 9, no hay que aplicar ningún plugin) y ya incluye `ViewModel`, corrutinas y dependencias de test. Puedes empezar a escribir Kotlin sin tocar Gradle.

## Tu trabajo

Son cuatro tareas obligatorias, dos por lado, más un documento breve. Los tiempos son una guía.

### B1. Acceso a datos con Stored Procedures (50 min)

Se cumple cuando:

- Login, listado de órdenes y cambio de estado se resuelven con Stored Procedures invocados con ADO.NET: `SqlCommand` con `CommandType.StoredProcedure` y `SqlParameter` con tipo y tamaño explícitos.
- No queda SQL armado concatenando texto en el código C#.
- El acceso a datos vive en su propia capa, fuera de los controllers.
- Conexiones, comandos y lectores se liberan correctamente.
- Los scripts de los procedimientos están en `backend/db/` (por ejemplo `03_procedures.sql`), de modo que `docker compose down -v && docker compose up --build` deja todo funcionando sin pasos manuales.

No uses un ORM ni un micro ORM (Entity Framework, Dapper). Queremos ver ADO.NET.

### B2. Autenticación y autorización con JWT (50 min)

Se cumple cuando:

- Los endpoints de órdenes solo responden si reciben un access token válido, con firma y expiración verificadas.
- El access token tiene vida corta y configurable. Para la demo puedes usar 1 o 2 minutos.
- Existe `POST /api/auth/refresh`, que entrega un access token nuevo a partir de un refresh token que el servidor guarda y puede invalidar.
- El técnico se obtiene del token y no de un dato que envía el cliente. Un técnico solo ve y modifica sus propias órdenes.
- La clave de firma no está en el código fuente.
- Los errores de autenticación y autorización responden con el código HTTP que corresponde.

### A1. Migrar la pantalla de órdenes a Kotlin + MVVM (75 min)

Se cumple cuando:

- `OrdersActivity` está migrada a Kotlin con MVVM: un `ViewModel` expone el estado de la pantalla (cargando, con datos, vacía, con error) y la vista solo lo dibuja.
- La red y el parseo salen de la Activity. Se usan corrutinas en lugar de `AsyncTask`.
- Girar el dispositivo no pierde el estado, no repite la carga y no provoca cierres inesperados.
- El usuario distingue entre "no hay órdenes" y "ocurrió un error".
- Hay al menos 3 tests unitarios del `ViewModel` que corren en la JVM, sin emulador.
- Se conserva lo que la pantalla hace hoy: listado, filtro por estado, cambio de estado, marca de orden vencida, resaltado de prioridad alta y cierre de sesión.

Puedes usar vistas XML o Compose. No pedimos rediseño visual. El login no hace falta migrarlo completo: cambia lo que necesites para A2.

### A2. Sesión y tokens en el cliente (40 min)

Se cumple cuando:

- Los tokens se guardan en un almacenamiento protegido por Android Keystore. La clave del usuario no se guarda.
- El header `Authorization` se agrega en un único punto del código.
- Si la API responde 401 porque el access token venció, la app lo renueva una vez y repite la petición sin que el usuario lo note.
- Si la renovación falla, la app limpia la sesión y vuelve al login.
- No se escriben tokens en Logcat.

### DECISIONES.md (15 min)

Completa la plantilla. Lo que más nos interesa es la sección de hallazgos: el código tiene más problemas de los que cubren estas cuatro tareas. No tienes que corregirlos todos, pero sí queremos saber cuáles viste y en qué orden los atacarías.

## Opcionales

Solo si te sobra tiempo. Suman, pero no compensan una tarea obligatoria floja. En la defensa conversaremos sobre varios de estos temas aunque no los implementes.

- Guardar las claves con hash en lugar de texto plano.
- Validar las transiciones de estado dentro del Stored Procedure, con transacción e historial de cambios.
- Paginación en el listado.
- Tests de la API.
- Manejo global de errores en la API.
- Rotación del refresh token con detección de reuso.
- Migrar también el login a Kotlin + MVVM.
- Inyección de dependencias en Android.
- Caché local para ver las órdenes sin conexión.
- Un Worker Service de .NET, instalable como Servicio de Windows, que marque las órdenes vencidas.
- Pipeline de integración continua.

## Reglas

- Puedes cambiar el contrato de la API (rutas, formato de las respuestas). Documenta los cambios; la app y la API deben seguir funcionando juntas.
- En Android puedes usar las librerías que quieras (Retrofit, OkHttp, Hilt, Moshi, etc.). Cuéntanos por qué las elegiste.
- Puedes usar asistentes de IA. Decláralo en `DECISIONES.md`. En la defensa vas a explicar tu código y a modificarlo en vivo, así que entrega solo lo que entiendas.
- Haz commits pequeños con mensajes claros. Nos interesa ver cómo avanzaste, no un único commit final.
- No publiques este enunciado ni tu solución en repositorios públicos.
- Si algo no está claro, pregunta respondiendo al correo con el que recibiste la prueba. Preguntar no resta puntos. Si no llegas a preguntar, toma una decisión razonable y anótala.

## Entrega

Envía el enlace a un repositorio privado con acceso para quien te envió la prueba, o un archivo zip que incluya la carpeta `.git`.

Antes de enviar, comprueba que:

1. `docker compose down -v && docker compose up --build` deja la API funcionando.
2. La app compila y los tests pasan con `./gradlew testDebugUnitTest` dentro de `android/`.
3. `DECISIONES.md` está completo.

## Cómo evaluamos

| Criterio | Peso |
|---|---|
| B1. Acceso a datos con Stored Procedures y ADO.NET | 20 |
| B2. JWT: validación, expiración, renovación, autorización | 20 |
| A1. Migración a Kotlin + MVVM, corrutinas y tests | 25 |
| A2. Sesión y tokens en el cliente | 15 |
| Calidad general: código limpio, historial de commits, el proyecto corre siguiendo las instrucciones | 10 |
| Comunicación: `DECISIONES.md`, hallazgos y prioridades | 10 |
| Opcionales | hasta 10 extra |

## Defensa presencial

Dura entre 45 y 60 minutos:

1. Nos muestras la app y la API funcionando.
2. Recorremos tu código y nos explicas tus decisiones.
3. Implementas en vivo un cambio pequeño sobre tu propia entrega.
4. Conversamos sobre otros temas del puesto que esta prueba no cubre.

## Referencia: API actual

Así se comporta la API que recibes. `backend/requests.http` tiene estas mismas llamadas listas para ejecutar.

### `POST /api/auth/login`

```json
{ "username": "jperez", "password": "123456" }
```

Respuesta (se muestran los campos que usa la app):

```json
{
  "ok": true,
  "token": "eyJhbGciOi...",
  "user": [ { "id": 1, "username": "jperez", "fullName": "Juan Pérez Salas", "role": "TECHNICIAN" } ]
}
```

Si las credenciales no coinciden, responde `200` con `{ "ok": false, "message": "..." }`.

### `GET /api/workorders?technicianId=1&status=PENDING`

Requiere el header `Authorization: Bearer <token>`. El parámetro `status` es opcional y acepta `PENDING`, `IN_PROGRESS` o `COMPLETED`.

```json
[
  {
    "id": 4,
    "code": "OT-0004",
    "title": "Mantenimiento preventivo de UPS",
    "description": "Revisión semestral según contrato.",
    "customerName": "Notaría Paredes",
    "address": "Calle Las Begonias 441, San Isidro",
    "priority": "MEDIUM",
    "status": "PENDING",
    "technicianId": 1,
    "scheduledAt": "2026-11-14T10:00:00",
    "updatedAt": null
  }
]
```

### `PUT /api/workorders/{id}/status`

Requiere el header `Authorization: Bearer <token>`.

```json
{ "status": "IN_PROGRESS" }
```

Respuesta: `{ "ok": true }`.
