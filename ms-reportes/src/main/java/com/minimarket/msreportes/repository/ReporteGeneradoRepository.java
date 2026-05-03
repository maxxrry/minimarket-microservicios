package com.minimarket.msreportes.repository;

import com.minimarket.msreportes.model.ReporteGenerado;
import com.minimarket.msreportes.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteGeneradoRepository extends JpaRepository<ReporteGenerado, Long> {

    List<ReporteGenerado> findByTipoReporteOrderByFechaGeneracionDesc(TipoReporte tipo);

    List<ReporteGenerado> findByGeneradoPorOrderByFechaGeneracionDesc(Long empleadoId);
}