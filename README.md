# Sistema de Gestión MiniMarket - Arquitectura de Microservicios

Proyecto desarrollado para la asignatura **DSY1103 - Desarrollo FullStack 1** 
(Duoc UC, 2026) como parte de la Evaluación Parcial 2.

## Integrantes

- Maxi Serey - GitHub: [@maxxrry](https://github.com/maxxrry)
- Vicente Muñoz - GitHub: [@Sk1xez](https://github.com/Sk1xez)
-Jose Taborga - GitHub:[]

## Descripción del proyecto

Sistema distribuido para la gestión integral de un minimarket de barrio, 
construido bajo una arquitectura de microservicios independientes que se 
comunican entre sí mediante APIs REST.

## Arquitectura

El sistema está compuesto por 10 microservicios:

| Microservicio | Puerto | Responsabilidad |
|---------------|--------|-----------------|
| ms-catalogo | 8081 | Gestión de productos |
| ms-categorias | 8082 | Categorías de productos |
| ms-proveedores | 8083 | Proveedores |
| ms-inventario | 8084 | Stock y movimientos |
| ms-clientes | 8085 | Clientes registrados |
| ms-empleados | 8086 | Empleados y cajeros |
| ms-ventas | 8087 | Registro de ventas |
| ms-pagos | 8088 | Métodos de pago |
| ms-promociones | 8089 | Descuentos y ofertas |
| ms-reportes | 8090 | Reportes consolidados |

## Stack tecnológico

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA + Hibernate**
- **MySQL 8** (vía Laragon)
- **Spring Cloud OpenFeign** (comunicación inter-servicios)
- **Spring Boot Starter Validation** (Bean Validation JSR 380)
- **Lombok**
- **SLF4J** (logging)
- **Maven**

## Patrón arquitectónico

Cada microservicio sigue el patrón **CSR (Controller-Service-Repository)** con 
separación clara de responsabilidades por capas.

## Cómo ejecutar

1. Levantar MySQL desde Laragon
2. Ejecutar los scripts SQL ubicados en cada microservicio (`/src/main/resources/sql`)
3. Abrir cada microservicio en IntelliJ IDEA
4. Ejecutar la clase principal `Application.java` de cada servicio
5. Probar endpoints con Postman (colección incluida en `/docs`)

## Estado del proyecto

🚧 En desarrollo - Evaluación Parcial 2 - Semana 1
