# 🔐 MÓDULO: AUTENTICACIÓN Y USUARIOS
*Documento Hijo 1*

---

## 📋 Información del Módulo
- **Responsable**: Security Team
- **Última Actualización**: 11 Agosto 2025
- **Estado**: ✅ Activo
- **Dependencias**: Ninguna (Módulo base)
- **Entidades**: 5

---

## 🎯 Propósito del Módulo
Gestión completa de autenticación, autorización y seguridad de sesiones. Maneja tokens JWT, refresh tokens, control de sesiones activas, prevención de ataques de fuerza bruta y recuperación segura de contraseñas.

---

## 🏛️ Entidades

### 1. REFRESH_TOKEN
**Propósito**: Gestión de tokens de actualización para JWT

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `token` | VARCHAR | 512 | ✅ | 🔐 UK | Token de refresh único |
| `id_usuario` | BIGINT | - | ✅ | 🔗 FK | Referencia al usuario |
| `fecha_expiracion` | TIMESTAMP | - | ✅ | - | Fecha límite de validez |
| `fecha_revocado` | TIMESTAMP | - | ❌ | - | Momento de revocación |
| `usado` | BOOLEAN | - | ✅ | - | Indica si ya fue utilizado |
| `revocado` | BOOLEAN | - | ✅ | - | Estado de revocación |
| `ip` | VARCHAR | 45 | ✅ | - | Dirección IP de origen |
| `user_agent` | TEXT | - | ✅ | - | Navegador/dispositivo |

---

### 2. TOKEN_ISSUED  
**Propósito**: Registro de todos los tokens JWT emitidos

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `token` | TEXT | - | ✅ | 🔐 UK | Token JWT completo |
| `id_usuario` | BIGINT | - | ✅ | 🔗 FK | Referencia al usuario |
| `fecha_expiracion` | TIMESTAMP | - | ✅ | - | Expiración del token |
| `fecha_emision` | TIMESTAMP | - | ✅ | - | Momento de creación |
| `revocado` | BOOLEAN | - | ✅ | - | Estado de revocación |

---

### 3. ACTIVE_SESSIONS
**Propósito**: Control de sesiones activas por usuario

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `token` | VARCHAR | 512 | ✅ | 🔐 UK | Token de sesión |
| `id_usuario` | BIGINT | - | ✅ | 🔗 FK | Referencia al usuario |
| `ip` | VARCHAR | 45 | ✅ | - | Dirección IP de la sesión |
| `user_agent` | TEXT | - | ✅ | - | Información del navegador |
| `fecha_inicio_sesion` | TIMESTAMP | - | ✅ | - | Inicio de la sesión |
| `fecha_expiracion` | TIMESTAMP | - | ✅ | - | Expiración de la sesión |
| `revocado` | BOOLEAN | - | ✅ | - | Estado de la sesión |

---

