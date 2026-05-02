package com.minimarket.msinventario.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.minimarket.msinventario.model.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStockResponseDTO {

    private Long id;
    private Long stockId;
    private Long productoId;
    private TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private String motivo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaMovimiento;
}