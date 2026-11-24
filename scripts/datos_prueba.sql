-- =====================================================================
-- Datos de prueba para SaludCerca (BD_Proyect_Arqui_Backend)
--
-- Ejecutar desde la raiz del backend con el backend ya iniciado al menos una
-- vez (crea las tablas y los roles ADMIN=1, MEDICO=2, PACIENTE=3):
--   psql -U postgres -h localhost -d BD_Proyect_Arqui_Backend -v ON_ERROR_STOP=1 -f scripts/datos_prueba.sql
-- No se ejecuta solo al arrancar: Spring Boot solo carga data.sql/schema.sql.
--
-- Solo se carga sobre una base sin datos (aparte de los roles). Todo va en
-- una transaccion: si algo falla no queda nada a medias.
--
-- Accesos (Guia_prueba_seguridad_SaludCerca):
--   ADMIN    admin@saludcerca.pe      / Admin123
--   MEDICO   cmendoza@saludcerca.test / 123456   (y los demas medicos)
--   PACIENTE jperez@saludcerca.test   / 123456   (y los demas pacientes)
-- El admin tiene los mismos datos que crea DataInitializer con
-- APP_ADMIN_CORREO/APP_ADMIN_PASSWORD, asi que arrancar el backend con esas
-- variables no lo duplica (DataInitializer lo omite si el correo ya existe).
-- =====================================================================
SET client_encoding = 'UTF8';

BEGIN;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios) OR EXISTS (SELECT 1 FROM especialidades)
       OR EXISTS (SELECT 1 FROM citas) OR EXISTS (SELECT 1 FROM examenes)
       OR EXISTS (SELECT 1 FROM diagnosticos) OR EXISTS (SELECT 1 FROM tratamientos)
       OR EXISTS (SELECT 1 FROM recetas) OR EXISTS (SELECT 1 FROM historiales_clinicos)
       OR EXISTS (SELECT 1 FROM detalles_historial) OR EXISTS (SELECT 1 FROM notificaciones) THEN
        RAISE EXCEPTION 'La base ya tiene datos: este script solo se carga sobre una base vacia';
    END IF;
    IF (SELECT count(*) FROM roles WHERE (id_rol, nombre) IN ((1, 'ADMIN'), (2, 'MEDICO'), (3, 'PACIENTE'))) <> 3 THEN
        RAISE EXCEPTION 'Faltan los roles ADMIN=1, MEDICO=2, PACIENTE=3: inicia el backend una vez antes de cargar los datos';
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- Especialidades (Traumatologia queda sin medicos a proposito)
-- ---------------------------------------------------------------------
INSERT INTO especialidades (id_especialidad, nombre) VALUES
    (1, 'Medicina General'),
    (2, 'Pediatría'),
    (3, 'Cardiología'),
    (4, 'Dermatología'),
    (5, 'Ginecología'),
    (6, 'Psicología'),
    (7, 'Traumatología');