### 4. LOGIN_FAILED_ATTEMPTS
**Propósito**: Prevención de ataques de fuerza bruta

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_usuario` | BIGINT | - | ❌ | 🔗 FK | Usuario (si existe) |
| `username` | VARCHAR | 100 | ❌ | - | Email/username intentado |
| `ip` | VARCHAR | 45 | ✅ | - | IP del intento fallido |
| `failed_attempts` | INTEGER | - | ✅ | - | Número de intentos |
| `last_attempt` | TIMESTAMP | - | ✅ | - | Último intento fallido |
| `blocked_until` | TIMESTAMP | - | ❌ | - | Bloqueo hasta fecha |

---

### 5. PASSWORD_RESET_TOKENS
**Propósito**: Tokens para recuperación de contraseñas

| Campo | Tipo | Tamaño | Requerido | Clave | Descripción |
|-------|------|--------|-----------|-------|-------------|
| `id` | BIGINT | - | ✅ | 🔑 PK | Identificador único |
| `id_usuario` | BIGINT | - | ✅ | 🔗 FK | Usuario solicitante |
| `token` | VARCHAR | 255 | ✅ | 🔐 UK | Token de reset único |
| `fecha_expiracion` | TIMESTAMP | - | ✅ | - | Límite de validez |
| `usado` | BOOLEAN | - | ✅ | - | Estado de uso |

---

## 🔗 Relaciones

### Hacia el Módulo de Usuarios
| Entidad Origen | Entidad Destino | Tipo | Cardinalidad | Descripción |
|----------------|-----------------|------|--------------|-------------|
| REFRESH_TOKEN | USUARIO | Muchos a Uno | N:1 | Un usuario puede tener múltiples refresh tokens |
| TOKEN_ISSUED | USUARIO | Muchos a Uno | N:1 | Un usuario puede tener múltiples tokens emitidos |
| ACTIVE_SESSIONS | USUARIO | Muchos a Uno | N:1 | Un usuario puede tener múltiples sesiones |
| LOGIN_FAILED_ATTEMPTS | USUARIO | Muchos a Uno | N:1 | Intentos fallidos por usuario |
| PASSWORD_RESET_TOKENS | USUARIO | Muchos a Uno | N:1 | Tokens de reset por usuario |

---

## ⚙️ Configuraciones

### Índices Críticos
```sql
-- Para consultas de autenticación (muy frecuentes)
CREATE INDEX idx_refresh_token_user ON REFRESH_TOKEN(id_usuario, revocado);
CREATE INDEX idx_refresh_token_lookup ON REFRESH_TOKEN(token) WHERE revocado = false;

-- Para validación de tokens JWT
CREATE INDEX idx_token_issued_user ON TOKEN_ISSUED(id_usuario, revocado);
CREATE INDEX idx_token_issued_lookup ON TOKEN_ISSUED(token) WHERE revocado = false;

-- Para gestión de sesiones
CREATE INDEX idx_active_sessions_user ON ACTIVE_SESSIONS(id_usuario, revocado);
CREATE INDEX idx_active_sessions_cleanup ON ACTIVE_SESSIONS(fecha_expiracion) WHERE revocado = false;

-- Para prevención de ataques
CREATE INDEX idx_login_attempts_ip ON LOGIN_FAILED_ATTEMPTS(ip, last_attempt);
CREATE INDEX idx_login_attempts_user ON LOGIN_FAILED_ATTEMPTS(id_usuario) WHERE id_usuario IS NOT NULL;

-- Para reset de passwords
CREATE INDEX idx_password_reset_user ON PASSWORD_RESET_TOKENS(id_usuario, usado);
CREATE INDEX idx_password_reset_token ON PASSWORD_RESET_TOKENS(token) WHERE usado = false;
```

### Políticas de Limpieza
```sql
-- Cleanup automático de tokens expirados (ejecutar diariamente)
DELETE FROM REFRESH_TOKEN 
WHERE fecha_expiracion < CURRENT_TIMESTAMP - INTERVAL '7 days';

DELETE FROM TOKEN_ISSUED 
WHERE fecha_expiracion < CURRENT_TIMESTAMP - INTERVAL '1 day';

DELETE FROM ACTIVE_SESSIONS 
WHERE fecha_expiracion < CURRENT_TIMESTAMP;

-- Cleanup de intentos fallidos antiguos (ejecutar semanalmente)
DELETE FROM LOGIN_FAILED_ATTEMPTS 
WHERE last_attempt < CURRENT_TIMESTAMP - INTERVAL '30 days';

