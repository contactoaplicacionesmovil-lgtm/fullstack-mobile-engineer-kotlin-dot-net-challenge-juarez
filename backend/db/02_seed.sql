-- FieldOps - datos de prueba
-- Solo inserta si las tablas estan vacias, para poder re-ejecutar sin duplicar.

USE FieldOps;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Users)
BEGIN
    INSERT INTO dbo.Users (Username, Password, FullName, Role, IsActive) VALUES
        ('jperez',  '123456',       N'Juan Pérez Salas',     'TECHNICIAN', 1),
        ('mquispe', '123456',       N'María Quispe Huamán',  'TECHNICIAN', 1),
        ('lramos',  'fieldops2024', N'Luis Ramos Castillo',  'TECHNICIAN', 1),
        ('ctorres', 'ctorres01',    N'Carlos Torres Vega',   'TECHNICIAN', 0),
        ('admin',   'admin',        N'Supervisor General',   'SUPERVISOR', 1);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.WorkOrders)
BEGIN
    DECLARE @today DATETIME = CAST(CAST(GETDATE() AS DATE) AS DATETIME);

    INSERT INTO dbo.WorkOrders
        (Code, Title, Description, CustomerName, Address, Priority, Status, TechnicianId, ScheduledAt, UpdatedAt)
    VALUES
        -- jperez (Id 1)
        ('OT-0001', N'Instalación de router empresarial',  N'Instalar y configurar router en sala de servidores.',        N'Estudio Contable Lazo SAC',   N'Av. Javier Prado Este 492, San Isidro',     'HIGH',   'PENDING',     1, DATEADD(HOUR,   9, DATEADD(DAY, -2, @today)), NULL),
        ('OT-0002', N'Revisión de cableado estructurado',  N'El cliente reporta cortes intermitentes en el piso 3.',      N'Clínica Santa Beatriz',       N'Av. Arequipa 1148, Lince',                  'MEDIUM', 'IN_PROGRESS', 1, DATEADD(HOUR,  11, DATEADD(DAY, -1, @today)), DATEADD(HOUR, 12, DATEADD(DAY, -1, @today))),
        ('OT-0003', N'Cambio de equipo ONT',               N'Reemplazo por equipo dañado tras corte de energía.',         N'Bodega Don Lucho',            N'Jr. Huiracocha 1530, Jesús María',          'LOW',    'COMPLETED',   1, DATEADD(HOUR,  15, DATEADD(DAY, -3, @today)), DATEADD(HOUR, 16, DATEADD(DAY, -3, @today))),
        ('OT-0004', N'Mantenimiento preventivo de UPS',    N'Revisión semestral según contrato.',                         N'Notaría Paredes',             N'Calle Las Begonias 441, San Isidro',        'MEDIUM', 'PENDING',     1, DATEADD(HOUR,  10, @today),                   NULL),
        ('OT-0005', N'Instalación de cámaras IP',          N'4 cámaras en ingreso y almacén.',                            N'Ferretería El Tornillo',      N'Av. Angamos Este 1805, Surquillo',          'HIGH',   'PENDING',     1, DATEADD(HOUR,  14, @today),                   NULL),
        ('OT-0006', N'Configuración de central telefónica',N'Alta de 12 anexos nuevos.',                                  N'Inmobiliaria Costa Verde',    N'Av. Larco 880, Miraflores',                 'LOW',    'PENDING',     1, DATEADD(HOUR,   9, DATEADD(DAY,  1, @today)), NULL),
        ('OT-0007', N'Soporte por caída de enlace',        N'Enlace dedicado sin servicio desde la madrugada.',           N'Laboratorios Andina',         N'Av. República de Panamá 3591, San Isidro',  'HIGH',   'IN_PROGRESS', 1, DATEADD(HOUR,   8, @today),                   DATEADD(HOUR,  8, @today)),
        -- mquispe (Id 2)
        ('OT-0008', N'Instalación de access points',       N'6 AP en oficinas administrativas.',                          N'Colegio San Agustín',         N'Av. Javier Prado Este 980, San Isidro',     'MEDIUM', 'PENDING',     2, DATEADD(HOUR,  10, @today),                   NULL),
        ('OT-0009', N'Reubicación de rack',                N'Mover rack de comunicaciones al nuevo data room.',           N'Agencia de Aduanas Pacífico', N'Av. Sáenz Peña 284, Callao',                'HIGH',   'PENDING',     2, DATEADD(HOUR,  16, DATEADD(DAY, -1, @today)), NULL),
        ('OT-0010', N'Cambio de fuente de poder',          N'Switch principal con fuente defectuosa.',                    N'Restaurante La Rosa Náutica', N'Espigón 4, Circuito de Playas, Miraflores', 'MEDIUM', 'COMPLETED',   2, DATEADD(HOUR,  12, DATEADD(DAY, -2, @today)), DATEADD(HOUR, 13, DATEADD(DAY, -2, @today))),
        ('OT-0011', N'Auditoría de red inalámbrica',       N'Medición de cobertura y reporte.',                           N'Hotel Los Delfines',          N'Calle Los Eucaliptos 555, San Isidro',      'LOW',    'IN_PROGRESS', 2, DATEADD(HOUR,   9, @today),                   DATEADD(HOUR,  9, @today)),
        ('OT-0012', N'Instalación de fibra óptica',        N'Tendido interno de 80 m.',                                   N'Textiles del Sur',            N'Av. Aviación 2405, San Borja',              'HIGH',   'PENDING',     2, DATEADD(HOUR,  11, DATEADD(DAY,  2, @today)), NULL),
        -- lramos (Id 3)
        ('OT-0013', N'Revisión de planta eléctrica',       N'Prueba de arranque y transferencia.',                        N'Centro Médico Monterrico',    N'Av. Primavera 1250, Santiago de Surco',     'MEDIUM', 'PENDING',     3, DATEADD(HOUR,  10, DATEADD(DAY,  1, @today)), NULL),
        ('OT-0014', N'Reemplazo de switch de acceso',      N'Cambio por modelo PoE.',                                     N'Universidad del Pacífico',    N'Jr. Sánchez Cerro 2141, Jesús María',       'LOW',    'COMPLETED',   3, DATEADD(HOUR,  14, DATEADD(DAY, -4, @today)), DATEADD(HOUR, 15, DATEADD(DAY, -4, @today))),
        ('OT-0015', N'Atención de garantía',               N'Equipo instalado la semana pasada no enciende.',             N'Panadería San Antonio',       N'Av. Vasco Núñez de Balboa 770, Miraflores', 'HIGH',   'PENDING',     3, DATEADD(HOUR,  13, DATEADD(DAY, -1, @today)), NULL);
END
GO
