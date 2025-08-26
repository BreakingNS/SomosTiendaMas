# 👥 MÓDULO: GESTIÓN DE USUARIOS
*Documento Hijo 2*

---

## 📋 Información del Módulo
- **Responsable**: User Management Team
- **Última Actualización**: 12 Agosto 2025
- **Estado**: ✅ Activo
- **Dependencias**: Módulo Autenticación
- **Entidades**: 4

---

## 🎯 Propósito del Módulo
Gestión completa de perfiles de usuarios (personas físicas y empresas), incluyendo datos personales, información fiscal, direcciones y teléfonos. Maneja la extensión de usuarios a perfiles empresariales con trazabilidad completa.

---

## 🏛️ Entidades

### 1. USUARIO
**Propósito**: Perfil base de todos los usuarios del sistema

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |

#### **Campos Críticos**
| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `username` | VARCHAR | 50 | ✅ | 🔐 UK | Nombre de usuario único |
| `email` | VARCHAR | 100 | ✅ | 🔐 UK | Email único del usuario |
| `password` | VARCHAR | 255 | ✅ | - | Contraseña encriptada |
| `activo` | BOOLEAN | - | ✅ | - | Estado del usuario |
| `email_verificado` | BOOLEAN | - | ✅ | - | Estado de verificación email |
| `fecha_registro` | TIMESTAMP | - | ✅ | - | Fecha de registro |
| `intentos_fallidos_login` | INTEGER | - | ✅ | - | Contador de intentos fallidos |
| `cuenta_bloqueada` | BOOLEAN | - | ✅ | - | Estado de bloqueo |
| `tipo_usuario` | ENUM | - | ✅ | - | PERSONA_FISICA, EMPRESA |
| `responsable_nombre` | VARCHAR | 100 | ✅ | - | Nombre del responsable |
| `responsable_apellido` | VARCHAR | 100 | ✅ | - | Apellido del responsable |
| `responsable_documento` | VARCHAR | 20 | ✅ | 🔐 UK | Documento único |
| `acepta_terminos` | BOOLEAN | - | ✅ | - | Aceptación términos y condiciones |
| `acepta_politica_priv` | BOOLEAN | - | ✅ | - | Aceptación política privacidad |

#### **Campos Importantes**
| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `fecha_verificacion_email` | TIMESTAMP | - | ❌ | - | Momento de verificación |
| `fecha_ultimo_acceso` | TIMESTAMP | - | ❌ | - | Último login exitoso |
| `fecha_ultima_modificacion` | TIMESTAMP | - | ✅ | - | Última actualización |
| `responsable_fecha_nacimiento` | DATE | - | ❌ | - | Fecha nacimiento responsable |
| `responsable_genero` | ENUM | - | ❌ | - | MASCULINO, FEMENINO, OTRO |
| `telefono_principal` | VARCHAR | 20 | ❌ | - | Teléfono principal |
| `direccion_principal` | TEXT | - | ❌ | - | Dirección principal |

#### **Campos Opcionales**
| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `recibir_promociones` | BOOLEAN | - | ❌ | - | Preferencia promociones |
| `recibir_newsletters` | BOOLEAN | - | ❌ | - | Preferencia newsletters |
| `notificaciones_email` | BOOLEAN | - | ❌ | - | Preferencia notif. email |
| `notificaciones_sms` | BOOLEAN | - | ❌ | - | Preferencia notif. SMS |
| `idioma` | VARCHAR | 5 | ❌ | - | Código idioma (es, en, pt) |
| `timezone` | VARCHAR | 50 | ❌ | - | Zona horaria usuario |

---

