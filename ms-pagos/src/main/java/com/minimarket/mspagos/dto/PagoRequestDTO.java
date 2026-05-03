package com.minimarket.mspagos.dto;

import com.minimarket.mspagos.model.MetodoPago;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear o actualizar un Pago.
 * Aplica validaciones de entrada antes de llegar al service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequestDTO {

    @NotBlank(message = "El número de transacción es obligatorio")
    @Size(max = 30, message = "El número de transacción no puede tener más de 30 caracteres")
    private String numeroTransaccion;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "El ID de venta es obligatorio")
    @Positive(message = "El ID de venta debe ser positivo")
    private Long ventaId;

    @Size(max = 300, message = "Las observaciones no pueden tener más de 300 caracteres")
    private String observaciones;
}