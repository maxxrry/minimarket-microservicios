package com.minimarket.msclientes.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Cliente registrado del minimarket.
 * Solo se registran clientes que quiere acumular puntos de fidelización,
 * pedir factura o comprar de forma frecuente.
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa un cliente registrado del minimarket")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del cliente", example = "1")
    private Long id;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    @Schema(description = "RUT del cliente", example = "12345678-9", maxLength = 12)
    private String rut;

    @Column(name = "nombre", nullable = false, length = 100)
    @Schema(description = "Nombre del cliente", example = "Juan", maxLength = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    @Schema(description = "Apellido del cliente", example = "Pérez", maxLength = 100)
    private String apellido;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @Schema(description = "Correo electrónico", example = "juan.perez@email.cl", maxLength = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    @Schema(description = "Teléfono de contacto", example = "+56912345678", maxLength = 20)
    private String telefono;

    @Column(name = "direccion", length = 200)
    @Schema(description = "Dirección del cliente", example = "Av. Principal 123", maxLength = 200)
    private String direccion;

    @Column(name = "fecha_nacimiento")
    @Schema(description = "Fecha de nacimiento", example = "1990-05-15")
    private LocalDate fechaNacimiento;

    @Column(name = "activo", nullable = false)
    @Schema(description = "Indica si el cliente está activo", example = "true")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @Schema(description = "Fecha de registro", example = "2026-01-15 10:30:00")
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    @Schema(description = "Fecha de última actualización", example = "2026-01-20 14:45:00")
    private LocalDateTime fechaActualizacion;
}