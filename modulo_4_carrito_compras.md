# 🛒 MÓDULO: CARRITO DE COMPRAS
*Documento Hijo 4*

---

## 📋 Información del Módulo
- **Responsable**: Shopping Experience Team
- **Última Actualización**: 12 Agosto 2025
- **Estado**: ✅ Activo
- **Dependencias**: Módulo Gestión de Usuarios, Módulo Catálogo de Productos
- **Entidades**: 2

---

## 🎯 Propósito del Módulo
Gestión del carrito de compras con persistencia, reserva temporal de productos y control de stock. Cada usuario/empresa tiene un carrito único y permanente que gestiona el ciclo completo desde agregar productos hasta el proceso de checkout con reserva temporal.

---

## 🏛️ Entidades

### 1. CARRITO
**Propósito**: Carrito único y permanente por usuario/empresa

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_carrito` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_usuario` | BIGINT | - | ❌ | 🔗 FK | Usuario propietario (excluyente) |
| `id_perfil_empresa` | BIGINT | - | ❌ | 🔗 FK | Empresa propietaria (excluyente) |
| `estado_carrito` | ENUM | - | ✅ | - | ACTIVO, RESERVADO, EXPIRADO |
| `fecha_creacion` | TIMESTAMP | - | ✅ | - | Fecha de creación del carrito |
| `fecha_ultima_modificacion` | TIMESTAMP | - | ✅ | - | Última actualización |
| `fecha_reserva` | TIMESTAMP | - | ❌ | - | Cuándo se inició checkout |
| `fecha_expiracion_reserva` | TIMESTAMP | - | ❌ | - | Límite para completar pago (12hs) |

---

### 2. ITEM_CARRITO
**Propósito**: Items individuales dentro del carrito con control de estado

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_item_carrito` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_carrito` | BIGINT | - | ✅ | 🔗 FK | Carrito al que pertenece |
| `id_producto` | BIGINT | - | ✅ | 🔗 FK | Producto agregado |
| `cantidad` | INTEGER | - | ✅ | - | Cantidad del producto |
| `precio_unitario` | DECIMAL | 10,2 | ✅ | - | Precio al momento de agregar |
| `sub_total` | DECIMAL | 10,2 | ✅ | - | Cantidad × precio_unitario |
| `estado_item` | ENUM | - | ✅ | - | PENDIENTE, RESERVADO, EXPIRADO |
| `fecha_agregado` | TIMESTAMP | - | ✅ | - | Cuándo se agregó al carrito |
| `fecha_ultima_modificacion` | TIMESTAMP | - | ✅ | - | Última actualización del item |
| `fecha_expiracion_item` | TIMESTAMP | - | ❌ | - | Cleanup automático (30 días) |

---

## 🔗 Relaciones

### Principales
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| CARRITO | ITEM_CARRITO | Uno a Muchos | 1:N | Un carrito puede tener múltiples items |
| PRODUCTO | ITEM_CARRITO | Uno a Muchos | 1:N | Un producto puede estar en múltiples carritos |

### Hacia Módulo Gestión de Usuarios
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| USUARIO | CARRITO | Uno a Uno | 1:0..1 | Un usuario tiene máximo un carrito (excluyente) |
| PERFIL_EMPRESA | CARRITO | Uno a Uno | 1:0..1 | Una empresa tiene máximo un carrito (excluyente) |

### Hacia Módulo Catálogo de Productos
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| PRODUCTO | ITEM_CARRITO | Uno a Muchos | 1:N | Un producto puede estar en múltiples carritos |

---

## ⚙️ Configuraciones

### Índices Críticos
```sql
-- Carrito - consultas por propietario
CREATE UNIQUE INDEX idx_carrito_usuario ON CARRITO(id_usuario) WHERE id_usuario IS NOT NULL;
CREATE UNIQUE INDEX idx_carrito_empresa ON CARRITO(id_perfil_empresa) WHERE id_perfil_empresa IS NOT NULL;
CREATE INDEX idx_carrito_estado ON CARRITO(estado_carrito, fecha_expiracion_reserva);

-- Items - consultas frecuentes
CREATE INDEX idx_item_carrito ON ITEM_CARRITO(id_carrito, estado_item);
CREATE INDEX idx_item_producto ON ITEM_CARRITO(id_producto);
CREATE UNIQUE INDEX idx_item_unico ON ITEM_CARRITO(id_carrito, id_producto);

-- Cleanup y expiración
CREATE INDEX idx_carrito_expiracion ON CARRITO(fecha_expiracion_reserva) WHERE estado_carrito = 'RESERVADO';
CREATE INDEX idx_item_expiracion ON ITEM_CARRITO(fecha_expiracion_item) WHERE fecha_expiracion_item IS NOT NULL;
```

