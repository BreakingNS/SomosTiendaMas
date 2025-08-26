# 🛍️ MÓDULO: CATÁLOGO DE PRODUCTOS
*Documento Hijo 3*

---

## 📋 Información del Módulo
- **Responsable**: Product Management Team
- **Última Actualización**: 12 Agosto 2025
- **Estado**: ✅ Activo
- **Dependencias**: Módulo Gestión de Usuarios
- **Entidades**: 7

---

## 🎯 Propósito del Módulo
Gestión completa del catálogo de productos, incluyendo categorización, atributos dinámicos, imágenes, control de stock, moderación de contenido y trazabilidad de precios. Solo perfiles empresariales pueden crear y vender productos.

---

## 🏛️ Entidades

### 1. CATEGORIA
**Propósito**: Clasificación principal de productos

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_categoria` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `nombre` | VARCHAR | 100 | ✅ | 🔐 UK | Nombre único de categoría |
| `descripcion` | TEXT | 500 | ❌ | - | Descripción de la categoría |
| `activa` | BOOLEAN | - | ✅ | - | Estado de la categoría |
| `fecha_creacion` | TIMESTAMP | - | ✅ | - | Fecha de creación |

---

### 2. SUBCATEGORIA
**Propósito**: Clasificación específica dentro de categorías

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_subcategoria` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_categoria` | BIGINT | - | ✅ | 🔗 FK | Categoría padre |
| `nombre` | VARCHAR | 100 | ✅ | - | Nombre de subcategoría |
| `descripcion` | TEXT | 500 | ❌ | - | Descripción de la subcategoría |
| `activa` | BOOLEAN | - | ✅ | - | Estado de la subcategoría |

---

### 3. MARCA
**Propósito**: Marcas de productos disponibles

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_marca` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `nombre` | VARCHAR | 100 | ✅ | 🔐 UK | Nombre único de marca |
| `descripcion` | TEXT | 500 | ❌ | - | Descripción de la marca |
| `logo_url` | VARCHAR | 500 | ❌ | - | URL del logo de la marca |
| `activa` | BOOLEAN | - | ✅ | - | Estado de la marca |

---

