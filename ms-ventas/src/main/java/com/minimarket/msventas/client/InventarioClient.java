package com.minimarket.msventas.client;

import com.minimarket.msventas.client.dto.MovimientoStockRequestDTO;
import com.minimarket.msventas.client.dto.StockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-inventario", url = "${ms-inventario.url}")
public interface InventarioClient {

    @GetMapping("/api/inventario/stock/producto/{productoId}")
    StockDTO obtenerStockPorProducto(@PathVariable("productoId") Long productoId);

    @PostMapping("/api/inventario/movimientos")
    void registrarMovimiento(@RequestBody MovimientoStockRequestDTO dto);
}