### Constraints de Integridad
```sql
-- Propietario excluyente: usuario O empresa, no ambos ni ninguno
ALTER TABLE CARRITO ADD CONSTRAINT chk_carrito_propietario
CHECK ((id_usuario IS NOT NULL AND id_perfil_empresa IS NULL) OR 
       (id_usuario IS NULL AND id_perfil_empresa IS NOT NULL));

-- Un solo carrito por usuario/empresa
ALTER TABLE CARRITO ADD CONSTRAINT uk_carrito_usuario 
UNIQUE(id_usuario) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE CARRITO ADD CONSTRAINT uk_carrito_empresa 
UNIQUE(id_perfil_empresa) DEFERRABLE INITIALLY DEFERRED;

-- Cantidad debe ser positiva
ALTER TABLE ITEM_CARRITO ADD CONSTRAINT chk_cantidad_positiva
CHECK (cantidad > 0);

-- Precios deben ser positivos
ALTER TABLE ITEM_CARRITO ADD CONSTRAINT chk_precio_positivo
CHECK (precio_unitario > 0 AND sub_total > 0);

-- Un producto por carrito (no duplicados)
ALTER TABLE ITEM_CARRITO ADD CONSTRAINT uk_item_producto
UNIQUE(id_carrito, id_producto);

-- Subtotal debe coincidir con cálculo
ALTER TABLE ITEM_CARRITO ADD CONSTRAINT chk_subtotal_correcto
CHECK (sub_total = cantidad * precio_unitario);
```

### Triggers Automáticos
```sql
-- Trigger: Gestión automática de reservas
CREATE TRIGGER gestionar_reserva_carrito
BEFORE UPDATE ON CARRITO
FOR EACH ROW
BEGIN
    -- Al pasar a RESERVADO, establecer fechas
    IF NEW.estado_carrito = 'RESERVADO' AND OLD.estado_carrito = 'ACTIVO' THEN
        SET NEW.fecha_reserva = NOW();
        SET NEW.fecha_expiracion_reserva = DATE_ADD(NOW(), INTERVAL 12 HOUR);
    END IF;
    
    -- Al volver a ACTIVO, limpiar fechas de reserva
    IF NEW.estado_carrito = 'ACTIVO' AND OLD.estado_carrito IN ('RESERVADO', 'EXPIRADO') THEN
        SET NEW.fecha_reserva = NULL;
        SET NEW.fecha_expiracion_reserva = NULL;
    END IF;
    
    SET NEW.fecha_ultima_modificacion = NOW();
END;

-- Trigger: Sincronizar estado de items con carrito
CREATE TRIGGER sincronizar_estado_items
AFTER UPDATE ON CARRITO
FOR EACH ROW
BEGIN
    -- Si carrito pasa a RESERVADO, reservar todos los items
    IF NEW.estado_carrito = 'RESERVADO' AND OLD.estado_carrito = 'ACTIVO' THEN
        UPDATE ITEM_CARRITO 
        SET estado_item = 'RESERVADO', fecha_ultima_modificacion = NOW()
        WHERE id_carrito = NEW.id_carrito AND estado_item = 'PENDIENTE';
    END IF;
    
    -- Si carrito vuelve a ACTIVO, liberar items reservados
    IF NEW.estado_carrito = 'ACTIVO' AND OLD.estado_carrito IN ('RESERVADO', 'EXPIRADO') THEN
        UPDATE ITEM_CARRITO 
        SET estado_item = 'PENDIENTE', fecha_ultima_modificacion = NOW()
        WHERE id_carrito = NEW.id_carrito AND estado_item = 'RESERVADO';
    END IF;
END;

-- Trigger: Calcular subtotal automáticamente
CREATE TRIGGER calcular_subtotal_item
BEFORE INSERT OR UPDATE ON ITEM_CARRITO
FOR EACH ROW
BEGIN
    SET NEW.sub_total = NEW.cantidad * NEW.precio_unitario;
    SET NEW.fecha_ultima_modificacion = NOW();
    
    -- Establecer fecha de expiración para cleanup (30 días)
    IF NEW.fecha_expiracion_item IS NULL THEN
        SET NEW.fecha_expiracion_item = DATE_ADD(NOW(), INTERVAL 30 DAY);
    END IF;
END;

-- Trigger: Actualizar carrito al modificar items
CREATE TRIGGER actualizar_carrito_modificacion
AFTER INSERT OR UPDATE OR DELETE ON ITEM_CARRITO
FOR EACH ROW
BEGIN
    -- Actualizar fecha de modificación del carrito
    UPDATE CARRITO 
    SET fecha_ultima_modificacion = NOW()
    WHERE id_carrito = COALESCE(NEW.id_carrito, OLD.id_carrito);
END;
```

