package com.minimarket.msventas.client;

import com.minimarket.msventas.client.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign que consume el microservicio ms-catalogo.
 * Spring genera la implementación automáticamente al arrancar.
 */
@FeignClient(name = "ms-catalogo", url = "${ms-catalogo.url}")
public interface CatalogoClient {

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id);
}
