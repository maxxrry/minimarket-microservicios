package com.minimarket.msproveedores.service;

import com.minimarket.msproveedores.dto.ProveedorRequestDTO;
import com.minimarket.msproveedores.dto.ProveedorResponseDTO;
import com.minimarket.msproveedores.exception.ProveedorDuplicadoException;
import com.minimarket.msproveedores.exception.RecursoNoEncontradoException;
import com.minimarket.msproveedores.model.Proveedor;
import com.minimarket.msproveedores.repository.ProveedorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProveedorService - Tests unitarios")
class ProveedorServiceTest {

    @Mock private ProveedorRepository proveedorRepository;
    @InjectMocks private ProveedorService proveedorService;

    @Nested @DisplayName("listarTodos()")
    class ListarTodos {
        @Test @DisplayName("Mapea proveedores a DTO")
        void listarTodos_ok() {
            // GIVEN
            when(proveedorRepository.findAll()).thenReturn(List.of(crear(1L, "ACME", "11.111.111-1", "a@a.cl", true)));
            // WHEN
            List<ProveedorResponseDTO> r = proveedorService.listarTodos();
            // THEN
            assertEquals(1, r.size());
            verify(proveedorRepository).findAll();
        }
    }

    @Nested @DisplayName("listarActivos()")
    class ListarActivos {
        @Test @DisplayName("Solo proveedores activos")
        void listarActivos_ok() {
            when(proveedorRepository.findByActivoTrue()).thenReturn(List.of(crear(1L, "ACME", "11.111.111-1", "a@a.cl", true)));
            assertEquals(1, proveedorService.listarActivos().size());
            verify(proveedorRepository).findByActivoTrue();
        }
    }

