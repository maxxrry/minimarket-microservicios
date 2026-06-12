package com.minimarket.mscatalogo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida que se devuelve al cliente.
 * Representa la "vista pública" del Producto en la API.
 * Si en el futuro hay campos internos en la entidad que no deben exponerse,
 * simplemente no se incluyen aquí.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de respuesta con los datos de un producto")
public class ProductoResponseDTO {

    @Schema(description = "ID único del producto", example = "1")
    private Long id;

    @Schema(description = "Nombre del producto", example = "Coca-Cola 1.5L")
    private String nombre;

    @Schema(description = "Descripción del producto", example = "Bebida gaseosa cola")
    private String descripcion;

    @Schema(description = "Precio del producto", example = "2490.00")
    private BigDecimal precio;

    @Schema(description = "Código de barras", example = "7800000000001")
    private String codigoBarra;

    @Schema(description = "ID de la categoría", example = "1")
    private Long categoriaId;

    @Schema(description = "ID del proveedor", example = "1")
    private Long proveedorId;

    @Schema(description = "Indica si el producto está activo", example = "true")
    private Boolean activo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de creación", example = "2026-01-15 10:30:00")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Fecha de última actualización", example = "2026-01-20 14:45:00")
    private LocalDateTime fechaActualizacion;
}