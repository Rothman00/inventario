CREATE DATABASE IF NOT EXISTS bd_inventario;
USE bd_inventario;

-- inv_kardex --
CREATE TABLE inv_kardex (
    kar_id       INT AUTO_INCREMENT PRIMARY KEY,
    kar_producto INT NULL,
    kar_fecha    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    kar_actual   DECIMAL(15,2) NULL,
    kar_ingreso  DECIMAL(15,2) NULL,
    kar_salida   DECIMAL(15,2) NULL,
    kar_total    DECIMAL(15,2) NULL,
    kar_estado   VARCHAR(1) NULL
);

-- inv_unidad --
CREATE TABLE inv_unidad (
    uni_id          INT AUTO_INCREMENT PRIMARY KEY,
    uni_nombre      VARCHAR(50) NULL,
    uni_abreviatura VARCHAR(25) NULL,
    uni_estado      VARCHAR(1) NULL
);

-- inv_producto --
CREATE TABLE inv_producto (
    pro_id     INT AUTO_INCREMENT PRIMARY KEY,
    pro_nombre VARCHAR(30) NULL,
    pro_precio DECIMAL(15,2) NULL,
    pro_unidad INT NULL,
    pro_estado VARCHAR(1) NULL,
    FOREIGN KEY (pro_unidad) REFERENCES inv_unidad(uni_id)
);

-- v_inv_producto (vista) --
CREATE OR REPLACE VIEW v_inv_producto AS
SELECT
    p.pro_id,
    p.pro_nombre,
    p.pro_precio,
    p.pro_unidad,
    u.uni_nombre,
    u.uni_abreviatura,
    k.kar_fecha,
    k.kar_total
FROM inv_producto p
LEFT JOIN inv_unidad u ON p.pro_unidad = u.uni_id
LEFT JOIN inv_kardex k ON k.kar_producto = p.pro_id;