-- Cleanup de tokens de reset usados o expirados
DELETE FROM PASSWORD_RESET_TOKENS 
WHERE usado = true OR fecha_expiracion < CURRENT_TIMESTAMP;
```

---

## 📋 Reglas de Negocio

### Gestión de Tokens
1. ✅ **Refresh tokens** tienen vigencia de 30 días
2. ✅ **JWT tokens** tienen vigencia de 15 minutos  
3. ✅ **Reset tokens** tienen vigencia de 1 hora
4. ✅ Solo se permite 1 refresh token activo por dispositivo
5. ✅ Tokens revocados no se pueden reutilizar

### Control de Sesiones
6. ✅ Máximo 5 sesiones activas por usuario
7. ✅ Sesiones inactivas por 30 minutos se marcan para expirar
8. ✅ Logout revoca todos los tokens relacionados
9. ✅ Cambio de contraseña revoca todas las sesiones

### Seguridad Anti-Brute Force
10. ✅ Máximo 5 intentos fallidos por IP en 15 minutos
11. ✅ Bloqueo de IP por 1 hora después de 5 intentos
12. ✅ Máximo 3 intentos fallidos por usuario en 15 minutos
13. ✅ Bloqueo de cuenta por 30 minutos después de 3 intentos

### Recuperación de Contraseñas
14. ✅ Solo 1 token de reset activo por usuario
15. ✅ Nuevo token invalida el anterior
16. ✅ Token usado una vez se marca como consumido
17. ✅ Máximo 3 solicitudes de reset por hora por usuario

---

## 🔄 Flujos de Trabajo

### 1. Proceso de Login
```
1. Usuario envía credenciales
2. Validar usuario activo
3. Verificar intentos fallidos
4. Autenticar contraseña
5. Crear JWT token
6. Crear refresh token
7. Registrar sesión activa
8. Retornar tokens al cliente
```

### 2. Refresh de Token
```
1. Cliente envía refresh token
2. Validar token no revocado/expirado
3. Verificar sesión activa
4. Generar nuevo JWT
5. Marcar refresh token como usado
6. Crear nuevo refresh token
7. Actualizar registro de sesión
```

### 3. Logout
```
1. Recibir request de logout
2. Revocar JWT actual
3. Revocar refresh tokens del usuario
4. Marcar sesión como revocada
5. Confirmar logout exitoso
```

### 4. Reset de Contraseña
```
1. Usuario solicita reset
2. Validar email existe
3. Invalidar tokens previos
4. Generar nuevo token
5. Enviar email con enlace
6. Usuario accede y cambia password
7. Marcar token como usado
8. Revocar todas las sesiones
```

---

## 🧪 Casos de Prueba Críticos

### Autenticación
- [ ] Login con credenciales válidas (debe crear tokens)
- [ ] Login con credenciales inválidas (debe incrementar intentos)
- [ ] Login después de 5 intentos fallidos (debe bloquear)
- [ ] Refresh token válido (debe generar nuevo JWT)
- [ ] Refresh token expirado (debe fallar)

### Seguridad
- [ ] Uso de token revocado (debe fallar)
- [ ] Múltiples sesiones desde diferentes IPs (debe permitir hasta 5)
- [ ] Intento de reutilizar refresh token (debe fallar)
- [ ] Reset de password con token expirado (debe fallar)

### Limpieza
- [ ] Cleanup automático de tokens expirados
- [ ] Cleanup de sesiones inactivas
- [ ] Cleanup de intentos fallidos antiguos

---

## 🚨 Consideraciones de Seguridad

### Almacenamiento Seguro
- **Tokens**: Hasheados con SHA-256 antes de almacenar
- **IPs**: Indexadas para consulta rápida de bloqueos
- **User Agents**: Limitados a 2000 caracteres para prevenir ataques

### Monitoreo
- **Alertas**: Más de 10 intentos fallidos por minuto
- **Logs**: Todos los eventos de autenticación
- **Métricas**: Tiempo de respuesta de validación de tokens

### Compliance
- **GDPR**: Datos de sesión se eliminan después de 30 días
- **PCI**: No se almacenan datos de tarjetas en este módulo
- **Auditoría**: Log completo de eventos de seguridad

---

*📝 Nota: Este módulo es crítico para la seguridad. Cualquier cambio debe ser revisado por el Security Team.*
