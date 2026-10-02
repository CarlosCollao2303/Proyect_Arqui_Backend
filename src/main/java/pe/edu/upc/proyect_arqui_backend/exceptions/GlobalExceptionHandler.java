package pe.edu.upc.proyect_arqui_backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.upc.proyect_arqui_backend.dtos.ErrorResponse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";
    private static final String NOT_NULL_VIOLATION_SQL_STATE = "23502";
    private static final Pattern UNIQUE_FIELD_PATTERN = Pattern.compile("Key \\(([^)]+)\\)=");
    private static final Pattern NOT_NULL_FIELD_PATTERN =
            Pattern.compile("null value in column \"([^\"]+)\"");

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "El parametro '" + ex.getName() + "' tiene un formato invalido" +
                        (ex.getRequiredType() == LocalDate.class ? " (use yyyy-MM-dd)" : ""),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                mensajeIntegridad(ex),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    private String mensajeIntegridad(DataIntegrityViolationException ex) {
        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (!(causa instanceof SQLException sqlException)) {
                continue;
            }

            String mensaje = sqlException.getMessage();
            if (UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                String campo = extraerCampo(mensaje, UNIQUE_FIELD_PATTERN);
                if (campo != null) {
                    return "El valor del campo " + nombreCampo(campo) + " ya esta registrado.";
                }
            } else if (NOT_NULL_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                String campo = extraerCampo(mensaje, NOT_NULL_FIELD_PATTERN);
                if (campo != null) {
                    return "El campo " + nombreCampo(campo) + " es obligatorio.";
                }
            }
        }

        return "La operacion viola una restriccion de la base de datos. Verifique los campos obligatorios, " +
                "los valores duplicados y las relaciones con otros registros.";
    }

    private String extraerCampo(String mensaje, Pattern patron) {
        if (mensaje == null) {
            return null;
        }

        Matcher matcher = patron.matcher(mensaje);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String nombreCampo(String campo) {
        return switch (campo) {
            case "dni" -> "DNI";
            case "correo" -> "correo";
            case "telefono" -> "telefono";
            case "especialidad_id" -> "especialidad";
            case "rol_id" -> "rol";
            default -> "'" + campo.replace('_', ' ') + "'";
        };
    }
}