### 2. PERFIL_EMPRESA
**Propósito**: Extensión empresarial del usuario base (el usuario se convierte en responsable)

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_usuario` | BIGINT | - | ✅ | 🔗 FK | Usuario responsable |

#### **Campos Críticos**
| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `razon_social` | VARCHAR | 200 | ✅ | - | Razón social empresa |
| `cuit` | VARCHAR | 15 | ✅ | 🔐 UK | CUIT único |
| `condicion_iva` | ENUM | - | ✅ | - | RI, MONOTRIBUTO, EXENTO |
| `estado_aprobado` | ENUM | - | ✅ | - | PENDIENTE, APROBADO, RECHAZADO |
| `email_empresa` | VARCHAR | 100 | ✅ | - | Email corporativo |
| `telefono_empresa` | VARCHAR | 20 | ✅ | - | Teléfono corporativo |
| `direccion_fiscal` | TEXT | - | ✅ | - | Dirección fiscal |

#### **Campos Importantes**
| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `descripcion_empresa` | TEXT | 1000 | ❌ | - | Descripción del negocio |
| `sitio_web` | VARCHAR | 255 | ❌ | - | URL sitio web |
| `categoria_empresa` | ENUM | - | ❌ | - | RETAIL, MAYORISTA, FABRICANTE |
| `requiere_facturacion` | BOOLEAN | - | ✅ | - | Si requiere facturación automática |
| `limite_credito_ventas` | DECIMAL | 12,2 | ❌ | - | Límite de crédito |
| `fecha_creacion` | TIMESTAMP | - | ✅ | - | Fecha creación perfil |
| `fecha_ultima_modificacion` | TIMESTAMP | - | ✅ | - | Última actualización |

#### **Campos Opcionales**
| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `logo_url` | VARCHAR | 500 | ❌ | - | URL del logo |
| `color_corporativo` | VARCHAR | 7 | ❌ | - | Color hex (#FFFFFF) |
| `descripcion_corta` | VARCHAR | 200 | ❌ | - | Descripción breve |
| `horario_atencion` | VARCHAR | 100 | ❌ | - | Horarios de atención |
| `dias_laborales` | VARCHAR | 50 | ❌ | - | Días que trabaja |
| `tiempo_procesamiento_pedidos` | INTEGER | - | ❌ | - | Días procesamiento |

---

### 3. DIRECCION
**Propósito**: Direcciones de usuarios y empresas (excluyentes)

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_direccion` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_usuario` | BIGINT | - | ❌ | 🔗 FK | Usuario (excluyente con empresa) |
| `id_perfil_empresa` | BIGINT | - | ❌ | 🔗 FK | Empresa (excluyente con usuario) |
| `tipo` | ENUM | - | ✅ | - | PERSONAL, FISCAL, ENVIO, FACTURACION |
| `calle` | VARCHAR | 200 | ✅ | - | Nombre de la calle |
| `numero` | VARCHAR | 10 | ✅ | - | Número de puerta |
| `piso` | VARCHAR | 10 | ❌ | - | Piso (opcional) |
| `departamento` | VARCHAR | 10 | ❌ | - | Departamento/oficina |
| `ciudad` | VARCHAR | 100 | ❌ | - | Ciudad |
| `provincia` | VARCHAR | 100 | ❌ | - | Provincia/estado |
| `codigo_postal` | VARCHAR | 10 | ✅ | - | Código postal |
| `pais` | VARCHAR | 100 | ✅ | - | País |
| `referencia` | TEXT | - | ❌ | - | Referencias adicionales |
| `activa` | BOOLEAN | - | ✅ | - | Estado de la dirección |
| `es_principal` | BOOLEAN | - | ✅ | - | Si es dirección principal |
| `es_copia_de` | BIGINT | - | ❌ | 🔗 FK | Referencia a dirección copiada |

---

### 4. TELEFONO
**Propósito**: Teléfonos de usuarios y empresas (excluyentes)

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id_telefono` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_usuario` | BIGINT | - | ❌ | 🔗 FK | Usuario (excluyente con empresa) |
| `id_perfil_empresa` | BIGINT | - | ❌ | 🔗 FK | Empresa (excluyente con usuario) |
| `tipo` | ENUM | - | ✅ | - | PRINCIPAL, SECUNDARIO, EMPRESA, WHATSAPP |
| `numero` | VARCHAR | 20 | ✅ | - | Número telefónico |
| `caracteristica` | VARCHAR | 10 | ✅ | - | Código de área |
| `activo` | BOOLEAN | - | ✅ | - | Estado del teléfono |
| `verificado` | BOOLEAN | - | ✅ | - | Si fue verificado |
| `es_copia_de` | BIGINT | - | ❌ | 🔗 FK | Referencia a teléfono copiado |

---

## 🔗 Relaciones

### Principales
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| USUARIO | PERFIL_EMPRESA | Uno a Muchos | 1:N | Un usuario puede ser responsable de múltiples empresas |
| USUARIO | DIRECCION | Uno a Muchos | 1:N | Un usuario puede tener múltiples direcciones |
| USUARIO | TELEFONO | Uno a Muchos | 1:N | Un usuario puede tener múltiples teléfonos |
| PERFIL_EMPRESA | DIRECCION | Uno a Muchos | 1:N | Una empresa puede tener múltiples direcciones |
| PERFIL_EMPRESA | TELEFONO | Uno a Muchos | 1:N | Una empresa puede tener múltiples teléfonos |

### Auto-Referencias (Híbridas)
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| DIRECCION | DIRECCION | Uno a Muchos | 1:N | Una dirección puede ser copiada por múltiples |
| TELEFONO | TELEFONO | Uno a Muchos | 1:N | Un teléfono puede ser copiado por múltiples |

### Hacia Módulo Autenticación
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| REFRESH_TOKEN | USUARIO | Muchos a Uno | N:1 | Tokens de refresh por usuario |
| TOKEN_ISSUED | USUARIO | Muchos a Uno | N:1 | Tokens JWT emitidos por usuario |
| ACTIVE_SESSIONS | USUARIO | Muchos a Uno | N:1 | Sesiones activas por usuario |
| LOGIN_FAILED_ATTEMPTS | USUARIO | Muchos a Uno | N:1 | Intentos fallidos por usuario |
| PASSWORD_RESET_TOKENS | USUARIO | Muchos a Uno | N:1 | Tokens de reset por usuario |

---

## ⚙️ Configuraciones

### Índices Críticos
```sql
-- Usuarios - consultas frecuentes
CREATE INDEX idx_usuario_email ON USUARIO(email);
CREATE INDEX idx_usuario_username ON USUARIO(username);
CREATE INDEX idx_usuario_documento ON USUARIO(responsable_documento);
CREATE INDEX idx_usuario_activo ON USUARIO(activo, email_verificado);