### Jobs de Limpieza Automática
```sql
-- Job 1: Liberar carritos con reserva expirada (ejecutar cada 15 minutos)
UPDATE CARRITO 
SET estado_carrito = 'ACTIVO', 
    fecha_reserva = NULL, 
    fecha_expiracion_reserva = NULL,
    fecha_ultima_modificacion = NOW()
WHERE estado_carrito = 'RESERVADO' 
  AND fecha_expiracion_reserva < NOW();

-- Job 2: Limpiar items expirados (ejecutar diariamente)
DELETE FROM ITEM_CARRITO 
WHERE fecha_expiracion_item < NOW() 
  AND estado_item = 'PENDIENTE';

-- Job 3: Crear carritos para usuarios nuevos (ejecutar al registrarse)
INSERT INTO CARRITO (id_usuario, estado_carrito, fecha_creacion, fecha_ultima_modificacion)
SELECT id, 'ACTIVO', NOW(), NOW()
FROM USUARIO 
WHERE id NOT IN (SELECT id_usuario FROM CARRITO WHERE id_usuario IS NOT NULL);
```

---

## 📋 Reglas de Negocio

### Gestión de Carrito
1. ✅ **Carrito único**: Un solo carrito por usuario/empresa, permanente
2. ✅ **Creación automática**: Se crea al registrarse el usuario/empresa
3. ✅ **Sistema excluyente**: Carrito pertenece a usuario O empresa, nunca ambos
4. ✅ **Estados sincronizados**: Estado del carrito se propaga a todos sus items

### Items y Productos
5. ✅ **Sin duplicados**: Un producto máximo una vez por carrito (se actualiza cantidad)
6. ✅ **Precio congelado**: Se guarda precio al momento de agregar
7. ✅ **Cantidad positiva**: No se permiten cantidades cero o negativas
8. ✅ **Cálculo automático**: Subtotal se calcula automáticamente

### Reserva y Checkout
9. ✅ **Reserva temporal**: 12 horas máximo para completar pago
10. ✅ **Stock bloqueado**: Durante reserva, stock no disponible para otros
11. ✅ **Liberación automática**: Reserva expira automáticamente
12. ✅ **Estado consistente**: Items y carrito mantienen estado sincronizado

### Expiración y Limpieza
13. ✅ **Cleanup automático**: Items sin actividad se eliminan en 30 días
14. ✅ **Notificación previa**: Avisar antes de eliminar items (opcional)
15. ✅ **Preservar carrito**: Carrito nunca se elimina, solo se vacía
16. ✅ **Logs de actividad**: Registrar cambios para auditoría

---

## 🔄 Flujos de Trabajo

### 1. Creación y Gestión de Carrito
```
1. Usuario/empresa se registra
2. Sistema crea carrito automáticamente (estado: ACTIVO)
3. Carrito permanece durante toda la vida del usuario
4. Items se agregan/quitan sin afectar existencia del carrito
5. Cleanup automático solo elimina items expirados
```

### 2. Agregar Producto al Carrito
```
1. Usuario selecciona producto y cantidad
2. Sistema valida stock disponible
3. Si producto ya existe: actualiza cantidad
4. Si producto nuevo: crea nuevo item
5. Guarda precio actual del producto
6. Calcula subtotal automáticamente
7. Establece fecha de expiración (30 días)
8. Actualiza fecha modificación del carrito
```

