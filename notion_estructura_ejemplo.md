# 📋 DOCUMENTACIÓN BASE DE DATOS - SOMOSTIENDAMAS
*Documento Raíz*

---

## 🏗️ Arquitectura General

### Resumen del Sistema
Sistema de e-commerce modular que gestiona usuarios, productos, imágenes y precios históricos con arquitectura escalable.

### Módulos del Sistema
| Módulo | Descripción | Estado | Responsable |
|--------|-------------|--------|-------------|
| [👤 Gestión de Usuarios](#modulo-usuarios) | Autenticación, perfiles, roles | ✅ Activo | Backend Team |
| [🛍️ Gestión de Productos](#modulo-productos) | Catálogo, precios, imágenes | ✅ Activo | Product Team |

---

## 📊 Métricas Generales
- **Total Entidades**: 5
- **Total Relaciones**: 8
- **Complejidad**: Media
- **Última Actualización**: 11 Agosto 2025

---

## 🔗 Enlaces Rápidos
- [Base de Datos en Producción](link)
- [Diagramas ER](link)
- [Scripts de Migración](link)

---

# 👤 MÓDULO: GESTIÓN DE USUARIOS
*Documento Hijo 1*

---

## 📋 Información del Módulo
- **Responsable**: Backend Team
- **Última Actualización**: 11 Agosto 2025
- **Estado**: ✅ Activo
- **Dependencias**: Ninguna

---

## 🏛️ Entidades

### USUARIO
**Propósito**: Gestión de usuarios del sistema

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `email` | VARCHAR | 100 | ✅ | 🔐 UK | Email único del usuario |
| `nombre` | VARCHAR | 50 | ✅ | - | Nombre completo |
| `apellido` | VARCHAR | 50 | ✅ | - | Apellido del usuario |
| `password` | VARCHAR | 255 | ✅ | - | Contraseña encriptada (BCrypt) |
| `telefono` | VARCHAR | 20 | ❌ | - | Número de contacto |
| `activo` | BOOLEAN | - | ✅ | - | Estado del usuario |
| `rol` | ENUM | - | ✅ | - | USER, ADMIN, MODERATOR |
| `fecha_registro` | TIMESTAMP | - | ✅ | - | Fecha de creación |
| `ultimo_acceso` | TIMESTAMP | - | ❌ | - | Última sesión |

---

## 🔗 Relaciones Salientes
| Destino | Tipo | Cardinalidad | Descripción |
|---------|------|--------------|-------------|
| PRODUCTO | Uno a Muchos | 1:N | Un usuario puede crear múltiples productos |

---

## ⚙️ Configuraciones

### Restricciones
```sql
-- Email único y válido
UNIQUE(email)
CHECK(email LIKE '%@%.%')

-- Password mínimo 8 caracteres
CHECK(LENGTH(password) >= 8)

-- Teléfono formato válido
CHECK(telefono REGEXP '^[0-9+\-\s()]+$' OR telefono IS NULL)
```

### Índices
```sql
CREATE INDEX idx_usuario_email ON USUARIO(email);
CREATE INDEX idx_usuario_activo ON USUARIO(activo);
CREATE INDEX idx_usuario_rol ON USUARIO(rol);
```

---

## 📋 Reglas de Negocio
1. ✅ Email debe ser único en todo el sistema
2. ✅ Solo usuarios ACTIVOS pueden iniciar sesión
3. ✅ Password debe cumplir política de seguridad
4. ✅ Rol por defecto es USER al registrarse
5. ❌ No se permite eliminación física (soft delete)

---

## 🧪 Casos de Prueba
- [ ] Registro con email duplicado (debe fallar)
- [ ] Login con usuario inactivo (debe fallar)
- [ ] Cambio de rol por admin (debe permitir)
- [ ] Actualización de último acceso (debe registrar)

---

# 🛍️ MÓDULO: GESTIÓN DE PRODUCTOS
*Documento Hijo 2*

---

## 📋 Información del Módulo
- **Responsable**: Product Team
- **Última Actualización**: 11 Agosto 2025
- **Estado**: ✅ Activo
- **Dependencias**: Módulo Usuarios

---

## 🏛️ Entidades

### PRODUCTO
**Propósito**: Catálogo principal de productos

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `nombre` | VARCHAR | 100 | ✅ | - | Nombre del producto |
| `descripcion` | TEXT | 2000 | ❌ | - | Descripción detallada |
| `precio_actual` | DECIMAL | 10,2 | ✅ | - | Precio vigente |
| `stock` | INTEGER | - | ✅ | - | Cantidad disponible |
| `sku` | VARCHAR | 50 | ✅ | 🔐 UK | Código único del producto |
| `categoria_id` | BIGINT | - | ✅ | 🔗 FK | Referencia a categoría |
| `usuario_id` | BIGINT | - | ✅ | 🔗 FK | Usuario creador |
| `activo` | BOOLEAN | - | ✅ | - | Estado del producto |
| `fecha_creacion` | TIMESTAMP | - | ✅ | - | Fecha de creación |

### PRODUCTO_IMAGEN
**Propósito**: Galería de imágenes por producto

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `producto_id` | BIGINT | - | ✅ | 🔗 FK | Referencia al producto |
| `url_imagen` | VARCHAR | 500 | ✅ | - | URL de la imagen |
| `alt_text` | VARCHAR | 200 | ❌ | - | Texto alternativo |
| `es_principal` | BOOLEAN | - | ✅ | - | Imagen principal del producto |
| `orden` | INTEGER | - | ✅ | - | Orden de visualización |
| `activo` | BOOLEAN | - | ✅ | - | Estado de la imagen |

### PRECIO_HISTORICO
**Propósito**: Historial de cambios de precios

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `producto_id` | BIGINT | - | ✅ | 🔗 FK | Referencia al producto |
| `precio_anterior` | DECIMAL | 10,2 | ✅ | - | Precio previo |
| `precio_nuevo` | DECIMAL | 10,2 | ✅ | - | Precio actualizado |
| `motivo` | VARCHAR | 100 | ❌ | - | Razón del cambio |
| `fecha_cambio` | TIMESTAMP | - | ✅ | - | Momento del cambio |
| `usuario_id` | BIGINT | - | ✅ | 🔗 FK | Usuario que hizo el cambio |

---

## 🔗 Relaciones

### Entrantes
| Origen | Tipo | Cardinalidad | Descripción |
|--------|------|--------------|-------------|
| USUARIO | Muchos a Uno | N:1 | Usuario creador del producto |

### Salientes
| Destino | Tipo | Cardinalidad | Descripción |
|---------|------|--------------|-------------|
| PRODUCTO_IMAGEN | Uno a Muchos | 1:N | Un producto tiene múltiples imágenes |
| PRECIO_HISTORICO | Uno a Muchos | 1:N | Un producto tiene historial de precios |

---

## ⚙️ Configuraciones

### Restricciones
```sql
-- SKU único
UNIQUE(sku)

-- Precio mayor a 0
CHECK(precio_actual > 0)

-- Stock no negativo
CHECK(stock >= 0)

-- Solo una imagen principal por producto
UNIQUE(producto_id) WHERE es_principal = true
```

### Índices
```sql
CREATE INDEX idx_producto_usuario ON PRODUCTO(usuario_id);
CREATE INDEX idx_producto_sku ON PRODUCTO(sku);
CREATE INDEX idx_producto_activo ON PRODUCTO(activo);
CREATE INDEX idx_imagen_producto ON PRODUCTO_IMAGEN(producto_id);
CREATE INDEX idx_precio_historico_producto ON PRECIO_HISTORICO(producto_id);
```

---

## 📋 Reglas de Negocio
1. ✅ Todo producto debe tener al menos una imagen
2. ✅ Solo puede haber una imagen principal por producto
3. ✅ SKU debe ser único en todo el sistema
4. ✅ Cambios de precio se registran automáticamente
5. ✅ Stock no puede ser negativo
6. ❌ Solo usuarios activos pueden crear productos

---

## 🧪 Casos de Prueba
- [ ] Crear producto sin imagen principal (debe fallar)
- [ ] Actualizar precio (debe crear registro histórico)
- [ ] Búsqueda por SKU (debe ser rápida)
- [ ] Producto con stock 0 (debe mostrar "Agotado")

---

## 🔄 Flujos de Trabajo
1. **Creación de Producto**:
   - Validar usuario activo
   - Generar SKU único
   - Asignar imagen principal
   - Registrar precio inicial

2. **Actualización de Precio**:
   - Validar nuevo precio > 0
   - Crear registro histórico
   - Actualizar precio actual
   - Notificar cambios

---

*📝 Nota: Esta estructura modular permite escalar fácilmente agregando nuevos módulos como Pedidos, Pagos, etc.*