-- ---------------------------------------------------------------------
-- Usuarios. Hashes BCrypt (BCryptPasswordEncoder de Spring):
--   admin -> "Admin123"; todos los demas -> "123456"
-- ---------------------------------------------------------------------
INSERT INTO usuarios (id_usuario, rol_id, especialidad_id, nombres, apellidos, dni, telefono, correo,
                      contrasena_hash, colegiatura, region_ubicacion, estado) VALUES
    -- ADMIN (mismos datos que crea DataInitializer)
    (1, 1, NULL, 'Administrador', 'Sistema', '00000000', '000000000', 'admin@saludcerca.pe',
        '$2a$10$tnGts.pyX0bLP8XeD5p9hOu2XUr6ExN7oIooGo1i4Bu3G2jOEABzq', NULL, NULL, true),
    -- MEDICOS (Miguel Castillo no tiene especialidad: sale como "Sin especialidad" en los reportes)
    (2, 2, 1, 'Carlos', 'Mendoza Ríos', '41258763', '987654321', 'cmendoza@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', 'CMP-045812', 'Lima', true),
    (3, 2, 2, 'Lucía', 'Fernández Paredes', '42587196', '986532147', 'lfernandez@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', 'CMP-052377', 'Arequipa', true),
    (4, 2, 3, 'Jorge', 'Ramírez Soto', '40871235', '985214763', 'jramirez@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', 'CMP-038904', 'Lima', true),
    (5, 2, 4, 'María', 'Torres Huamán', '45123698', '984123657', 'mtorres@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', 'CMP-061245', 'Cusco', true),
    (6, 2, 5, 'Rosa', 'Quispe Mamani', '43698521', '983654712', 'rquispe@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', 'CMP-057730', 'Puno', true),
    (7, 2, 6, 'Diego', 'Salazar Vega', '46325874', '982147536', 'dsalazar@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', 'CPsP-24519', 'La Libertad', true),
    (8, 2, NULL, 'Miguel', 'Castillo León', '44785236', '981236547', 'mcastillo@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', 'CMP-070112', 'Piura', true),
    -- PACIENTES (Elena Silva esta desactivada: no puede iniciar sesion)
    (9, 3, NULL, 'Juan', 'Pérez García', '72145896', '956321478', 'jperez@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Lima', true),
    (10, 3, NULL, 'Sofía', 'Gutiérrez Rojas', '73258964', '957412369', 'sgutierrez@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Callao', true),
    (11, 3, NULL, 'Luis', 'Chávez Flores', '70369852', '958523614', 'lchavez@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Arequipa', true),
    (12, 3, NULL, 'Valeria', 'Díaz Morales', '74521036', '959632145', 'vdiaz@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Cusco', true),
    (13, 3, NULL, 'Pedro', 'Vargas Núñez', '71458023', '951478236', 'pvargas@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Lima', true),
    (14, 3, NULL, 'Camila', 'Rojas Medina', '75896321', '952369874', 'crojas@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Piura', true),
    (15, 3, NULL, 'Mateo', 'Herrera Campos', '81236547', '953147852', 'mherrera@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Junín', true),
    (16, 3, NULL, 'Elena', 'Silva Cárdenas', '76321458', '954785214', 'esilva@saludcerca.test',
        '$2a$10$2BicHckmY6v9yYeHjYjazex.fc6xzqY2XrdbGmZUS/Lc7CzReMNVO', NULL, 'Ica', false);

-- ---------------------------------------------------------------------
-- Historias clinicas (Mateo Herrera, id 15, queda sin historia a proposito)
-- ---------------------------------------------------------------------
INSERT INTO historiales_clinicos (id_historia_clinica, paciente_id, fecha_creacion) VALUES
    (1, 9,  '2026-03-12 00:00:00'),
    (2, 10, '2026-04-02 00:00:00'),
    (3, 11, '2026-05-20 00:00:00'),
    (4, 12, '2026-06-08 00:00:00'),
    (5, 13, '2026-07-15 00:00:00'),
    (6, 14, '2026-08-03 00:00:00'),
    (7, 16, '2026-08-21 00:00:00');

-- ---------------------------------------------------------------------
-- Diagnosticos (CIE-10)
-- ---------------------------------------------------------------------
INSERT INTO diagnosticos (id_diagnostico, codigo_cie10, descripcion, fecha_registro) VALUES
    (1, 'J06.9', 'Infección aguda de las vías respiratorias superiores, no especificada', '2026-09-11 00:00:00'),
    (2, 'I10',   'Hipertensión esencial (primaria)', '2026-09-17 00:00:00'),
    (3, 'E11.9', 'Diabetes mellitus tipo 2 sin complicaciones', '2026-10-03 00:00:00'),
    (4, 'L20.9', 'Dermatitis atópica, no especificada', '2026-10-14 00:00:00'),
    (5, 'K29.7', 'Gastritis, no especificada', '2026-11-12 00:00:00'),
    (6, 'F41.1', 'Trastorno de ansiedad generalizada', '2026-11-03 00:00:00'),
    (7, 'J45.9', 'Asma, no especificada', '2026-11-20 00:00:00'),
    (8, 'N39.0', 'Infección de vías urinarias, sitio no especificado', '2026-11-06 00:00:00');

