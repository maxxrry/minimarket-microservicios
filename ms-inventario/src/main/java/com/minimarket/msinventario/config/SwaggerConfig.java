package com.minimarket.msinventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para el microservicio ms-inventario.
 * Documentación disponible en: http://localhost:8084/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Ms-Inventario - Gestión de Stock")
                        .version("1.0.0")
                        .description("""
                                Documentación de la API para el sistema de gestión de inventario del minimarket.

                                Endpoints disponibles para gestionar stock y movimientos, incluyendo:
                                - Control de existencias por producto
                                - Registro de movimientos de entrada y salida

                                **Nota:** Se comunica con ms-catalogo para validar productos.
                                """)
                        .contact(new Contact()
                                .name("Equipo MiniMarket")
                                .email("soporte@minimarket.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}