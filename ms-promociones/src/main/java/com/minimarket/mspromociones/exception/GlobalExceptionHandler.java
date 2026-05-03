package com.minimarket.mspromociones.exception;

import com.minimarket.mspromociones.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador GLOBAL de excepciones del microservicio ms-promociones.
 * Centraliza el tratamiento de errores y devuelve respuestas HTTP
 * coherentes y estructuradas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(PromocionInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> manejarPromocionInvalida(
            PromocionInvalidaException ex, HttpServletRequest request) {
        log.warn("Promoción inválida: {}", ex.getMessage());
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> manejarArgumentoIlegal(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    /**
     * Captura los errores de validación de Bean Validation (@Valid en DTOs).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresDeValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Errores de validación en la petición");
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatearErrorCampo)
                .collect(Collectors.toList());

        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "Error de validación en los datos enviados", request, errores);
    }

    /**
     * Red de seguridad: captura cualquier excepción no manejada explícitamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Error interno del servidor: ", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error inesperado. Contacte al administrador.", request, null);
    }

    /**
     * Método helper para construir la respuesta de error de forma consistente.
     */
    private ResponseEntity<ErrorResponseDTO> construirRespuesta(
            HttpStatus status, String mensaje,
            HttpServletRequest request, List<String> errores) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .mensaje(mensaje)
                .path(request.getRequestURI())
                .errores(errores)
                .build();
        return new ResponseEntity<>(error, status);
    }

    /**
     * Formatea un FieldError como "campo: mensaje".
     */
    private String formatearErrorCampo(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
