-- ============================================================
-- Script de creacion de bases de datos para minimarket-microservicios
-- Ejecutar UNA VEZ en HeidiSQL/Laragon como usuario root
-- ============================================================
-- Idempotente: usa IF NOT EXISTS, se puede correr varias veces sin error.
-- Charset utf8mb4 para soportar emojis y acentos sin problemas.
-- ============================================================

CREATE DATABASE IF NOT EXISTS mm_catalogo
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_categorias
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_proveedores
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_inventario
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_clientes
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_empleados
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_ventas
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_pagos
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_promociones
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mm_reportes
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verificar que se crearon las 10:
SHOW DATABASES LIKE 'mm_%';
