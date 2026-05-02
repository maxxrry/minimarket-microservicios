package com.minimarket.msinventario.dto;

import com.minimarket.msinventario.model.TipoMovimiento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStockRequestDTO {

    @NotNull(message = "El ID del stock es obligatorio")
    @Positive(message = "El ID del stock debe ser positivo")
    private Long stockId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
    private String motivo;
}