### 4. PRODUCTO
**Propósito**: Catálogo principal de productos del sistema

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_producto` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `nombre` | VARCHAR | 200 | ✅ | - | Nombre del producto |
| `descripcion` | TEXT | 2000 | ✅ | - | Descripción detallada |
| `precio_actual` | DECIMAL | 10,2 | ✅ | - | Precio vigente |
| `stock` | INTEGER | - | ✅ | - | Cantidad disponible |
| `stock_minimo` | INTEGER | - | ✅ | - | Stock mínimo requerido |
| `esta_disponible` | BOOLEAN | - | ✅ | - | Disponibilidad para venta |
| `fecha_creacion` | TIMESTAMP | - | ✅ | - | Fecha de creación |
| `estado_producto` | ENUM | - | ✅ | - | BORRADOR, PENDIENTE, APROBADO, RECHAZADO, SUSPENDIDO, INACTIVO |
| `estado_moderacion` | ENUM | - | ✅ | - | SIN_REVISAR, EN_REVISION, APROBADO, RECHAZADO, REQUIERE_CAMBIOS |
| `fecha_aprobacion` | TIMESTAMP | - | ❌ | - | Fecha de aprobación |
| `motivo_rechazo` | TEXT | 500 | ❌ | - | Razón del rechazo |
| `comision_plataforma` | DECIMAL | 5,4 | ✅ | - | Comisión de la plataforma (%) |
| `id_subcategoria` | BIGINT | - | ✅ | 🔗 FK | Subcategoría del producto |
| `id_marca` | BIGINT | - | ✅ | 🔗 FK | Marca del producto |
| `id_perfil_empresa_vendedor` | BIGINT | - | ✅ | 🔗 FK | Empresa vendedora |
| `id_moderador` | BIGINT | - | ❌ | 🔗 FK | Usuario moderador |

---

### 5. ATRIBUTO_VALOR
**Propósito**: Atributos dinámicos de productos (talla, color, etc.)

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_atributo` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_producto` | BIGINT | - | ✅ | 🔗 FK | Producto al que pertenece |
| `nombre` | VARCHAR | 50 | ✅ | - | Nombre del atributo (color, talla) |
| `valor` | VARCHAR | 100 | ✅ | - | Valor del atributo (rojo, XL) |

---

### 6. PRODUCTO_IMAGEN
**Propósito**: Galería de imágenes por producto

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_imagen` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_producto` | BIGINT | - | ✅ | 🔗 FK | Producto al que pertenece |
| `url_imagen` | VARCHAR | 500 | ✅ | - | URL de la imagen |
| `es_portada` | BOOLEAN | - | ✅ | - | Si es imagen principal |
| `orden` | INTEGER | - | ✅ | - | Orden de visualización |

---

### 7. PRECIO_HISTORICO
**Propósito**: Historial de cambios de precios con trazabilidad

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_precio_historico` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_producto` | BIGINT | - | ✅ | 🔗 FK | Producto afectado |
| `precio_anterior` | DECIMAL | 10,2 | ✅ | - | Precio previo |
| `precio` | DECIMAL | 10,2 | ✅ | - | Precio nuevo |
| `fecha_inicio` | TIMESTAMP | - | ✅ | - | Inicio de vigencia |
| `fecha_fin` | TIMESTAMP | - | ❌ | - | Fin de vigencia (NULL = actual) |
| `motivo` | ENUM | - | ✅ | - | DESCUENTO, INFLACION, PROMOCION |
| `activo` | BOOLEAN | - | ✅ | - | Estado del registro |
| `id_usuario` | BIGINT | - | ❌ | 🔗 FK | Admin que modificó (excluyente) |
| `id_perfil_empresa` | BIGINT | - | ❌ | 🔗 FK | Empresa que modificó (excluyente) |

---

## 🔗 Relaciones

### Principales
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| CATEGORIA | SUBCATEGORIA | Uno a Muchos | 1:N | Una categoría tiene múltiples subcategorías |
| SUBCATEGORIA | PRODUCTO | Uno a Muchos | 1:N | Una subcategoría tiene múltiples productos |
| MARCA | PRODUCTO | Uno a Muchos | 1:N | Una marca tiene múltiples productos |
| PRODUCTO | ATRIBUTO_VALOR | Uno a Muchos | 1:N | Un producto tiene múltiples atributos |
| PRODUCTO | PRODUCTO_IMAGEN | Uno a Muchos | 1:N | Un producto tiene múltiples imágenes |
| PRODUCTO | PRECIO_HISTORICO | Uno a Muchos | 1:N | Un producto tiene historial de precios |

### Hacia Módulo Gestión de Usuarios
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| PERFIL_EMPRESA | PRODUCTO | Uno a Muchos | 1:N | Una empresa puede vender múltiples productos |
| USUARIO | PRODUCTO | Uno a Muchos | 1:N | Un moderador puede moderar múltiples productos |
| USUARIO | PRECIO_HISTORICO | Uno a Muchos | 1:N | Admin modifica precios (excluyente) |
| PERFIL_EMPRESA | PRECIO_HISTORICO | Uno a Muchos | 1:N | Empresa modifica sus precios (excluyente) |

---

## ⚙️ Configuraciones

### Índices Críticos
```sql
-- Categorías y subcategorías
CREATE INDEX idx_categoria_activa ON CATEGORIA(activa);
CREATE INDEX idx_subcategoria_categoria ON SUBCATEGORIA(id_categoria, activa);

-- Marcas
CREATE INDEX idx_marca_activa ON MARCA(activa);
CREATE INDEX idx_marca_nombre ON MARCA(nombre);

-- Productos - búsquedas frecuentes
CREATE INDEX idx_producto_subcategoria ON PRODUCTO(id_subcategoria, estado_producto);
CREATE INDEX idx_producto_marca ON PRODUCTO(id_marca, esta_disponible);
CREATE INDEX idx_producto_vendedor ON PRODUCTO(id_perfil_empresa_vendedor);
CREATE INDEX idx_producto_estado ON PRODUCTO(estado_producto, estado_moderacion);
CREATE INDEX idx_producto_precio ON PRODUCTO(precio_actual, esta_disponible);
CREATE INDEX idx_producto_stock ON PRODUCTO(stock, stock_minimo) WHERE esta_disponible = true;
CREATE INDEX idx_producto_busqueda ON PRODUCTO(nombre, descripcion) WHERE estado_producto = 'APROBADO';

-- Atributos - filtros de productos
CREATE INDEX idx_atributo_producto ON ATRIBUTO_VALOR(id_producto);
CREATE INDEX idx_atributo_filtro ON ATRIBUTO_VALOR(nombre, valor);

-- Imágenes
CREATE INDEX idx_imagen_producto ON PRODUCTO_IMAGEN(id_producto, orden);
CREATE INDEX idx_imagen_portada ON PRODUCTO_IMAGEN(id_producto) WHERE es_portada = true;

-- Precios históricos - análisis y reportes
CREATE INDEX idx_precio_producto ON PRECIO_HISTORICO(id_producto, fecha_inicio);
CREATE INDEX idx_precio_activo ON PRECIO_HISTORICO(id_producto) WHERE fecha_fin IS NULL;
CREATE INDEX idx_precio_modificador_usuario ON PRECIO_HISTORICO(id_usuario) WHERE id_usuario IS NOT NULL;
CREATE INDEX idx_precio_modificador_empresa ON PRECIO_HISTORICO(id_perfil_empresa) WHERE id_perfil_empresa IS NOT NULL;
```

### Constraints de Integridad
```sql
-- Subcategoría debe tener categoría activa
ALTER TABLE SUBCATEGORIA ADD CONSTRAINT chk_categoria_activa
CHECK (id_categoria IN (SELECT id_categoria FROM CATEGORIA WHERE activa = true));