-- ---------------------------------------------------------------------
-- Examenes (fecha_resultado NULL = pendiente, sale en el reporte de pendientes)
-- ---------------------------------------------------------------------
INSERT INTO examenes (id_examen, tipo_examen, resultado, fecha_solicitud, fecha_resultado) VALUES
    (1,  'Hemograma completo', 'Leucocitos 11 500/mm³, ligera leucocitosis. Hemoglobina 14.2 g/dL.', '2026-09-10 00:00:00', '2026-09-11 00:00:00'),
    (2,  'Perfil lipídico', 'Colesterol total 245 mg/dL, LDL 160 mg/dL, HDL 38 mg/dL, triglicéridos 210 mg/dL.', '2026-09-15 00:00:00', '2026-09-17 00:00:00'),
    (3,  'Glucosa en ayunas', 'Glucosa 132 mg/dL (valor elevado).', '2026-10-02 00:00:00', '2026-10-03 00:00:00'),
    (4,  'Hemoglobina glicosilada (HbA1c)', 'HbA1c 7.4 %.', '2026-10-03 00:00:00', '2026-10-06 00:00:00'),
    (5,  'Electrocardiograma', 'Ritmo sinusal, FC 78 lpm, sin alteraciones agudas del ST.', '2026-10-20 00:00:00', '2026-10-20 00:00:00'),
    (6,  'Examen de orina completo', 'Leucocituria (25-30 x campo), nitritos positivos.', '2026-11-05 00:00:00', '2026-11-06 00:00:00'),
    (7,  'Urocultivo', 'Urocultivo de control tras antibioticoterapia.', '2026-11-13 00:00:00', NULL),
    (8,  'Ecocardiograma', 'Solicitado para evaluar soplo sistólico.', '2026-11-18 00:00:00', NULL),
    (9,  'Espirometría', 'Solicitada para confirmar diagnóstico de asma.', '2026-11-20 00:00:00', NULL),
    (10, 'Radiografía de tórax', 'Solicitada para descartar neumonía.', '2026-11-22 00:00:00', NULL);

-- ---------------------------------------------------------------------
-- Tratamientos (estados del formulario: Activo, Pendiente, Finalizado)
-- ---------------------------------------------------------------------
INSERT INTO tratamientos (id_tratamiento, descripcion, fecha_inicio, fecha_fin, estado) VALUES
    (1, 'Paracetamol, hidratación y reposo por cuadro respiratorio agudo', '2026-09-11', '2026-09-16', 'Finalizado'),
    (2, 'Control de presión arterial con dieta hiposódica, actividad física y Losartán', '2026-09-17', '2027-03-17', 'Activo'),
    (3, 'Metformina y plan nutricional para control glucémico', '2026-10-06', '2027-04-06', 'Activo'),
    (4, 'Hidratación cutánea diaria y corticoide tópico', '2026-10-14', '2026-11-14', 'Finalizado'),
    (5, 'Terapia cognitivo-conductual: 8 sesiones semanales', '2026-11-03', '2026-12-29', 'Activo'),
    (6, 'Antibioticoterapia oral para infección urinaria', '2026-11-06', '2026-11-13', 'Finalizado'),
    (7, 'Rehabilitación respiratoria y control con inhalador', '2026-12-01', '2027-01-31', 'Pendiente');

-- ---------------------------------------------------------------------
-- Recetas
-- ---------------------------------------------------------------------
INSERT INTO recetas (id_receta, medicamento, dosis, frecuencia, duracion_dias, indicaciones, fecha_emision) VALUES
    (1, 'Paracetamol 500 mg', '1 tableta', 'Cada 8 horas', 5, 'Tomar después de los alimentos. No exceder 4 g al día.', '2026-09-11 00:00:00'),
    (2, 'Losartán 50 mg', '1 tableta', 'Cada 24 horas', 90, 'Tomar por la mañana. Controlar la presión arterial una vez por semana.', '2026-09-17 00:00:00'),
    (3, 'Metformina 850 mg', '1 tableta', 'Cada 12 horas', 90, 'Tomar con el desayuno y la cena.', '2026-10-06 00:00:00'),
    (4, 'Hidrocortisona crema 1 %', 'Capa fina', 'Cada 12 horas', 14, 'Aplicar solo sobre las zonas afectadas. No usar en el rostro.', '2026-10-14 00:00:00'),
    (5, 'Nitrofurantoína 100 mg', '1 cápsula', 'Cada 12 horas', 7, 'Completar el tratamiento aunque desaparezcan los síntomas.', '2026-11-06 00:00:00'),
    (6, 'Omeprazol 20 mg', '1 cápsula', 'Cada 24 horas', 30, 'Tomar en ayunas, 30 minutos antes del desayuno.', '2026-11-12 00:00:00'),
    (7, 'Salbutamol inhalador 100 mcg', '2 inhalaciones', 'Cada 6 horas si hay silbidos', 30, 'Usar con aerocámara. Acudir a emergencia si no mejora.', '2026-11-20 00:00:00');

