package com.minimarket.msventas.client;

import com.minimarket.msventas.client.dto.EmpleadoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-empleados", url = "${ms-empleados.url}")
public interface EmpleadoClient {

    @GetMapping("/api/empleados/{id}")
    EmpleadoDTO obtenerEmpleadoPorId(@PathVariable("id") Long id);
}