-- Producto debe tener subcategoría y marca activas
ALTER TABLE PRODUCTO ADD CONSTRAINT chk_subcategoria_activa
CHECK (id_subcategoria IN (SELECT id_subcategoria FROM SUBCATEGORIA WHERE activa = true));

ALTER TABLE PRODUCTO ADD CONSTRAINT chk_marca_activa
CHECK (id_marca IN (SELECT id_marca FROM MARCA WHERE activa = true));

-- Precios deben ser positivos
ALTER TABLE PRODUCTO ADD CONSTRAINT chk_precio_positivo
CHECK (precio_actual > 0);

ALTER TABLE PRECIO_HISTORICO ADD CONSTRAINT chk_precios_positivos
CHECK (precio_anterior >= 0 AND precio > 0);

-- Stock no puede ser negativo
ALTER TABLE PRODUCTO ADD CONSTRAINT chk_stock_positivo
CHECK (stock >= 0 AND stock_minimo >= 0);

-- Comisión debe estar entre 0 y 100%
ALTER TABLE PRODUCTO ADD CONSTRAINT chk_comision_valida
CHECK (comision_plataforma >= 0 AND comision_plataforma <= 1);

-- Solo una imagen portada por producto
CREATE UNIQUE INDEX idx_producto_portada_unica
ON PRODUCTO_IMAGEN(id_producto) WHERE es_portada = true;

-- Modificador de precio excluyente
ALTER TABLE PRECIO_HISTORICO ADD CONSTRAINT chk_precio_modificador
CHECK ((id_usuario IS NOT NULL AND id_perfil_empresa IS NULL) OR 
       (id_usuario IS NULL AND id_perfil_empresa IS NOT NULL));

-- Fechas de precio coherentes
ALTER TABLE PRECIO_HISTORICO ADD CONSTRAINT chk_fechas_precio
CHECK (fecha_fin IS NULL OR fecha_fin > fecha_inicio);
```

### Triggers de Gestión Automática
```sql
-- Actualizar fecha_fin del precio anterior al crear uno nuevo
CREATE TRIGGER update_precio_anterior
BEFORE INSERT ON PRECIO_HISTORICO
FOR EACH ROW
BEGIN
    UPDATE PRECIO_HISTORICO 
    SET fecha_fin = NEW.fecha_inicio
    WHERE id_producto = NEW.id_producto 
      AND fecha_fin IS NULL 
      AND id_precio_historico != NEW.id_precio_historico;
END;

-- Actualizar precio_actual del producto
CREATE TRIGGER update_precio_actual
AFTER INSERT ON PRECIO_HISTORICO
FOR EACH ROW
BEGIN
    IF NEW.fecha_fin IS NULL THEN
        UPDATE PRODUCTO 
        SET precio_actual = NEW.precio
        WHERE id_producto = NEW.id_producto;
    END IF;
END;

-- Validar stock mínimo y alertar
CREATE TRIGGER check_stock_minimo
AFTER UPDATE ON PRODUCTO
FOR EACH ROW
BEGIN
    IF NEW.stock <= NEW.stock_minimo AND OLD.stock > OLD.stock_minimo THEN
        -- Trigger para notificación de stock bajo
        INSERT INTO NOTIFICACION_STOCK_BAJO (id_producto, stock_actual, fecha)
        VALUES (NEW.id_producto, NEW.stock, NOW());
    END IF;
