package com.minimarket.msreportes.service;

import com.minimarket.msreportes.dto.ReporteRequestDTO;
import com.minimarket.msreportes.dto.ReporteResponseDTO;
import com.minimarket.msreportes.exception.RecursoNoEncontradoException;
import com.minimarket.msreportes.model.ReporteGenerado;
import com.minimarket.msreportes.model.TipoReporte;
import com.minimarket.msreportes.repository.ReporteGeneradoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio ms-reportes.
 *
 * NOTA: Este servicio guarda el HISTORIAL de reportes generados.
 * La GENERACIÓN real de reportes (consultar ms-ventas, ms-inventario, etc.)
 * se implementará con Feign Client en una próxima fase.
 */
@Service
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);

    @Autowired
    private ReporteGeneradoRepository reporteRepository;

    public List<ReporteResponseDTO> listarTodos() {
        log.info("Listando todos los reportes generados");
        return reporteRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public ReporteResponseDTO obtenerPorId(Long id) {
        log.info("Buscando reporte con ID: {}", id);
        ReporteGenerado reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Reporte con ID " + id + " no encontrado"));
        return convertirAResponseDTO(reporte);
    }

    public List<ReporteResponseDTO> listarPorTipo(TipoReporte tipo) {
        log.info("Listando reportes de tipo: {}", tipo);
        return reporteRepository.findByTipoReporteOrderByFechaGeneracionDesc(tipo).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReporteResponseDTO> listarPorEmpleado(Long empleadoId) {
        log.info("Listando reportes generados por empleado: {}", empleadoId);
        return reporteRepository.findByGeneradoPorOrderByFechaGeneracionDesc(empleadoId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un registro de reporte generado.
     * El resultadoJson se asigna como string vacío por ahora.
     * Cuando se implemente Feign, se generará dinámicamente consultando
     * los microservicios correspondientes.
     */
    public ReporteResponseDTO crear(ReporteRequestDTO dto) {
        log.info("Generando nuevo reporte de tipo: {}", dto.getTipoReporte());

        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(dto.getTipoReporte());
        reporte.setDescripcion(dto.getDescripcion());
        reporte.setParametros(dto.getParametros());
        reporte.setGeneradoPor(dto.getGeneradoPor());

        // TODO: cuando se implemente Feign, aquí se llamarán a otros microservicios
        // y se construirá el resultadoJson dinámicamente. Por ahora queda vacío.
        reporte.setResultadoJson("{\"pendiente\": \"Implementar consulta vía Feign Client\"}");

        ReporteGenerado guardado = reporteRepository.save(reporte);
        log.info("Reporte creado exitosamente con ID: {}", guardado.getId());
        return convertirAResponseDTO(guardado);
    }

    public void eliminar(Long id) {
        log.info("Eliminando reporte con ID: {}", id);
        ReporteGenerado reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Reporte con ID " + id + " no encontrado"));
        reporteRepository.delete(reporte);
        log.info("Reporte con ID {} eliminado", id);
    }

    private ReporteResponseDTO convertirAResponseDTO(ReporteGenerado r) {
        return ReporteResponseDTO.builder()
                .id(r.getId())
                .tipoReporte(r.getTipoReporte())
                .descripcion(r.getDescripcion())
                .parametros(r.getParametros())
                .resultadoJson(r.getResultadoJson())
                .generadoPor(r.getGeneradoPor())
                .fechaGeneracion(r.getFechaGeneracion())
                .build();
    }
}