-- Perfil empresa - búsquedas comerciales
CREATE INDEX idx_perfil_empresa_usuario ON PERFIL_EMPRESA(id_usuario);
CREATE INDEX idx_perfil_empresa_cuit ON PERFIL_EMPRESA(cuit);
CREATE INDEX idx_perfil_empresa_estado ON PERFIL_EMPRESA(estado_aprobado);
CREATE INDEX idx_perfil_empresa_categoria ON PERFIL_EMPRESA(categoria_empresa);

-- Direcciones - geolocalización
CREATE INDEX idx_direccion_usuario ON DIRECCION(id_usuario) WHERE id_usuario IS NOT NULL;
CREATE INDEX idx_direccion_empresa ON DIRECCION(id_perfil_empresa) WHERE id_perfil_empresa IS NOT NULL;
CREATE INDEX idx_direccion_principal ON DIRECCION(id_usuario, es_principal) WHERE es_principal = true;
CREATE INDEX idx_direccion_ubicacion ON DIRECCION(pais, provincia, ciudad);

-- Teléfonos - comunicación
CREATE INDEX idx_telefono_usuario ON TELEFONO(id_usuario) WHERE id_usuario IS NOT NULL;
CREATE INDEX idx_telefono_empresa ON TELEFONO(id_perfil_empresa) WHERE id_perfil_empresa IS NOT NULL;
CREATE INDEX idx_telefono_numero ON TELEFONO(caracteristica, numero);
```

### Constraints de Integridad
```sql
-- Usuario y empresa son excluyentes en direcciones
ALTER TABLE DIRECCION ADD CONSTRAINT chk_direccion_owner
CHECK ((id_usuario IS NOT NULL AND id_perfil_empresa IS NULL) OR 
       (id_usuario IS NULL AND id_perfil_empresa IS NOT NULL));

