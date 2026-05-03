package com.minimarket.msclientes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para devolver datos de Cliente al cliente HTTP.
 * Incluye campos de auditoría (fechas) que no están en el request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    private Long id;
    private String rut;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}