-- ---------------------------------------------------------------------
-- Detalles de historial (une historia, medico, examen, diagnostico, tratamiento y receta)
-- ---------------------------------------------------------------------
INSERT INTO detalles_historial (id_detalle_historial, historial_id, medico_id, examen_id, diagnostico_id,
                                tratamiento_id, receta_id, motivo_consulta, cifrado_datos, fecha_registro) VALUES
    (1,  1, 2, 1,    1,    1,    1,    'Fiebre, dolor de garganta y congestión nasal', 'T° 38.4 °C. Faringe congestiva.', '2026-09-11 00:00:00'),
    (2,  3, 4, 2,    2,    2,    2,    'Cefalea y presión arterial elevada en control', 'PA 150/95 mmHg. Antecedente familiar de HTA.', '2026-09-17 00:00:00'),
    (3,  5, 2, 3,    3,    NULL, NULL, 'Sed y cansancio frecuentes', NULL, '2026-10-03 00:00:00'),
    (4,  5, 2, 4,    3,    3,    3,    'Revisión de resultados de HbA1c', 'IMC 29.1. Se deriva a nutrición.', '2026-10-06 00:00:00'),
    (5,  4, 5, NULL, 4,    4,    4,    'Lesiones eccematosas con picazón en ambos brazos', NULL, '2026-10-14 00:00:00'),
    (6,  3, 4, 5,    NULL, NULL, NULL, 'Control cardiológico con electrocardiograma', 'PA 135/85 mmHg con tratamiento.', '2026-10-20 00:00:00'),
    (7,  2, 6, 6,    8,    6,    5,    'Ardor al orinar y dolor pélvico', NULL, '2026-11-06 00:00:00'),
    (8,  2, 6, 7,    NULL, NULL, NULL, 'Seguimiento de infección urinaria', 'Asintomática al control.', '2026-11-13 00:00:00'),
    (9,  6, 7, NULL, 6,    5,    NULL, 'Ansiedad, insomnio y dificultad para concentrarse', 'Escala GAD-7: 14 puntos.', '2026-11-03 00:00:00'),
    (10, 7, 8, NULL, 5,    NULL, 6,    'Dolor epigástrico y acidez después de las comidas', NULL, '2026-11-12 00:00:00'),
    (11, 3, 4, 8,    NULL, NULL, NULL, 'Soplo sistólico detectado en control', NULL, '2026-11-18 00:00:00'),
    (12, 1, 2, 9,    7,    7,    7,    'Tos persistente y silbidos al respirar', 'SatO2 96 %. Sibilancias espiratorias.', '2026-11-20 00:00:00'),
    (13, 1, 2, 10,   NULL, NULL, NULL, 'Control de tos persistente', NULL, '2026-11-22 00:00:00');

-- ---------------------------------------------------------------------
-- Citas: atendidas (pasadas, con tiempo de espera), canceladas y pendientes
-- (dos de ellas hoy, 24/11/2026, para probar la videollamada)
-- ---------------------------------------------------------------------
INSERT INTO citas (id_cita, paciente_id, medico_id, fecha_hora_programada, tiempo_espera_minutos, estado, sala_videollamada)
SELECT id, paciente, medico, fecha::timestamp, espera, estado,
       'SaludCerca-' || replace(gen_random_uuid()::text, '-', '')
FROM (VALUES
    (1,  9,  2, '2026-09-11 09:00', 15, 'ATENDIDA'),
    (2,  11, 4, '2026-09-17 10:30', 25, 'ATENDIDA'),
    (3,  13, 2, '2026-10-03 08:30', 10, 'ATENDIDA'),
    (4,  13, 2, '2026-10-06 11:00', 20, 'ATENDIDA'),
    (5,  12, 5, '2026-10-14 15:00', 35, 'ATENDIDA'),
    (6,  11, 4, '2026-10-20 09:30', 40, 'ATENDIDA'),
    (7,  14, 7, '2026-11-03 17:00', 5,  'ATENDIDA'),
    (8,  10, 6, '2026-11-06 16:00', 12, 'ATENDIDA'),
    (9,  16, 8, '2026-11-12 10:00', 30, 'ATENDIDA'),
    (10, 10, 6, '2026-11-13 16:30', 8,  'ATENDIDA'),
    (11, 11, 4, '2026-11-18 10:00', 22, 'ATENDIDA'),
    (12, 9,  2, '2026-11-20 09:00', 18, 'ATENDIDA'),
    (13, 15, 3, '2026-10-25 10:00', 0,  'CANCELADA'),
    (14, 12, 5, '2026-11-10 11:00', 0,  'CANCELADA'),
    (15, 14, 2, '2026-11-15 08:00', 0,  'CANCELADA'),
    (16, 9,  2, '2026-11-24 15:00', 0,  'PENDIENTE'),
    (17, 15, 3, '2026-11-24 17:30', 0,  'PENDIENTE'),
    (18, 10, 6, '2026-11-26 09:00', 0,  'PENDIENTE'),
    (19, 11, 4, '2026-11-28 10:00', 0,  'PENDIENTE'),
    (20, 14, 7, '2026-12-01 17:00', 0,  'PENDIENTE'),
    (21, 12, 5, '2026-12-03 11:30', 0,  'PENDIENTE'),
    (22, 13, 2, '2026-12-05 08:30', 0,  'PENDIENTE'),
    (23, 13, 4, '2026-12-09 09:00', 0,  'PENDIENTE')
) AS v(id, paciente, medico, fecha, espera, estado);

