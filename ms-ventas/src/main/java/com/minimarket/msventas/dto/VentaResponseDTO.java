package com.minimarket.msventas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.minimarket.msventas.model.EstadoVenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponseDTO {

    private Long id;
    private String numeroVenta;
    private Long clienteId;
    private Long empleadoId;
    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal iva;
    private BigDecimal total;
    private EstadoVenta estado;
    private List<DetalleVentaResponseDTO> detalles;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaVenta;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;
}