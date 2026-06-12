package com.minimarket.mspromociones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para el microservicio ms-promociones.
 * Documentación disponible en: http://localhost:8089/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Ms-Promociones - Gestión de Promociones")
                        .version("1.0.0")
                        .description("""
                                Documentación de la API para el sistema de promociones del minimarket.

                                Endpoints disponibles para gestionar promociones y descuentos, incluyendo:
                                - Crear y gestionar promociones activas
                                - Aplicar descuentos a productos

                                **Nota:** Se comunica con ms-catalogo para aplicar descuentos a productos.
                                """)
                        .contact(new Contact()
                                .name("Equipo MiniMarket")
                                .email("soporte@minimarket.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}