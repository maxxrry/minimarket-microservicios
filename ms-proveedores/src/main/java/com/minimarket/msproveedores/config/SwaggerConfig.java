package com.minimarket.msproveedores.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para el microservicio ms-proveedores.
 * Documentación disponible en: http://localhost:8083/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Ms-Proveedores - Gestión de Proveedores")
                        .version("1.0.0")
                        .description("""
                                Documentación de la API para el sistema de gestión de proveedores del minimarket.

                                Endpoints disponibles para gestionar proveedores, incluyendo:
                                - Crear, listar, actualizar y eliminar proveedores
                                - Información de contacto y datos comerciales

                                **Nota:** Los proveedores se referencian desde ms-catalogo mediante IDs.
                                """)
                        .contact(new Contact()
                                .name("Equipo MiniMarket")
                                .email("soporte@minimarket.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}