END;
```

---

## 📋 Reglas de Negocio

### Categorización
1. ✅ Solo categorías activas pueden tener subcategorías
2. ✅ Solo subcategorías activas pueden tener productos
3. ✅ Marcas inactivas no pueden usarse en productos nuevos
4. ✅ No se puede desactivar categoría/marca con productos activos

### Productos y Moderación
5. ✅ Solo perfiles empresa pueden crear productos
6. ✅ Productos deben tener al menos una imagen portada
7. ✅ Estado BORRADOR permite modificación libre
8. ✅ Estado PENDIENTE bloquea edición hasta moderación
9. ✅ Solo productos APROBADOS aparecen en búsquedas públicas
10. ✅ Productos RECHAZADOS requieren motivo obligatorio

### Stock y Disponibilidad
11. ✅ Stock no puede ser negativo
12. ✅ Productos con stock 0 se marcan como no disponibles automáticamente
13. ✅ Stock por debajo del mínimo genera alerta
14. ✅ Solo productos disponibles aparecen en catálogo público

### Precios e Histórico
15. ✅ Precios deben ser mayores a 0
16. ✅ Cambio de precio crea registro histórico automáticamente
17. ✅ Solo un precio activo (fecha_fin = NULL) por producto
18. ✅ Empresas solo pueden modificar precios de sus productos
19. ✅ Admins pueden modificar cualquier precio con motivo

### Imágenes y Multimedia
20. ✅ Solo una imagen portada por producto
21. ✅ Mínimo una imagen requerida para aprobar producto
22. ✅ Orden de imágenes debe ser secuencial
23. ✅ URLs de imágenes deben ser válidas y accesibles

---

## 🔄 Flujos de Trabajo

### 1. Creación de Producto
```
1. Empresa autenticada accede a crear producto
2. Selecciona categoría → subcategoría → marca
3. Completa información básica y descripción
4. Agrega atributos específicos (color, talla, etc.)
5. Sube imágenes (mínimo 1, máximo 10)
6. Establece precio y stock inicial
7. Producto queda en estado BORRADOR
8. Empresa puede pre-visualizar y editar
9. Al finalizar, envía a moderación (PENDIENTE)
```

### 2. Moderación de Productos
```
1. Moderador recibe notificación de producto PENDIENTE
2. Revisa información, imágenes y cumplimiento de políticas
3. Cambia estado a EN_REVISION
4. Si cumple: APROBADO + fecha_aprobacion
5. Si no cumple: RECHAZADO + motivo_rechazo
6. Si necesita ajustes: REQUIERE_CAMBIOS + comentarios
7. Notifica resultado a la empresa vendedora
```

### 3. Gestión de Stock
```
1. Sistema monitorea stock en tiempo real
2. Al vender: stock = stock - cantidad_vendida
3. Si stock <= stock_minimo: genera alerta
4. Si stock = 0: esta_disponible = false automáticamente
5. Empresa puede reponer stock manual o automáticamente
6. Al reponer: esta_disponible = true si stock > 0
```

### 4. Actualización de Precios
```
1. Empresa o admin solicita cambio de precio
2. Sistema valida permisos y precio válido
3. Crea nuevo registro en PRECIO_HISTORICO
4. Trigger actualiza fecha_fin del precio anterior
5. Trigger actualiza precio_actual del producto
6. Registra quien hizo el cambio y motivo
7. Notifica cambio si es promoción/descuento
```

---

## 🧪 Casos de Prueba Críticos

### Categorización
- [ ] Crear producto con subcategoría inactiva (debe fallar)
- [ ] Desactivar categoría con productos activos (debe fallar)
- [ ] Producto con marca inexistente (debe fallar)

### Moderación
- [ ] Producto en BORRADOR puede editarse (debe permitir)
- [ ] Producto PENDIENTE no puede editarse (debe bloquear)
- [ ] Moderador aprueba producto (debe cambiar estado y fecha)
- [ ] Rechazo sin motivo (debe fallar)

### Stock y Precios
- [ ] Venta reduce stock correctamente (debe actualizar)
- [ ] Stock negativo (debe fallar)
- [ ] Precio negativo o cero (debe fallar)
- [ ] Cambio de precio crea histórico (debe registrar)

### Imágenes
- [ ] Producto sin imagen portada (debe fallar moderación)
- [ ] Múltiples imágenes portada (debe fallar)
- [ ] URL de imagen inválida (debe fallar)

### Permisos
- [ ] Usuario común crea producto (debe fallar)
- [ ] Empresa modifica producto de otra (debe fallar)
- [ ] Admin modifica cualquier precio (debe permitir)

---

## 🚨 Consideraciones de Seguridad

### Validación de Contenido
- **Imágenes**: Validar formato, tamaño y contenido apropiado
- **Descripciones**: Filtrar contenido ofensivo o spam
- **Precios**: Validar rangos realistas por categoría

### Control de Acceso
- **Productos**: Solo dueño y moderadores pueden editar
- **Precios**: Log completo de cambios con responsable
- **Stock**: Validar permisos antes de modificar

### Auditoría
- **Moderación**: Registro completo de decisiones
- **Cambios críticos**: Log de precio, stock, estado
- **Acceso**: Monitorear consultas masivas o sospechosas

### Performance
- **Caché**: Productos populares en memoria
- **Índices**: Optimizar búsquedas por precio, categoría
- **Imágenes**: CDN para servir contenido multimedia

---

## 📊 Métricas y Reportes

### KPIs del Catálogo
- Productos por categoría/subcategoría
- Tiempo promedio de moderación
- Productos con stock bajo
- Variación de precios por período

### Análisis de Ventas
- Productos más vendidos por categoría
- Marcas con mejor performance
- Histórico de precios y tendencias
- Stock rotation por producto

---

*📝 Nota: Este módulo es crítico para la experiencia de usuario. Implementar cache agresivo y monitoreo de performance.*
