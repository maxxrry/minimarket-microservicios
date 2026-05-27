package com.minimarket.msventas.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vista local del Empleado remoto (ms-empleados).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDTO {
    private Long id;
    private String rut;
    private String nombre;
    private String apellido;
    private Boolean activo;
}
