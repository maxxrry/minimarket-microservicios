package com.minimarket.msclientes.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para recibir datos de creación/actualización de Cliente.
 * Aplica todas las validaciones de entrada antes de llegar al service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDTO {

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(
            regexp = "^\\d{1,2}\\.\\d{3}\\.\\d{3}-[0-9kK]$",
            message = "El RUT debe tener formato XX.XXX.XXX-X (ej: 12.345.678-9)"
    )
    @Size(max = 12, message = "El RUT no puede tener más de 12 caracteres")
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100, message = "El email no puede tener más de 100 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    private String telefono;

    @Size(max = 200, message = "La dirección no puede tener más de 200 caracteres")
    private String direccion;

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    private Boolean activo;
}