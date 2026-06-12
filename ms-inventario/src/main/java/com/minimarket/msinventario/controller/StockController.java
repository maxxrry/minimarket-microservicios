package com.minimarket.msinventario.controller;

import com.minimarket.msinventario.dto.MovimientoStockRequestDTO;
import com.minimarket.msinventario.dto.MovimientoStockResponseDTO;
import com.minimarket.msinventario.dto.StockRequestDTO;
import com.minimarket.msinventario.dto.StockResponseDTO;
import com.minimarket.msinventario.model.TipoMovimiento;
import com.minimarket.msinventario.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventario", description = "Endpoints para gestionar stock y movimientos de inventario")
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    @Autowired
    private StockService stockService;

    // ════════════════════════════════════════════════════════════
    // ENDPOINTS DE STOCK
    // ════════════════════════════════════════════════════════════

    @GetMapping("/stock")
    @Operation(summary = "Listar todo el stock", description = "Retorna una lista con todos los registros de stock")
    public ResponseEntity<List<StockResponseDTO>> listarTodos() {
        log.info("GET /api/inventario/stock");
        return ResponseEntity.ok(stockService.listarTodos());
    }

    @GetMapping("/stock/{id}")
    @Operation(summary = "Obtener stock por ID", description = "Retorna un registro de stock específico según su ID")
    public ResponseEntity<StockResponseDTO> obtenerPorId(
            @Parameter(description = "ID del stock a buscar", example = "1")
            @PathVariable Long id) {
        log.info("GET /api/inventario/stock/{}", id);
        return ResponseEntity.ok(stockService.obtenerPorId(id));
    }

    @GetMapping("/stock/producto/{productoId}")
    @Operation(summary = "Obtener stock por ID de producto", description = "Retorna el stock de un producto específico")
    public ResponseEntity<StockResponseDTO> obtenerPorProductoId(
            @Parameter(description = "ID del producto", example = "1")
            @PathVariable Long productoId) {
        log.info("GET /api/inventario/stock/producto/{}", productoId);
        return ResponseEntity.ok(stockService.obtenerPorProductoId(productoId));
    }

    @GetMapping("/stock/alerta-reposicion")
    @Operation(summary = "Listar stock bajo", description = "Retorna los productos con stock bajo el mínimo de reposición")
    public ResponseEntity<List<StockResponseDTO>> listarStockBajo() {
        log.info("GET /api/inventario/stock/alerta-reposicion");
        return ResponseEntity.ok(stockService.listarStockBajo());
    }

    @PostMapping("/stock")
    @Operation(summary = "Crear registro de stock", description = "Crea un nuevo registro de stock para un producto")
    public ResponseEntity<StockResponseDTO> crearStock(
            @Valid @RequestBody StockRequestDTO dto) {
        log.info("POST /api/inventario/stock - producto ID: {}", dto.getProductoId());
        StockResponseDTO creado = stockService.crearStock(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/stock/{id}")
    @Operation(summary = "Actualizar stock", description = "Actualiza los datos de un registro de stock existente")
    public ResponseEntity<StockResponseDTO> actualizarStock(
            @Parameter(description = "ID del stock a actualizar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StockRequestDTO dto) {
        log.info("PUT /api/inventario/stock/{}", id);
        return ResponseEntity.ok(stockService.actualizarStock(id, dto));
    }

    @DeleteMapping("/stock/{id}")
    @Operation(summary = "Eliminar stock", description = "Elimina un registro de stock")
    public ResponseEntity<Void> eliminarStock(
            @Parameter(description = "ID del stock a eliminar", example = "1")
            @PathVariable Long id) {
        log.info("DELETE /api/inventario/stock/{}", id);
        stockService.eliminarStock(id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════
    // ENDPOINTS DE MOVIMIENTOS
    // ════════════════════════════════════════════════════════════

    @PostMapping("/movimientos")
    @Operation(summary = "Registrar movimiento", description = "Registra un movimiento de stock (entrada o salida)")
    public ResponseEntity<MovimientoStockResponseDTO> registrarMovimiento(
            @Valid @RequestBody MovimientoStockRequestDTO dto) {
        log.info("POST /api/inventario/movimientos - tipo: {}", dto.getTipoMovimiento());
        MovimientoStockResponseDTO creado = stockService.registrarMovimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/movimientos/stock/{stockId}")
    @Operation(summary = "Listar movimientos por stock", description = "Retorna los movimientos de un stock específico")
    public ResponseEntity<List<MovimientoStockResponseDTO>> listarMovimientosPorStock(
            @Parameter(description = "ID del stock", example = "1")
            @PathVariable Long stockId) {
        log.info("GET /api/inventario/movimientos/stock/{}", stockId);
        return ResponseEntity.ok(stockService.listarMovimientosPorStock(stockId));
    }

    @GetMapping("/movimientos/tipo/{tipo}")
    @Operation(summary = "Listar movimientos por tipo", description = "Retorna los movimientos de un tipo específico")
    public ResponseEntity<List<MovimientoStockResponseDTO>> listarMovimientosPorTipo(
            @Parameter(description = "Tipo de movimiento", example = "ENTRADA")
            @PathVariable TipoMovimiento tipo) {
        log.info("GET /api/inventario/movimientos/tipo/{}", tipo);
        return ResponseEntity.ok(stockService.listarMovimientosPorTipo(tipo));
    }
}