    @Nested @DisplayName("obtenerPorId()")
    class ObtenerPorId {
        @Test @DisplayName("Existe → devuelve DTO")
        void ok() {
            when(proveedorRepository.findById(1L)).thenReturn(Optional.of(crear(1L, "ACME", "11.111.111-1", "a@a.cl", true)));
            assertEquals("ACME", proveedorService.obtenerPorId(1L).getRazonSocial());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> proveedorService.obtenerPorId(99L));
        }
    }

    @Nested @DisplayName("listarPorCiudad()")
    class ListarPorCiudad {
        @Test @DisplayName("Devuelve proveedores por ciudad (case-insensitive)")
        void ok() {
            when(proveedorRepository.findByCiudadIgnoreCase("Santiago"))
                    .thenReturn(List.of(crear(1L, "ACME", "11.111.111-1", "a@a.cl", true)));
            assertEquals(1, proveedorService.listarPorCiudad("Santiago").size());
        }
    }

    @Nested @DisplayName("crear()")
    class Crear {
        @Test @DisplayName("RUT y email únicos → crea proveedor")
        void ok() {
            // GIVEN
            ProveedorRequestDTO dto = crearDTO("ACME", "11.111.111-1", "a@a.cl", true);
            when(proveedorRepository.existsByRut("11.111.111-1")).thenReturn(false);
            when(proveedorRepository.existsByEmailIgnoreCase("a@a.cl")).thenReturn(false);
            when(proveedorRepository.save(any(Proveedor.class)))
                    .thenAnswer(inv -> { Proveedor p = inv.getArgument(0); p.setId(7L); return p; });
            // WHEN
            ProveedorResponseDTO r = proveedorService.crear(dto);
            // THEN
            assertEquals(7L, r.getId());
        }

        @Test @DisplayName("activo=null → default true")
        void defaultActivo() {
            ProveedorRequestDTO dto = crearDTO("ACME", "11.111.111-1", "a@a.cl", null);
            when(proveedorRepository.existsByRut(any())).thenReturn(false);
            when(proveedorRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
            ArgumentCaptor<Proveedor> captor = ArgumentCaptor.forClass(Proveedor.class);
            when(proveedorRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
            proveedorService.crear(dto);
            assertTrue(captor.getValue().getActivo());
        }

        @Test @DisplayName("RUT duplicado → ProveedorDuplicadoException")
        void rutDuplicado() {
            when(proveedorRepository.existsByRut("11.111.111-1")).thenReturn(true);
            assertThrows(ProveedorDuplicadoException.class,
                    () -> proveedorService.crear(crearDTO("X", "11.111.111-1", "x@x.cl", true)));
            verify(proveedorRepository, never()).save(any());
        }

        @Test @DisplayName("Email duplicado → ProveedorDuplicadoException")
        void emailDuplicado() {
            when(proveedorRepository.existsByRut("22.222.222-2")).thenReturn(false);
            when(proveedorRepository.existsByEmailIgnoreCase("dup@x.cl")).thenReturn(true);
            assertThrows(ProveedorDuplicadoException.class,
                    () -> proveedorService.crear(crearDTO("X", "22.222.222-2", "dup@x.cl", true)));
            verify(proveedorRepository, never()).save(any());
        }
    }

    @Nested @DisplayName("actualizar()")
    class Actualizar {
        @Test @DisplayName("Mismo RUT y email → no valida duplicados")
        void sinCambios() {
            Proveedor existente = crear(1L, "ACME", "11.111.111-1", "a@a.cl", true);
            ProveedorRequestDTO dto = crearDTO("ACME II", "11.111.111-1", "a@a.cl", true);
            when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
            ProveedorResponseDTO r = proveedorService.actualizar(1L, dto);
            assertEquals("ACME II", r.getRazonSocial());
            verify(proveedorRepository, never()).existsByRut(any());
            verify(proveedorRepository, never()).existsByEmailIgnoreCase(any());
        }

        @Test @DisplayName("Nuevo RUT duplicado → ProveedorDuplicadoException")
        void rutNuevoDuplicado() {
            Proveedor existente = crear(1L, "ACME", "11.111.111-1", "a@a.cl", true);
            when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(proveedorRepository.existsByRut("99.999.999-9")).thenReturn(true);
            assertThrows(ProveedorDuplicadoException.class,
                    () -> proveedorService.actualizar(1L, crearDTO("X", "99.999.999-9", "a@a.cl", true)));
        }

        @Test @DisplayName("Nuevo email duplicado → ProveedorDuplicadoException")
        void emailNuevoDuplicado() {
            Proveedor existente = crear(1L, "ACME", "11.111.111-1", "a@a.cl", true);
            when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(proveedorRepository.existsByEmailIgnoreCase("otro@x.cl")).thenReturn(true);
            assertThrows(ProveedorDuplicadoException.class,
                    () -> proveedorService.actualizar(1L, crearDTO("X", "11.111.111-1", "otro@x.cl", true)));
        }

        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(proveedorRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> proveedorService.actualizar(404L, crearDTO("X", "X-X", "x@x.cl", true)));
        }

        @Test @DisplayName("activo=null conserva valor")
        void activoNullConserva() {
            Proveedor existente = crear(1L, "ACME", "11.111.111-1", "a@a.cl", true);
            ProveedorRequestDTO dto = crearDTO("ACME", "11.111.111-1", "a@a.cl", null);
            when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
            ProveedorResponseDTO r = proveedorService.actualizar(1L, dto);
            assertTrue(r.getActivo());
        }
    }

    @Nested @DisplayName("darDeBaja()")
    class DarDeBaja {
        @Test @DisplayName("Existe → marca activo=false")
        void ok() {
            Proveedor existente = crear(1L, "ACME", "11.111.111-1", "a@a.cl", true);
            when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
            proveedorService.darDeBaja(1L);
            assertFalse(existente.getActivo());
            verify(proveedorRepository, never()).deleteById(any());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> proveedorService.darDeBaja(99L));
        }
    }

    @Nested @DisplayName("reactivar()")
    class Reactivar {
        @Test @DisplayName("Existe → activo=true")
        void ok() {
            Proveedor existente = crear(1L, "ACME", "11.111.111-1", "a@a.cl", false);
            when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
            assertTrue(proveedorService.reactivar(1L).getActivo());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> proveedorService.reactivar(99L));
        }
    }

    // Helpers
    private Proveedor crear(Long id, String razon, String rut, String email, Boolean activo) {
        Proveedor p = new Proveedor();
        p.setId(id);
        p.setRazonSocial(razon);
        p.setRut(rut);
        p.setNombreContacto("contacto");
        p.setEmail(email);
        p.setTelefono("+56 9 1234 5678");
        p.setDireccion("Calle 1");
        p.setCiudad("Santiago");
        p.setActivo(activo);
        return p;
    }

    private ProveedorRequestDTO crearDTO(String razon, String rut, String email, Boolean activo) {
        ProveedorRequestDTO dto = new ProveedorRequestDTO();
        dto.setRazonSocial(razon);
        dto.setRut(rut);
        dto.setNombreContacto("contacto");
        dto.setEmail(email);
        dto.setTelefono("+56 9 1234 5678");
        dto.setDireccion("Calle 1");
        dto.setCiudad("Santiago");
        dto.setActivo(activo);
        return dto;
    }
}
