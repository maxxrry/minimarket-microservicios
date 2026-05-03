package com.minimarket.mspagos.dto;

import com.minimarket.mspagos.model.EstadoPago;
import com.minimarket.mspagos.model.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {
    private Long id;
    private String numeroTransaccion;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estado;
    private Long ventaId;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}