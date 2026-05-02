package com.minimarket.msinventario.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long productoId;

    @NotNull(message = "La cantidad actual es obligatoria")
    @Min(value = 0, message = "La cantidad actual no puede ser negativa")
    private Integer cantidadActual;

    @NotNull(message = "La cantidad mínima es obligatoria")
    @Min(value = 0, message = "La cantidad mínima no puede ser negativa")
    private Integer cantidadMinima;

    @NotNull(message = "La cantidad máxima es obligatoria")
    @Min(value = 1, message = "La cantidad máxima debe ser al menos 1")
    private Integer cantidadMaxima;

    @Size(max = 100, message = "La ubicación no puede superar los 100 caracteres")
    private String ubicacion;
}