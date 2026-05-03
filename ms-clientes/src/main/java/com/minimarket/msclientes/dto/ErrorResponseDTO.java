package com.minimarket.msclientes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO estandarizado para devolver errores al cliente HTTP.
 * Lo usa el GlobalExceptionHandler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String mensaje;
    private String path;
    private Map<String, String> erroresValidacion;
}