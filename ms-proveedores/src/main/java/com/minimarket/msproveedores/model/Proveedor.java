package com.minimarket.msproveedores.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Proveedor del minimarket.
 * Mapea a la tabla "proveedores" en la base de datos mm_proveedores.
 */
@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Razón social legal de la empresa proveedora.
     * Ej: "Distribuidora Soprole S.A."
     */
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    /**
     * RUT chileno del proveedor. Único.
     * Formato: XX.XXX.XXX-X (con puntos y guion).
     */
    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    /**
     * Persona de contacto en la empresa proveedora.
     */
    @Column(name = "nombre_contacto", length = 100)
    private String nombreContacto;

    /**
     * Email de contacto único.
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Teléfono de contacto.
     */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Dirección física del proveedor.
     */
    @Column(name = "direccion", length = 200)
    private String direccion;

    /**
     * Ciudad donde opera el proveedor.
     */
    @Column(name = "ciudad", length = 50)
    private String ciudad;

    /**
     * Estado activo/inactivo (borrado lógico).
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}