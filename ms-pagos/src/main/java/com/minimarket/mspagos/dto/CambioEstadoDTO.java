package com.minimarket.mspagos.dto;

import com.minimarket.mspagos.model.EstadoPago;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el endpoint de cambio de estado del pago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoPago nuevoEstado;
}