-- ---------------------------------------------------------------------
-- Notificaciones (historial; no se envia ningun correo al cargarlas)
-- ---------------------------------------------------------------------
INSERT INTO notificaciones (id_notificacion, usuario_id, tipo, mensaje, fecha_envio, estado_envio) VALUES
    (1, 13, 'RESULTADO_EXAMEN', 'Ya está disponible el resultado de tu examen de hemoglobina glicosilada (HbA1c).', '2026-10-06 12:10:00', 'FALLIDO'),
    (2, 14, 'AVISO_GENERAL', 'SaludCerca realizará un mantenimiento programado el domingo de 02:00 a 04:00.', '2026-11-15 18:00:00', 'ENVIADO'),
    (3, 10, 'RESULTADO_EXAMEN', 'Ya está disponible el resultado de tu examen de orina completo.', '2026-11-06 10:45:00', 'ENVIADO'),
    (4, 15, 'CITA_REGISTRADA', 'Tu cita con Lucía Fernández Paredes quedo registrada para el 24/11/2026.' || chr(10) || 'Ese dia puedes ingresar a la consulta por videollamada desde el siguiente boton.', '2026-11-20 09:15:00', 'FALLIDO'),
    (5, 9, 'CITA_REGISTRADA', 'Tu cita con Carlos Mendoza Ríos quedo registrada para el 24/11/2026.' || chr(10) || 'Ese dia puedes ingresar a la consulta por videollamada desde el siguiente boton.', '2026-11-21 11:30:00', 'ENVIADO'),
    (6, 11, 'RECORDATORIO', 'Recuerda tu cita de Cardiología con Jorge Ramírez Soto el 28/11/2026 a las 10:00.', '2026-11-23 08:00:00', 'ENVIADO'),
    (7, 12, 'RECORDATORIO', 'Recuerda tu cita de Dermatología con María Torres Huamán el 03/12/2026 a las 11:30.', '2026-11-23 08:05:00', 'ENVIADO'),
    (8, 2, 'AVISO_GENERAL', 'Se actualizó el módulo de historias clínicas: ahora puedes adjuntar recetas a cada detalle.', '2026-11-22 19:00:00', 'ENVIADO');

-- ---------------------------------------------------------------------
-- Las secuencias de identidad siguen despues del ultimo id insertado
-- ---------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('especialidades', 'id_especialidad'), (SELECT max(id_especialidad) FROM especialidades));
SELECT setval(pg_get_serial_sequence('usuarios', 'id_usuario'), (SELECT max(id_usuario) FROM usuarios));
SELECT setval(pg_get_serial_sequence('historiales_clinicos', 'id_historia_clinica'), (SELECT max(id_historia_clinica) FROM historiales_clinicos));
SELECT setval(pg_get_serial_sequence('diagnosticos', 'id_diagnostico'), (SELECT max(id_diagnostico) FROM diagnosticos));
SELECT setval(pg_get_serial_sequence('examenes', 'id_examen'), (SELECT max(id_examen) FROM examenes));
SELECT setval(pg_get_serial_sequence('tratamientos', 'id_tratamiento'), (SELECT max(id_tratamiento) FROM tratamientos));
SELECT setval(pg_get_serial_sequence('recetas', 'id_receta'), (SELECT max(id_receta) FROM recetas));
SELECT setval(pg_get_serial_sequence('detalles_historial', 'id_detalle_historial'), (SELECT max(id_detalle_historial) FROM detalles_historial));
SELECT setval(pg_get_serial_sequence('citas', 'id_cita'), (SELECT max(id_cita) FROM citas));
SELECT setval(pg_get_serial_sequence('notificaciones', 'id_notificacion'), (SELECT max(id_notificacion) FROM notificaciones));

COMMIT;
