package com.minimarket.msventas.client;

import com.minimarket.msventas.client.dto.PromocionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-promociones", url = "${ms-promociones.url}")
public interface PromocionClient {

    @GetMapping("/api/promociones/producto/{productoId}")
    List<PromocionDTO> listarPorProducto(@PathVariable("productoId") Long productoId);

    @GetMapping("/api/promociones/categoria/{categoriaId}")
    List<PromocionDTO> listarPorCategoria(@PathVariable("categoriaId") Long categoriaId);
}