-- Usuario y empresa son excluyentes en teléfonos  
ALTER TABLE TELEFONO ADD CONSTRAINT chk_telefono_owner
CHECK ((id_usuario IS NOT NULL AND id_perfil_empresa IS NULL) OR 
       (id_usuario IS NULL AND id_perfil_empresa IS NOT NULL));

-- Solo una dirección principal por usuario/empresa
CREATE UNIQUE INDEX idx_direccion_principal_usuario 
ON DIRECCION(id_usuario) WHERE es_principal = true AND id_usuario IS NOT NULL;

CREATE UNIQUE INDEX idx_direccion_principal_empresa 
ON DIRECCION(id_perfil_empresa) WHERE es_principal = true AND id_perfil_empresa IS NOT NULL;

-- Referencias de copia con desvinculación automática
ALTER TABLE DIRECCION ADD CONSTRAINT fk_direccion_copia
FOREIGN KEY (es_copia_de) REFERENCES DIRECCION(id_direccion) ON DELETE SET NULL;

ALTER TABLE TELEFONO ADD CONSTRAINT fk_telefono_copia  
FOREIGN KEY (es_copia_de) REFERENCES TELEFONO(id_telefono) ON DELETE SET NULL;
```

### Triggers de Desvinculación
```sql
-- Desvinculación automática en direcciones
CREATE TRIGGER handle_direccion_change
AFTER UPDATE ON DIRECCION
FOR EACH ROW
BEGIN
    IF OLD.calle != NEW.calle OR OLD.numero != NEW.numero OR 
       OLD.ciudad != NEW.ciudad OR OLD.provincia != NEW.provincia THEN
        UPDATE DIRECCION 
        SET es_copia_de = NULL
        WHERE es_copia_de = NEW.id_direccion;
    END IF;
END;

-- Desvinculación automática en teléfonos
CREATE TRIGGER handle_telefono_change
AFTER UPDATE ON TELEFONO
FOR EACH ROW
BEGIN
    IF OLD.numero != NEW.numero OR OLD.caracteristica != NEW.caracteristica THEN
        UPDATE TELEFONO 
        SET es_copia_de = NULL
        WHERE es_copia_de = NEW.id_telefono;
    END IF;
