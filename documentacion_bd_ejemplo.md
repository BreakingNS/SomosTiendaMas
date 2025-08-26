# Documentación Base de Datos - SomosTiendaMas

## 1. Resumen del Sistema
Sistema de e-commerce que permite la gestión de productos, usuarios, imágenes y precios históricos.

## 2. Entidades y Atributos

### 2.1 USUARIO
| Atributo | Tipo | Tamaño | Obligatorio | Clave | Descripción |
|----------|------|--------|-------------|-------|-------------|
| id | BIGINT | - | ✓ | PK | Identificador único del usuario |
| email | VARCHAR | 100 | ✓ | UK | Email del usuario |
| nombre | VARCHAR | 50 | ✓ | - | Nombre del usuario |
| password | VARCHAR | 255 | ✓ | - | Contraseña encriptada |
| activo | BOOLEAN | - | ✓ | - | Estado del usuario (activo/inactivo) |
| fecha_registro | TIMESTAMP | - | ✓ | - | Fecha de registro en el sistema |

### 2.2 PRODUCTO
| Atributo | Tipo | Tamaño | Obligatorio | Clave | Descripción |
|----------|------|--------|-------------|-------|-------------|
| id | BIGINT | - | ✓ | PK | Identificador único del producto |
| nombre | VARCHAR | 100 | ✓ | - | Nombre del producto |
| descripcion | TEXT | - | ✗ | - | Descripción detallada |
| precio_actual | DECIMAL | 10,2 | ✓ | - | Precio actual del producto |
| stock | INTEGER | - | ✓ | - | Cantidad disponible |
| activo | BOOLEAN | - | ✓ | - | Estado del producto |
| fecha_creacion | TIMESTAMP | - | ✓ | - | Fecha de creación |
| usuario_id | BIGINT | - | ✓ | FK | Referencia al usuario creador |

### 2.3 PRODUCTO_IMAGEN
| Atributo | Tipo | Tamaño | Obligatorio | Clave | Descripción |
|----------|------|--------|-------------|-------|-------------|
| id | BIGINT | - | ✓ | PK | Identificador único de la imagen |
| producto_id | BIGINT | - | ✓ | FK | Referencia al producto |
| url_imagen | VARCHAR | 255 | ✓ | - | URL de la imagen |
| es_principal | BOOLEAN | - | ✓ | - | Indica si es la imagen principal |
| orden | INTEGER | - | ✗ | - | Orden de visualización |

## 3. Relaciones

### 3.1 Relaciones Principales
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | FK |
|----------------|-----------------|------|--------------|-----|
| PRODUCTO | USUARIO | Muchos a Uno | N:1 | usuario_id |
| PRODUCTO_IMAGEN | PRODUCTO | Muchos a Uno | N:1 | producto_id |

### 3.2 Descripción de Relaciones
- **USUARIO → PRODUCTO**: Un usuario puede crear múltiples productos
- **PRODUCTO → PRODUCTO_IMAGEN**: Un producto puede tener múltiples imágenes

## 4. Restricciones e Índices

### 4.1 Restricciones
- `USUARIO.email` debe ser único
- `PRODUCTO_IMAGEN.es_principal` solo puede haber una imagen principal por producto
- `PRODUCTO.precio_actual` debe ser mayor a 0

### 4.2 Índices Recomendados
```sql
CREATE INDEX idx_producto_usuario ON PRODUCTO(usuario_id);
CREATE INDEX idx_producto_imagen_producto ON PRODUCTO_IMAGEN(producto_id);
CREATE INDEX idx_usuario_email ON USUARIO(email);
```

## 5. Reglas de Negocio
1. Un producto debe tener al menos una imagen
2. Solo usuarios activos pueden crear productos
3. Los precios no pueden ser negativos
4. Email debe ser válido y único por usuario

---
*Documentación generada el: 11 de agosto de 2025*
