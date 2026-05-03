package com.minimarket.msreportes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que registra un reporte generado.
 * Sirve como historial para auditoría y evitar regenerar el mismo reporte.
 */
@Entity
@Table(name = "reportes_generados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte", nullable = false, length = 50)
    private TipoReporte tipoReporte;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "parametros", length = 500)
    private String parametros;

    /**
     * El resultado del reporte serializado como JSON.
     * Tipo TEXT para soportar reportes grandes.
     */
    @Lob
    @Column(name = "resultado_json", columnDefinition = "TEXT")
    private String resultadoJson;

    @CreationTimestamp
    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion;

    /**
     * ID del empleado que generó el reporte.
     * Referencia lógica al microservicio ms-empleados.
     */
    @Column(name = "generado_por")
    private Long generadoPor;
}