### 3. Proceso de Checkout y Reserva
```
1. Usuario inicia checkout
2. Sistema valida stock de todos los items
3. Carrito pasa a estado RESERVADO
4. Todos los items pasan a RESERVADO
5. Stock se "bloquea" temporalmente
6. Usuario tiene 12hs para completar pago
7. Si paga: items se convierten en order_items
8. Si no paga: reserva expira automáticamente
```

### 4. Expiración de Reserva
```
1. Job automático revisa cada 15 minutos
2. Identifica carritos con reserva expirada
3. Cambia estado carrito a ACTIVO
4. Cambia estado items a PENDIENTE
5. Libera stock bloqueado
6. Notifica usuario (opcional)
7. Registra evento en logs
```

### 5. Limpieza de Items Antiguos
```
1. Job diario identifica items expirados (30 días)
2. Notifica usuario antes de eliminar (opcional)
3. Elimina items sin actividad reciente
4. Mantiene carrito activo aunque esté vacío
5. Actualiza estadísticas de uso
```

---

## 🧪 Casos de Prueba Críticos

### Gestión Básica
- [ ] Crear usuario automáticamente crea carrito (debe funcionar)
- [ ] Agregar producto duplicado actualiza cantidad (debe sumar)
- [ ] Agregar producto sin stock (debe fallar)
- [ ] Modificar cantidad a cero (debe eliminar item)

### Reserva y Checkout
- [ ] Iniciar checkout reserva carrito y items (debe cambiar estados)
- [ ] Reserva expira automáticamente después de 12hs (debe liberar)
- [ ] Stock bloqueado durante reserva no disponible (debe fallar para otros)
- [ ] Pago exitoso convierte items en orden (debe vaciar carrito)

### Sistema Excluyente
- [ ] Usuario y empresa no pueden compartir carrito (debe fallar)
- [ ] Carrito sin propietario (debe fallar)
- [ ] Múltiples carritos por usuario (debe fallar)

### Limpieza Automática
- [ ] Items antiguos se eliminan automáticamente (debe limpiar)
- [ ] Carrito permanece aunque esté vacío (debe mantener)
- [ ] Cleanup respeta items reservados (no debe eliminar)

### Integridad de Datos
- [ ] Subtotal calculado incorrectamente (debe fallar)
- [ ] Precio negativo (debe fallar)
- [ ] Producto inexistente en carrito (debe fallar)
- [ ] Sincronización estado carrito-items (debe mantener consistencia)

---

## 🚨 Consideraciones de Seguridad

### Control de Acceso
- **Propietario único**: Solo el dueño puede modificar su carrito
- **Validación de stock**: Verificar disponibilidad antes de reservar
- **Límites de cantidad**: Prevenir cantidades excesivas por producto

### Integridad de Precios
- **Precio congelado**: Mantener precio al momento de agregar
- **Validación**: Verificar coherencia entre precio y producto
- **Auditoría**: Log de cambios de precios en items

### Performance
- **Índices optimizados**: Consultas rápidas por usuario/producto
- **Cleanup eficiente**: Jobs de limpieza sin impacto en performance
- **Cache**: Carritos activos en memoria para acceso rápido

### Monitoreo
- **Reservas expiradas**: Alertar sobre problemas de checkout
- **Items abandonados**: Métricas de conversión carrito-compra
- **Stock bloqueado**: Monitorear bloqueos prolongados

---

## 📊 Métricas y KPIs

### Comportamiento del Usuario
- Tiempo promedio items en carrito antes de compra
- Tasa de abandono por cantidad de items
- Productos más agregados vs más comprados
- Tiempo promedio de checkout

### Performance del Sistema
- Tiempo respuesta agregar/quitar items
- Eficiencia jobs de cleanup
- Uso de memoria por carritos activos
- Precisión del sistema de reservas

### Conversión
- Tasa carrito → checkout → compra
- Impacto de expiración de reservas
- Productos más abandonados en carrito
- Efectividad de notificaciones de expiración

---

*📝 Nota: Este módulo es crítico para la experiencia de compra. Monitorear performance y tiempos de respuesta constantemente.*
