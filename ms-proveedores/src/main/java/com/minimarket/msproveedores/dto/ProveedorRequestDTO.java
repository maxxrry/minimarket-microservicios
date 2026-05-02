package com.minimarket.msproveedores.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRequestDTO {

    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 3, max = 150, message = "La razón social debe tener entre 3 y 150 caracteres")
    private String razonSocial;

    /**
     * RUT chileno con formato XX.XXX.XXX-X
     * Ej: 76.123.456-7 o 76.123.456-K
     */
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3}-[0-9Kk]$",
            message = "El RUT debe tener formato XX.XXX.XXX-X (ej: 76.123.456-7)")
    private String rut;

    @Size(max = 100, message = "El nombre de contacto no puede superar los 100 caracteres")
    private String nombreContacto;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$",
            message = "El teléfono debe tener entre 8 y 20 dígitos, puede incluir + y espacios")
    private String telefono;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String direccion;

    @Size(max = 50, message = "La ciudad no puede superar los 50 caracteres")
    private String ciudad;

    private Boolean activo;
}