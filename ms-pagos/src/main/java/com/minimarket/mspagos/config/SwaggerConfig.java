package com.minimarket.mspagos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para el microservicio ms-pagos.
 * Documentación disponible en: http://localhost:8088/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Ms-Pagos - Gestión de Pagos")
                        .version("1.0.0")
                        .description("""
                                Documentación de la API para el sistema de gestión de pagos del minimarket.

                                Endpoints disponibles para gestionar métodos y estados de pago, incluyendo:
                                - Registro de pagos por venta
                                - Diferentes métodos de pago (efectivo, tarjeta, etc.)

                                **Nota:** Se comunica con ms-ventas para registrar pagos.
                                """)
                        .contact(new Contact()
                                .name("Equipo MiniMarket")
                                .email("soporte@minimarket.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}