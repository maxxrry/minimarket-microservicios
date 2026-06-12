package com.minimarket.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del API Gateway para el sistema MiniMarket.
 *
 * Este gateway acting as punto de entrada único para todos los microservicios,
 * manejando el enrutamiento de solicitudes a los servicios correspondientes.
 *
 * Puertos de los microservicios:
 * - ms-catalogo: 8081
 * - ms-categorias: 8082
 * - ms-proveedores: 8083
 * - ms-inventario: 8084
 * - ms-clientes: 8085
 * - ms-empleados: 8086
 * - ms-ventas: 8087
 * - ms-pagos: 8088
 * - ms-promociones: 8089
 * - ms-reportes: 8090
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}