END;
```

---

## 📋 Reglas de Negocio

### Usuarios Base
1. ✅ Email y username deben ser únicos en todo el sistema
2. ✅ Solo usuarios con `email_verificado = true` pueden operar completamente
3. ✅ Documento del responsable debe ser único
4. ✅ Usuarios inactivos no pueden crear empresas
5. ✅ Aceptación de términos y política de privacidad obligatoria

### Perfil Empresa  
6. ✅ CUIT debe ser único y válido
7. ✅ Solo empresas `estado_aprobado = APROBADO` pueden vender
8. ✅ Un usuario puede ser responsable de múltiples empresas
9. ✅ Empresa requiere al menos una dirección fiscal
10. ✅ Email empresa puede ser igual al email del responsable

### Direcciones
11. ✅ Solo una dirección principal por usuario/empresa
12. ✅ Dirección fiscal obligatoria para empresas
13. ✅ Direcciones copiadas se desvincular automáticamente al cambiar original
14. ✅ No se puede eliminar dirección principal si es la única

### Teléfonos
15. ✅ Al menos un teléfono activo por usuario/empresa
16. ✅ Teléfonos principales deben estar verificados
17. ✅ Teléfonos copiados se desvincular automáticamente al cambiar original

### Sistema Híbrido de Copia
18. ✅ Solo empresas pueden copiar direcciones/teléfonos de usuarios (flujo registro)
19. ✅ Cambios en dirección/teléfono original desvincula automáticamente las copias
20. ✅ Eliminación de original convierte copias en independientes
21. ✅ Re-vinculación manual permitida después de desvinculación

---

## 🔄 Flujos de Trabajo

### 1. Registro de Usuario Persona Física
```
1. Usuario completa formulario básico
2. Valida email único y documento único
3. Envía email de verificación
4. Usuario verifica email
5. Solicita dirección principal
6. Solicita teléfono principal
7. Activar cuenta completamente
```

### 2. Creación de Perfil Empresa
```
1. Usuario autenticado solicita crear empresa
2. Completa datos fiscales (CUIT, razón social)
3. Sistema pregunta: "¿Usar misma dirección/teléfono?"
4. Si SÍ: crear con es_copia_de = direccion_usuario_id
5. Si NO: solicita datos independientes
6. Empresa queda en estado PENDIENTE
7. Admin revisa y APRUEBA/RECHAZA
```

### 3. Actualización de Datos
```
1. Usuario/empresa modifica dirección/teléfono
2. Sistema detecta cambio en campos clave
3. Trigger desvincula automáticamente las copias
4. Notifica a afectados sobre desvinculación
5. Permite re-vinculación manual si se desea
```

### 4. Cambio de Responsable de Empresa
```
1. Empresa solicita cambio de responsable
2. Nuevo responsable debe ser usuario verificado
3. Validar que nuevo responsable acepta
4. Transferir ownership de empresa
5. Mantener histórico del cambio
6. Notificar a ambos usuarios
```

---

## 🧪 Casos de Prueba Críticos

### Usuarios
- [ ] Registro con email duplicado (debe fallar)
- [ ] Registro sin aceptar términos (debe fallar)
- [ ] Verificación de email (debe activar funcionalidades)
- [ ] Login con cuenta no verificada (funcionalidad limitada)

### Empresas
- [ ] Crear empresa con CUIT duplicado (debe fallar)
- [ ] Empresa en estado PENDIENTE intenta vender (debe fallar)
- [ ] Usuario responsable de múltiples empresas (debe permitir)
- [ ] Transferir responsabilidad de empresa (debe funcionar)

### Direcciones/Teléfonos
- [ ] Crear dirección copiada (debe vincular correctamente)
- [ ] Cambiar dirección original (debe desvincular copias)
- [ ] Eliminar dirección original (debe independizar copias)
- [ ] Re-vincular después de desvinculación (debe permitir)

### Sistema Híbrido
- [ ] Empresa copia dirección de usuario (debe funcionar)
- [ ] Usuario intenta copiar dirección de empresa (debe fallar)
- [ ] Múltiples direcciones copian de la misma original (debe permitir)

---

## 🚨 Consideraciones de Seguridad

### Datos Sensibles
- **Documentos**: Encriptar números de documento en reposo
- **Teléfonos**: Ofuscar en logs y APIs públicas
- **Direcciones**: Limitar acceso geográfico preciso

### Validaciones
- **CUIT**: Validar algoritmo de verificación
- **Emails**: Validar formato y dominio
- **Teléfonos**: Validar formato por país

### Auditoría
- **Cambios críticos**: Log completo de modificaciones
- **Acceso a datos**: Registrar consultas a información sensible
- **Transferencias**: Trazabilidad completa de cambios de responsabilidad

### GDPR/Privacidad
- **Derecho al olvido**: Anonizar datos al eliminar usuario
- **Consentimiento**: Registro de aceptaciones legales
- **Portabilidad**: Export completo de datos del usuario

---

*📝 Nota: Este módulo maneja datos sensibles. Implementar encriptación y auditoría según normativas locales.*
