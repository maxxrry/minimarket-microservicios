package com.minimarket.mscatalogo.exception;

import com.minimarket.mscatalogo.dto.ErrorResponseDTO;
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
 * Manejador GLOBAL de excepciones para todos los controladores REST.
 * Centraliza el tratamiento de errores y devuelve respuestas HTTP
 * coherentes y estructuradas, en lugar de stacktraces crudos de Java.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody:
 *   - @ControllerAdvice: aplica a TODOS los @Controller del microservicio.
 *   - @ResponseBody: serializa los retornos a JSON automáticamente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja recursos no encontrados → HTTP 404 NOT FOUND.
     * Ej: cuando se busca un producto por ID que no existe.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        log.warn("Recurso no encontrado: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .mensaje(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja códigos de barra duplicados → HTTP 409 CONFLICT.
     */
    @ExceptionHandler(CodigoBarraDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarCodigoBarraDuplicado(
            CodigoBarraDuplicadoException ex,
            HttpServletRequest request) {

        log.warn("Código de barra duplicado: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .mensaje(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja errores de validación de Bean Validation (@Valid en DTOs).
     * → HTTP 400 BAD REQUEST con la lista de campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extraer todos los errores de validación campo por campo
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatearErrorCampo)
                .collect(Collectors.toList());

        log.warn("Errores de validación: {}", errores);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .mensaje("Los datos enviados no son válidos")
                .path(request.getRequestURI())
                .errores(errores)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Maneja argumentos ilegales (IllegalArgumentException).
     * → HTTP 400 BAD REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> manejarArgumentoIlegal(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Argumento ilegal: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .mensaje(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Maneja CUALQUIER otra excepción no controlada explícitamente.
     * → HTTP 500 INTERNAL SERVER ERROR.
     * Es la red de seguridad final.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        log.error("Error interno del servidor: ", ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .mensaje("Ha ocurrido un error inesperado. Contacte al administrador.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Formatea un FieldError como un string legible.
     * Ej: "precio: El precio debe ser mayor a cero"
     */
    private String formatearErrorCampo(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}