package com.minimarket.msempleados.dto;

import com.minimarket.msempleados.model.Cargo;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para crear o actualizar un Empleado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoRequestDTO {

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

    @NotNull(message = "El cargo es obligatorio")
    private Cargo cargo;

    @NotNull(message = "El sueldo es obligatorio")
    @DecimalMin(value = "500000.00", message = "El sueldo no puede ser menor al sueldo mínimo legal")
    @Digits(integer = 8, fraction = 2, message = "El sueldo debe tener máximo 8 enteros y 2 decimales")
    private BigDecimal sueldo;

    @NotNull(message = "La fecha de contratación es obligatoria")
    @PastOrPresent(message = "La fecha de contratación no puede ser futura")
    private LocalDate fechaContratacion;

    private Boolean activo;
}