package com.minimarket.msinventario.service;

import com.minimarket.msinventario.dto.MovimientoStockRequestDTO;
import com.minimarket.msinventario.dto.MovimientoStockResponseDTO;
import com.minimarket.msinventario.dto.StockRequestDTO;
import com.minimarket.msinventario.dto.StockResponseDTO;
import com.minimarket.msinventario.exception.RecursoNoEncontradoException;
import com.minimarket.msinventario.exception.StockDuplicadoException;
import com.minimarket.msinventario.exception.StockInsuficienteException;
import com.minimarket.msinventario.model.MovimientoStock;
import com.minimarket.msinventario.model.Stock;
import com.minimarket.msinventario.model.TipoMovimiento;
import com.minimarket.msinventario.repository.MovimientoStockRepository;
import com.minimarket.msinventario.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio ms-inventario.
 * Gestiona Stock y MovimientoStock de forma coordinada.
 *
 * Las operaciones de entrada/salida son TRANSACCIONALES: si falla
 * el registro del movimiento, se hace rollback del cambio en stock.
 */
@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    // ════════════════════════════════════════════════════════════
    // OPERACIONES SOBRE STOCK
    // ════════════════════════════════════════════════════════════

    public List<StockResponseDTO> listarTodos() {
        log.info("Listando todos los registros de stock");
        return stockRepository.findAll().stream()
                .map(this::convertirAStockResponseDTO)
                .collect(Collectors.toList());
    }

    public StockResponseDTO obtenerPorId(Long id) {
        log.info("Buscando stock con ID: {}", id);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Stock con ID {} no encontrado", id);
                    return new RecursoNoEncontradoException(
                            "Stock con ID " + id + " no encontrado");
                });
        return convertirAStockResponseDTO(stock);
    }

    public StockResponseDTO obtenerPorProductoId(Long productoId) {
        log.info("Buscando stock del producto: {}", productoId);
        Stock stock = stockRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe stock registrado para el producto con ID " + productoId));
        return convertirAStockResponseDTO(stock);
    }

    /**
     * Lista los stocks que requieren reposición (cantidad <= cantidadMinima).
     * Útil para alertas y reportes de stock crítico.
     */
    public List<StockResponseDTO> listarStockBajo() {
        log.info("Listando stocks con alerta de reposición");
        return stockRepository.findAll().stream()
                .filter(s -> s.getCantidadActual() <= s.getCantidadMinima())
                .map(this::convertirAStockResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo registro de stock para un producto.
     * Reglas de negocio:
     *   1. Un producto solo puede tener UN stock.
     *   2. Cantidad máxima debe ser >= cantidad actual.
     *   3. Cantidad mínima debe ser <= cantidad máxima.
     */
    public StockResponseDTO crearStock(StockRequestDTO dto) {
        log.info("Creando stock para producto ID: {}", dto.getProductoId());

        // Regla 1: producto único
        if (stockRepository.existsByProductoId(dto.getProductoId())) {
            log.warn("Ya existe stock para producto ID: {}", dto.getProductoId());
            throw new StockDuplicadoException(
                    "Ya existe un registro de stock para el producto con ID " + dto.getProductoId());
        }

        // Regla 2 y 3: validar coherencia de cantidades
        validarCoherenciaCantidades(dto.getCantidadActual(),
                dto.getCantidadMinima(),
                dto.getCantidadMaxima());

        Stock stock = new Stock();
        stock.setProductoId(dto.getProductoId());
        stock.setCantidadActual(dto.getCantidadActual());
        stock.setCantidadMinima(dto.getCantidadMinima());
        stock.setCantidadMaxima(dto.getCantidadMaxima());
        stock.setUbicacion(dto.getUbicacion());

        Stock guardado = stockRepository.save(stock);
        log.info("Stock creado exitosamente con ID: {}", guardado.getId());

        return convertirAStockResponseDTO(guardado);
    }

    /**
     * Actualiza la información de un stock existente (sin tocar la cantidad actual).
     * Para modificar la cantidad, se debe usar registrarMovimiento().
     */
    public StockResponseDTO actualizarStock(Long id, StockRequestDTO dto) {
        log.info("Actualizando stock con ID: {}", id);

        Stock existente = stockRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Stock con ID " + id + " no encontrado"));

        // Validar coherencia con la nueva configuración
        validarCoherenciaCantidades(existente.getCantidadActual(),
                dto.getCantidadMinima(),
                dto.getCantidadMaxima());

        existente.setCantidadMinima(dto.getCantidadMinima());
        existente.setCantidadMaxima(dto.getCantidadMaxima());
        existente.setUbicacion(dto.getUbicacion());

        Stock guardado = stockRepository.save(existente);
        log.info("Stock con ID {} actualizado", id);
        return convertirAStockResponseDTO(guardado);
    }

    public void eliminarStock(Long id) {
        log.info("Eliminando stock con ID: {}", id);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Stock con ID " + id + " no encontrado"));
        stockRepository.delete(stock);
        log.info("Stock con ID {} eliminado", id);
    }

    // ════════════════════════════════════════════════════════════
    // OPERACIONES SOBRE MOVIMIENTOS DE STOCK
    // ════════════════════════════════════════════════════════════

    /**
     * Registra un movimiento de stock (ENTRADA, SALIDA o AJUSTE)
     * y actualiza la cantidad del Stock asociado.
     *
     * Es @Transactional: si falla cualquier paso, se hace rollback
     * y el stock NO se modifica.
     *
     * Reglas de negocio:
     *   - ENTRADA: suma cantidad al stock.
     *   - SALIDA: resta cantidad. Si no hay suficiente, lanza StockInsuficienteException.
     *   - AJUSTE: establece la cantidad directamente al valor recibido.
     */
    @Transactional
    public MovimientoStockResponseDTO registrarMovimiento(MovimientoStockRequestDTO dto) {
        log.info("Registrando movimiento {} de {} unidades sobre stock ID: {}",
                dto.getTipoMovimiento(), dto.getCantidad(), dto.getStockId());

        Stock stock = stockRepository.findById(dto.getStockId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Stock con ID " + dto.getStockId() + " no encontrado"));

        // Aplicar el movimiento según su tipo
        switch (dto.getTipoMovimiento()) {
            case ENTRADA:
                aplicarEntrada(stock, dto.getCantidad());
                break;
            case SALIDA:
                aplicarSalida(stock, dto.getCantidad());
                break;
            case AJUSTE:
                aplicarAjuste(stock, dto.getCantidad());
                break;
        }

        // Guardar el stock actualizado
        stockRepository.save(stock);

        // Crear y guardar el movimiento
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setStock(stock);
        movimiento.setTipoMovimiento(dto.getTipoMovimiento());
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setMotivo(dto.getMotivo());

        MovimientoStock guardado = movimientoStockRepository.save(movimiento);
        log.info("Movimiento registrado exitosamente con ID: {}. Nuevo stock: {}",
                guardado.getId(), stock.getCantidadActual());

        return convertirAMovimientoResponseDTO(guardado);
    }

    /**
     * Lista el historial de movimientos de un stock específico.
     */
    public List<MovimientoStockResponseDTO> listarMovimientosPorStock(Long stockId) {
        log.info("Listando movimientos del stock ID: {}", stockId);
        return movimientoStockRepository
                .findByStockIdOrderByFechaMovimientoDesc(stockId).stream()
                .map(this::convertirAMovimientoResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos los movimientos por tipo (ENTRADA, SALIDA, AJUSTE).
     */
    public List<MovimientoStockResponseDTO> listarMovimientosPorTipo(TipoMovimiento tipo) {
        log.info("Listando movimientos de tipo: {}", tipo);
        return movimientoStockRepository.findByTipoMovimiento(tipo).stream()
                .map(this::convertirAMovimientoResponseDTO)
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS - LÓGICA DE MOVIMIENTOS
    // ════════════════════════════════════════════════════════════

    private void aplicarEntrada(Stock stock, Integer cantidad) {
        int nuevaCantidad = stock.getCantidadActual() + cantidad;
        if (nuevaCantidad > stock.getCantidadMaxima()) {
            throw new IllegalArgumentException(
                    "La entrada excede la capacidad máxima del stock (" +
                            stock.getCantidadMaxima() + ")");
        }
        stock.setCantidadActual(nuevaCantidad);
    }

    private void aplicarSalida(Stock stock, Integer cantidad) {
        if (stock.getCantidadActual() < cantidad) {
            log.warn("Stock insuficiente. Disponible: {}, requerido: {}",
                    stock.getCantidadActual(), cantidad);
            throw new StockInsuficienteException(
                    "Stock insuficiente. Disponible: " + stock.getCantidadActual() +
                            ", solicitado: " + cantidad);
        }
        stock.setCantidadActual(stock.getCantidadActual() - cantidad);
    }

    private void aplicarAjuste(Stock stock, Integer nuevaCantidad) {
        if (nuevaCantidad > stock.getCantidadMaxima()) {
            throw new IllegalArgumentException(
                    "El ajuste excede la capacidad máxima del stock");
        }
        stock.setCantidadActual(nuevaCantidad);
    }

    private void validarCoherenciaCantidades(Integer actual, Integer minima, Integer maxima) {
        if (minima > maxima) {
            throw new IllegalArgumentException(
                    "La cantidad mínima no puede ser mayor a la máxima");
        }
        if (actual > maxima) {
            throw new IllegalArgumentException(
                    "La cantidad actual no puede ser mayor a la máxima");
        }
    }

    // ════════════════════════════════════════════════════════════
    // CONVERSIONES ENTRE ENTIDAD Y DTO
    // ════════════════════════════════════════════════════════════

    private StockResponseDTO convertirAStockResponseDTO(Stock s) {
        boolean alerta = s.getCantidadActual() <= s.getCantidadMinima();
        return StockResponseDTO.builder()
                .id(s.getId())
                .productoId(s.getProductoId())
                .cantidadActual(s.getCantidadActual())
                .cantidadMinima(s.getCantidadMinima())
                .cantidadMaxima(s.getCantidadMaxima())
                .ubicacion(s.getUbicacion())
                .alertaReposicion(alerta)
                .fechaCreacion(s.getFechaCreacion())
                .fechaActualizacion(s.getFechaActualizacion())
                .build();
    }

    private MovimientoStockResponseDTO convertirAMovimientoResponseDTO(MovimientoStock m) {
        return MovimientoStockResponseDTO.builder()
                .id(m.getId())
                .stockId(m.getStock().getId())
                .productoId(m.getStock().getProductoId())
                .tipoMovimiento(m.getTipoMovimiento())
                .cantidad(m.getCantidad())
                .motivo(m.getMotivo())
                .fechaMovimiento(m.getFechaMovimiento())
                .build();
    }
}