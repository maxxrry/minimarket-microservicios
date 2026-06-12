package com.minimarket.msreportes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para el microservicio ms-reportes.
 * Documentación disponible en: http://localhost:8090/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Ms-Reportes - Reportes Consolidados")
                        .version("1.0.0")
                        .description("""
                                Documentación de la API para el sistema de reportes del minimarket.

                                Endpoints disponibles para generar reportes consolidados, incluyendo:
                                - Reportes de ventas
                                - Reportes de inventario
                                - Reportes de clientes

                                **Nota:** Consume datos de los demás microservicios.
                                """)
                        .contact(new Contact()
                                .name("Equipo MiniMarket")
                                .email("soporte@minimarket.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}