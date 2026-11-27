# SaludCerca - Backend

API REST de SaludCerca (telemedicina) hecha con Spring Boot 4, Java 21 y PostgreSQL.

## Correr en local

Requisitos: Java 21 o superior y PostgreSQL con usuario `postgres` y contraseña `postgre`.
Si tu PostgreSQL usa otra contraseña, cámbiala en el archivo `.env`.

1. Abre esta carpeta en IntelliJ.
2. En **Run > Edit Configurations > ProyectArquiBackendApplication > Modify options >
   Environment variables** agrega `RESEND_API_KEY=<key del equipo>` (pídesela al equipo).
3. Dale **Run**. La primera vez crea sola la base `bd_proyect_arqui_backend` y carga
   `scripts/datos_prueba.sql`. En el log tiene que salir "Se creo la base local" y
   "Datos de prueba cargados".

La API queda en `http://localhost:8080` y la documentación en `http://localhost:8080/swagger-ui.html`.

Si ya tenías la base de antes y quieres los datos nuevos, bórrala en pgAdmin y vuelve a
darle Run: se crea de nuevo con los datos de prueba.

### Accesos de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | admin@saludcerca.pe | Admin123 |
| ADMIN | javier.27.29.24@gmail.com, carlos.collao.quiroz@gmail.com, fabianaqr09@gmail.com | Admin123 |
| MÉDICO | cmendoza@saludcerca.test (y los demás médicos) | 123456 |
| PACIENTE | jperez@saludcerca.test (y los demás pacientes) | 123456 |
| PACIENTE | danielorellanagutierrez@gmail.com | Admin123 |

## Envío de correos (Resend)

- La API key se lee de la variable `RESEND_API_KEY`. No se sube al repositorio: GitHub
  bloquea el push si encuentra la key.
- La cuenta de Resend está en modo prueba: solo envía a `danielorellanagutierrez@gmail.com`,
  el dueño de la cuenta. Enviar a otro correo falla.
- Para probarlo: **Notificaciones > Enviar**, elige la cita **#69 · Daniel Orellana Gutiérrez**
  y dale **Enviar correo**.

## Configuración local y Render

- El archivo `.env` apunta a PostgreSQL local y activa `APP_PREPARAR_BD_LOCAL`, que crea la
  base y carga los datos de prueba al arrancar.
- Render construye con el `Dockerfile`, que solo copia `pom.xml` y `src`: el `.env` no entra
  en la imagen, así que en Render se usan la base de Render y los valores de
  `application.properties`. Allí `RESEND_API_KEY` se define en **Environment**.
- Para usar la base de Render desde tu PC, borra o renombra el `.env`. Desde la red de la
  universidad no funciona, porque el puerto 5432 está bloqueado.
