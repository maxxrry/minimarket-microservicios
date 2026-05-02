package com.minimarket.msinventario.controller;

import com.minimarket.msinventario.dto.MovimientoStockRequestDTO;
import com.minimarket.msinventario.dto.MovimientoStockResponseDTO;
import com.minimarket.msinventario.dto.StockRequestDTO;
import com.minimarket.msinventario.dto.StockResponseDTO;
import com.minimarket.msinventario.model.TipoMovimiento;
import com.minimarket.msinventario.service.StockService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del microservicio ms-inventario.
 * Expone endpoints para gestionar Stock y MovimientoStock.
 */
@RestController
@RequestMapping("/api/inventario")
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    @Autowired
    private StockService stockService;

    // ════════════════════════════════════════════════════════════
    // ENDPOINTS DE STOCK
    // ════════════════════════════════════════════════════════════

    @GetMapping("/stock")
    public ResponseEntity<List<StockResponseDTO>> listarTodos() {
        log.info("GET /api/inventario/stock");
        return ResponseEntity.ok(stockService.listarTodos());
    }

    @GetMapping("/stock/{id}")
    public ResponseEntity<StockResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/inventario/stock/{}", id);
        return ResponseEntity.ok(stockService.obtenerPorId(id));
    }

    @GetMapping("/stock/producto/{productoId}")
    public ResponseEntity<StockResponseDTO> obtenerPorProductoId(@PathVariable Long productoId) {
        log.info("GET /api/inventario/stock/producto/{}", productoId);
        return ResponseEntity.ok(stockService.obtenerPorProductoId(productoId));
    }

    @GetMapping("/stock/alerta-reposicion")
    public ResponseEntity<List<StockResponseDTO>> listarStockBajo() {
        log.info("GET /api/inventario/stock/alerta-reposicion");
        return ResponseEntity.ok(stockService.listarStockBajo());
    }

    @PostMapping("/stock")
    public ResponseEntity<StockResponseDTO> crearStock(
            @Valid @RequestBody StockRequestDTO dto) {
        log.info("POST /api/inventario/stock - producto ID: {}", dto.getProductoId());
        StockResponseDTO creado = stockService.crearStock(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/stock/{id}")
    public ResponseEntity<StockResponseDTO> actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody StockRequestDTO dto) {
        log.info("PUT /api/inventario/stock/{}", id);
        return ResponseEntity.ok(stockService.actualizarStock(id, dto));
    }

    @DeleteMapping("/stock/{id}")
    public ResponseEntity<Void> eliminarStock(@PathVariable Long id) {
        log.info("DELETE /api/inventario/stock/{}", id);
        stockService.eliminarStock(id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════
    // ENDPOINTS DE MOVIMIENTOS
    // ════════════════════════════════════════════════════════════

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoStockResponseDTO> registrarMovimiento(
            @Valid @RequestBody MovimientoStockRequestDTO dto) {
        log.info("POST /api/inventario/movimientos - tipo: {}", dto.getTipoMovimiento());
        MovimientoStockResponseDTO creado = stockService.registrarMovimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/movimientos/stock/{stockId}")
    public ResponseEntity<List<MovimientoStockResponseDTO>> listarMovimientosPorStock(
            @PathVariable Long stockId) {
        log.info("GET /api/inventario/movimientos/stock/{}", stockId);
        return ResponseEntity.ok(stockService.listarMovimientosPorStock(stockId));
    }

    @GetMapping("/movimientos/tipo/{tipo}")
    public ResponseEntity<List<MovimientoStockResponseDTO>> listarMovimientosPorTipo(
            @PathVariable TipoMovimiento tipo) {
        log.info("GET /api/inventario/movimientos/tipo/{}", tipo);
        return ResponseEntity.ok(stockService.listarMovimientosPorTipo(tipo));
    }
}