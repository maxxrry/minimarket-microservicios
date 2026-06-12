package com.minimarket.msclientes.service;

import com.minimarket.msclientes.dto.ClienteRequestDTO;
import com.minimarket.msclientes.dto.ClienteResponseDTO;
import com.minimarket.msclientes.exception.ClienteDuplicadoException;
import com.minimarket.msclientes.exception.RecursoNoEncontradoException;
import com.minimarket.msclientes.model.Cliente;
import com.minimarket.msclientes.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService - Tests unitarios")
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private ClienteService clienteService;

    @Nested @DisplayName("listarTodos()")
    class ListarTodos {
        @Test @DisplayName("Mapea clientes")
        void ok() {
            when(clienteRepository.findAll()).thenReturn(List.of(crear(1L, "11.111.111-1", "a@a.cl", true)));
            assertEquals(1, clienteService.listarTodos().size());
        }
    }

    @Nested @DisplayName("listarActivos()")
    class ListarActivos {
        @Test @DisplayName("Solo activos")
        void ok() {
            when(clienteRepository.findByActivoTrue()).thenReturn(List.of(crear(1L, "11.111.111-1", "a@a.cl", true)));
            assertEquals(1, clienteService.listarActivos().size());
            verify(clienteRepository, never()).findAll();
        }
    }

    @Nested @DisplayName("buscarPorId()")
    class BuscarPorId {
        @Test @DisplayName("Existe")
        void ok() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(crear(1L, "11.111.111-1", "a@a.cl", true)));
            assertEquals(1L, clienteService.buscarPorId(1L).getId());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> clienteService.buscarPorId(99L));
        }
    }

    @Nested @DisplayName("buscarPorRut()")
    class BuscarPorRut {
        @Test @DisplayName("Existe")
        void ok() {
            when(clienteRepository.findByRut("11.111.111-1")).thenReturn(Optional.of(crear(1L, "11.111.111-1", "a@a.cl", true)));
            assertEquals("11.111.111-1", clienteService.buscarPorRut("11.111.111-1").getRut());
        }
        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(clienteRepository.findByRut("X")).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> clienteService.buscarPorRut("X"));
        }
    }

    @Nested @DisplayName("crear()")
    class Crear {
        @Test @DisplayName("RUT y email únicos → crea")
        void ok() {
            ClienteRequestDTO dto = crearDTO("11.111.111-1", "a@a.cl", true);
            when(clienteRepository.existsByRut("11.111.111-1")).thenReturn(false);
            when(clienteRepository.existsByEmailIgnoreCase("a@a.cl")).thenReturn(false);
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> { Cliente c = inv.getArgument(0); c.setId(5L); return c; });
            ClienteResponseDTO r = clienteService.crear(dto);
            assertEquals(5L, r.getId());
        }

        @Test @DisplayName("activo=null → default true")
        void defaultActivo() {
            ClienteRequestDTO dto = crearDTO("11.111.111-1", "a@a.cl", null);
            when(clienteRepository.existsByRut(any())).thenReturn(false);
            when(clienteRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            when(clienteRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
            clienteService.crear(dto);
            assertTrue(captor.getValue().getActivo());
        }

        @Test @DisplayName("RUT duplicado → ClienteDuplicadoException")
        void rutDuplicado() {
            when(clienteRepository.existsByRut("11.111.111-1")).thenReturn(true);
            assertThrows(ClienteDuplicadoException.class,
                    () -> clienteService.crear(crearDTO("11.111.111-1", "a@a.cl", true)));
            verify(clienteRepository, never()).save(any());
        }

        @Test @DisplayName("Email duplicado → ClienteDuplicadoException")
        void emailDuplicado() {
            when(clienteRepository.existsByRut("22.222.222-2")).thenReturn(false);
            when(clienteRepository.existsByEmailIgnoreCase("dup@x.cl")).thenReturn(true);
            assertThrows(ClienteDuplicadoException.class,
                    () -> clienteService.crear(crearDTO("22.222.222-2", "dup@x.cl", true)));
        }
    }

    @Nested @DisplayName("actualizar()")
    class Actualizar {
        @Test @DisplayName("Sin cambio de RUT/email → no valida duplicados")
        void sinCambios() {
            Cliente existente = crear(1L, "11.111.111-1", "a@a.cl", true);
            ClienteRequestDTO dto = crearDTO("11.111.111-1", "a@a.cl", true);
            dto.setNombre("Nuevo");
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
            ClienteResponseDTO r = clienteService.actualizar(1L, dto);
            assertEquals("Nuevo", r.getNombre());
            verify(clienteRepository, never()).existsByRut(any());
            verify(clienteRepository, never()).existsByEmailIgnoreCase(any());
        }

        @Test @DisplayName("RUT nuevo duplicado → excepción")
        void rutNuevoDuplicado() {
            Cliente existente = crear(1L, "11.111.111-1", "a@a.cl", true);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(clienteRepository.existsByRut("33.333.333-3")).thenReturn(true);
            assertThrows(ClienteDuplicadoException.class,
                    () -> clienteService.actualizar(1L, crearDTO("33.333.333-3", "a@a.cl", true)));
        }

        @Test @DisplayName("Email nuevo duplicado → excepción")
        void emailNuevoDuplicado() {
            Cliente existente = crear(1L, "11.111.111-1", "a@a.cl", true);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(clienteRepository.existsByEmailIgnoreCase("otro@x.cl")).thenReturn(true);
            assertThrows(ClienteDuplicadoException.class,
                    () -> clienteService.actualizar(1L, crearDTO("11.111.111-1", "otro@x.cl", true)));
        }

        @Test @DisplayName("No existe → RecursoNoEncontradoException")
        void noExiste() {
            when(clienteRepository.findById(404L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class,
                    () -> clienteService.actualizar(404L, crearDTO("X", "x@x.cl", true)));
        }

        @Test @DisplayName("activo=null conserva valor")
        void activoNullConserva() {
            Cliente existente = crear(1L, "11.111.111-1", "a@a.cl", true);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
            ClienteResponseDTO r = clienteService.actualizar(1L, crearDTO("11.111.111-1", "a@a.cl", null));
            assertTrue(r.getActivo());
        }
    }

    @Nested @DisplayName("desactivar()")
    class Desactivar {
        @Test @DisplayName("Existe → activo=false")
        void ok() {
            Cliente c = crear(1L, "11.111.111-1", "a@a.cl", true);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(c));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
            clienteService.desactivar(1L);
            assertFalse(c.getActivo());
        }
        @Test @DisplayName("No existe → excepción")
        void noExiste() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> clienteService.desactivar(99L));
        }
    }

    @Nested @DisplayName("reactivar()")
    class Reactivar {
        @Test @DisplayName("Existe → activo=true")
        void ok() {
            Cliente c = crear(1L, "11.111.111-1", "a@a.cl", false);
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(c));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
            assertTrue(clienteService.reactivar(1L).getActivo());
        }
        @Test @DisplayName("No existe → excepción")
        void noExiste() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(RecursoNoEncontradoException.class, () -> clienteService.reactivar(99L));
        }
    }

    // Helpers
    private Cliente crear(Long id, String rut, String email, Boolean activo) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setRut(rut);
        c.setNombre("Juan");
        c.setApellido("Perez");
        c.setEmail(email);
        c.setTelefono("+56912345678");
        c.setDireccion("Calle 1");
        c.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        c.setActivo(activo);
        return c;
    }

    private ClienteRequestDTO crearDTO(String rut, String email, Boolean activo) {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setRut(rut);
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setEmail(email);
        dto.setTelefono("+56912345678");
        dto.setDireccion("Calle 1");
        dto.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        dto.setActivo(activo);
        return dto;
    }
}
