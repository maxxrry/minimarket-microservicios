package com.minimarket.msempleados.service;

import com.minimarket.msempleados.dto.EmpleadoRequestDTO;
import com.minimarket.msempleados.dto.EmpleadoResponseDTO;
import com.minimarket.msempleados.exception.EmpleadoDuplicadoException;
import com.minimarket.msempleados.exception.RecursoNoEncontradoException;
import com.minimarket.msempleados.model.Cargo;
import com.minimarket.msempleados.model.Empleado;
import com.minimarket.msempleados.repository.EmpleadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmpleadoService - Tests unitarios")
class EmpleadoServiceTest {

    @Mock private EmpleadoRepository empleadoRepository;
    @InjectMocks private EmpleadoService empleadoService;

    @Nested @DisplayName("listarTodos()")
    class ListarTodos {
        @Test @DisplayName("Mapea todos los empleados")
        void ok() {
            when(empleadoRepository.findAll()).thenReturn(List.of(crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true)));
            assertEquals(1, empleadoService.listarTodos().size());
        }
    }

    @Nested @DisplayName("listarActivos()")
    class ListarActivos {
        @Test @DisplayName("Solo activos")
        void ok() {
            when(empleadoRepository.findByActivoTrue()).thenReturn(List.of(crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true)));
            assertEquals(1, empleadoService.listarActivos().size());
        }
    }

    @Nested @DisplayName("buscarPorId()")
    class BuscarPorId {
        @Test @DisplayName("Existe")
        void ok() {
            when(empleadoRepository.findById(1L)).thenReturn(Optional.of(crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true)));
            assertEquals(1L, empleadoService.buscarPorId(1L).getId());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(empleadoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> empleadoService.buscarPorId(99L));
        }
    }

    @Nested @DisplayName("buscarPorRut()")
    class BuscarPorRut {
        @Test @DisplayName("Existe")
        void ok() {
            when(empleadoRepository.findByRut("11.111.111-1"))
                    .thenReturn(Optional.of(crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true)));
            assertEquals("11.111.111-1", empleadoService.buscarPorRut("11.111.111-1").getRut());
        }
        @Test @DisplayName("No existe → excepción")
        void noExiste() {
            when(empleadoRepository.findByRut("X")).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> empleadoService.buscarPorRut("X"));
        }
    }

    @Nested @DisplayName("buscarPorCargo()")
    class BuscarPorCargo {
        @Test @DisplayName("Filtra por cargo")
        void ok() {
            when(empleadoRepository.findByCargo(Cargo.SUPERVISOR))
                    .thenReturn(List.of(crear(1L, "11.111.111-1", "a@a.cl", Cargo.SUPERVISOR, true)));
            assertEquals(1, empleadoService.buscarPorCargo(Cargo.SUPERVISOR).size());
        }
    }

    @Nested @DisplayName("crear()")
    class Crear {
        @Test @DisplayName("RUT y email únicos → crea")
        void ok() {
            EmpleadoRequestDTO dto = crearDTO("11.111.111-1", "a@a.cl", Cargo.CAJERO, true);
            when(empleadoRepository.existsByRut("11.111.111-1")).thenReturn(false);
            when(empleadoRepository.existsByEmailIgnoreCase("a@a.cl")).thenReturn(false);
            when(empleadoRepository.save(any(Empleado.class)))
                    .thenAnswer(inv -> { Empleado e = inv.getArgument(0); e.setId(5L); return e; });
            EmpleadoResponseDTO r = empleadoService.crear(dto);
            assertEquals(5L, r.getId());
        }

        @Test @DisplayName("activo=null → default true")
        void defaultActivo() {
            EmpleadoRequestDTO dto = crearDTO("11.111.111-1", "a@a.cl", Cargo.CAJERO, null);
            when(empleadoRepository.existsByRut(any())).thenReturn(false);
            when(empleadoRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
            ArgumentCaptor<Empleado> captor = ArgumentCaptor.forClass(Empleado.class);
            when(empleadoRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
            empleadoService.crear(dto);
            assertTrue(captor.getValue().getActivo());
        }

        @Test @DisplayName("RUT duplicado → EmpleadoDuplicadoException")
        void rutDuplicado() {
            when(empleadoRepository.existsByRut("11.111.111-1")).thenReturn(true);
            assertThrows(EmpleadoDuplicadoException.class,
                    () -> empleadoService.crear(crearDTO("11.111.111-1", "a@a.cl", Cargo.CAJERO, true)));
            verify(empleadoRepository, never()).save(any());
        }

        @Test @DisplayName("Email duplicado → EmpleadoDuplicadoException")
        void emailDuplicado() {
            when(empleadoRepository.existsByRut(any())).thenReturn(false);
            when(empleadoRepository.existsByEmailIgnoreCase("dup@x.cl")).thenReturn(true);
            assertThrows(EmpleadoDuplicadoException.class,
                    () -> empleadoService.crear(crearDTO("22.222.222-2", "dup@x.cl", Cargo.CAJERO, true)));
        }
    }

    @Nested @DisplayName("actualizar()")
    class Actualizar {
        @Test @DisplayName("Sin cambio → no valida duplicados")
        void sinCambios() {
            Empleado existente = crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true);
            EmpleadoRequestDTO dto = crearDTO("11.111.111-1", "a@a.cl", Cargo.SUPERVISOR, true);
            when(empleadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
            EmpleadoResponseDTO r = empleadoService.actualizar(1L, dto);
            assertEquals(Cargo.SUPERVISOR, r.getCargo());
            verify(empleadoRepository, never()).existsByRut(any());
        }

        @Test @DisplayName("RUT nuevo duplicado → excepción")
        void rutNuevoDuplicado() {
            Empleado existente = crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true);
            when(empleadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(empleadoRepository.existsByRut("33.333.333-3")).thenReturn(true);
            assertThrows(EmpleadoDuplicadoException.class,
                    () -> empleadoService.actualizar(1L, crearDTO("33.333.333-3", "a@a.cl", Cargo.CAJERO, true)));
        }

        @Test @DisplayName("Email nuevo duplicado → excepción")
        void emailNuevoDuplicado() {
            Empleado existente = crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true);
            when(empleadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(empleadoRepository.existsByEmailIgnoreCase("otro@x.cl")).thenReturn(true);
            assertThrows(EmpleadoDuplicadoException.class,
                    () -> empleadoService.actualizar(1L, crearDTO("11.111.111-1", "otro@x.cl", Cargo.CAJERO, true)));
        }

        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(empleadoRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> empleadoService.actualizar(404L, crearDTO("X", "x@x.cl", Cargo.CAJERO, true)));
        }

        @Test @DisplayName("activo=null conserva valor")
        void activoNullConserva() {
            Empleado existente = crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true);
            when(empleadoRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
            EmpleadoResponseDTO r = empleadoService.actualizar(1L, crearDTO("11.111.111-1", "a@a.cl", Cargo.CAJERO, null));
            assertTrue(r.getActivo());
        }
    }

    @Nested @DisplayName("desactivar()")
    class Desactivar {
        @Test @DisplayName("Existe → activo=false")
        void ok() {
            Empleado e = crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, true);
            when(empleadoRepository.findById(1L)).thenReturn(Optional.of(e));
            when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
            empleadoService.desactivar(1L);
            assertFalse(e.getActivo());
        }
        @Test @DisplayName("No existe → excepción")
        void noExiste() {
            when(empleadoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> empleadoService.desactivar(99L));
        }
    }

    @Nested @DisplayName("reactivar()")
    class Reactivar {
        @Test @DisplayName("Existe → activo=true")
        void ok() {
            Empleado e = crear(1L, "11.111.111-1", "a@a.cl", Cargo.CAJERO, false);
            when(empleadoRepository.findById(1L)).thenReturn(Optional.of(e));
            when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
            assertTrue(empleadoService.reactivar(1L).getActivo());
        }
        @Test @DisplayName("No existe → excepción")
        void noExiste() {
            when(empleadoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> empleadoService.reactivar(99L));
        }
    }

    // Helpers
    private Empleado crear(Long id, String rut, String email, Cargo cargo, Boolean activo) {
        Empleado e = new Empleado();
        e.setId(id);
        e.setRut(rut);
        e.setNombre("Maria");
        e.setApellido("Gonzalez");
        e.setEmail(email);
        e.setTelefono("+56912345678");
        e.setCargo(cargo);
        e.setSueldo(new BigDecimal("650000.00"));
        e.setFechaContratacion(LocalDate.of(2024, 1, 1));
        e.setActivo(activo);
        return e;
    }

    private EmpleadoRequestDTO crearDTO(String rut, String email, Cargo cargo, Boolean activo) {
        EmpleadoRequestDTO dto = new EmpleadoRequestDTO();
        dto.setRut(rut);
        dto.setNombre("Maria");
        dto.setApellido("Gonzalez");
        dto.setEmail(email);
        dto.setTelefono("+56912345678");
        dto.setCargo(cargo);
        dto.setSueldo(new BigDecimal("650000.00"));
        dto.setFechaContratacion(LocalDate.of(2024, 1, 1));
        dto.setActivo(activo);
        return dto;
    }
}
