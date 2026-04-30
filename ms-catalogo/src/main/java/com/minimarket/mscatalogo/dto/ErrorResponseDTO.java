package com.minimarket.mscatalogo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO estándar para respuestas de error.
 * Todas las excepciones del microservicio se convierten en este formato,
 * garantizando una API uniforme y predecible para los clientes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // Omite campos null en el JSON
public class ErrorResponseDTO {

    /**
     * Fecha y hora en que ocurrió el error.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Código HTTP del error (404, 409, 400, etc.).
     */
    private int status;

    /**
     * Nombre descriptivo del error (NOT_FOUND, CONFLICT, BAD_REQUEST).
     */
    private String error;

    /**
     * Mensaje descriptivo del error para el cliente.
     */
    private String mensaje;

    /**
     * Ruta del endpoint donde ocurrió el error.
     */
    private String path;

    /**
     * Lista de errores específicos por campo (solo en errores de validación).
     * Si no hay errores de campo, este atributo se omite del JSON gracias a
     * @JsonInclude(NON_NULL).
     */
    private List<String> errores;
}