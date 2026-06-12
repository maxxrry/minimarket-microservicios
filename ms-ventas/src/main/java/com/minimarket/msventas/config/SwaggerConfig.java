package com.minimarket.msventas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para el microservicio ms-ventas.
 * Documentación disponible en: http://localhost:8087/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Ms-Ventas - Gestión de Ventas")
                        .version("1.0.0")
                        .description("""
                                Documentación de la API para el sistema de gestión de ventas del minimarket.

                                Endpoints disponibles para gestionar ventas, incluyendo:
                                - Registro de ventas
                                - Detalle de productos vendidos
                                - Integración con clientes y empleados

                                **Nota:** Se comunica con ms-catalogo, ms-clientes, ms-empleados y ms-pagos.
                                """)
                        .contact(new Contact()
                                .name("Equipo MiniMarket")
                                .email("soporte@minimarket.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}