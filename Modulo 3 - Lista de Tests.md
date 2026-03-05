Modulo 3: Tests Unitarios y de Integracion

---
## 🎯 FASE 1 - Tests Prioritarios (Pragmático)

### Complejidad 1 - CRUD Básico
**Etiqueta, Marca, Vendedor**
- [ ] Tests de integración básicos (Repository + API CRUD con DB real)

#### 📝 Bitácora - Tests de Integración CRUD Básico

**Para cada entidad (Etiqueta, Marca, Vendedor):**

# Lista de Models:
## -2-  -3-  -4-
## [ok] [ok] [ok] Etiqueta (Complejidad: 1)
## [ok] [ok] [ok] Marca (Complejidad: 1)
REVIEW: Implementar controller "Vendedor" para tests
## [ok] [  ] [  ] Vendedor (Complejidad: 1) 
## [ok] [ok] [ok] Categoria (Complejidad: 2)
## [ok] [ok] [ok] Opcion (Complejidad: 2)
## [--] [--] [--] PlantillaCampo (Complejidad: 2)
## [ok] [ok] [ok] OpcionValor (Complejidad: 3)
## [--] [--] [--] PlantillaCategoria (Complejidad: 3)
## [--] [--] [--] ProductoAtributo (Complejidad: 3)
## [ok] [ok] [ok] ProductoEtiqueta (Complejidad: 3)
## [--] [--] [--] ProductDraft (Complejidad: 3)
## [ok] [ok] [ok] Producto (Complejidad: 4)
## [ok] [ok] [ok] Variante (Complejidad: 4)
NOTE: VarianteOpcionValorIntegration se encarga de los tests de API relacionados con VarianteOpcion
## [ok] [--] [ok] VarianteOpcion (Complejidad: 4)
## [ok] [ok] [ok] VarianteOpcionValor (Complejidad: 4)
NOTE: VarianteOpcionValorIntegration se encarga de los tests de API relacionados con VarianteValor
## [ok] [--] [ok] VarianteValor (Complejidad: 4)
## [ok] [ok] [ok] ImagenVariante (Complejidad: 5)
## [ok] [ok] [ok] PrecioVariante (Complejidad: 5)
## [ok] [ok] [ok] InventarioVariante (Complejidad: 5)
## [ok] [ok] [ok] VarianteFisico (Complejidad: 5)
## [ok] [ok] [ok] MovimientoInventario (Complejidad: 5)

1. **Setup del Test**
   - [x] Crear clase `[Entidad]IntegrationTest` en `src/test/java/.../catalogo/integration/`
   - [x] Anotar con `@SpringBootTest` y `@Transactional` (rollback automático)
   - [x] Inyectar `[Entidad]Repository` y/o `TestRestTemplate` (para API)
   - [x] Configurar DB de test (H2 o PostgreSQL según `application-test.properties`)

2. **Tests de Repository (JPA)**
   - [x] `save_DeberiaCrear[Entidad]_CuandoDatosValidos()`
      - Crear entidad con datos válidos
      - Llamar `repository.save(entidad)`
      - Verificar que `id != null` (auto-generado)
      - Verificar que los datos se guardaron correctamente
   
   - [x] `findById_DeberiaRetornar[Entidad]_CuandoExiste()`
      - Guardar entidad en DB
      - Buscar por ID: `repository.findById(id)`
      - Verificar que `Optional.isPresent()` es true
      - Verificar datos coinciden
   
   - [x] `findAll_DeberiaRetornarLista_CuandoExisten[Entidades]()`
      - Guardar 3 entidades diferentes
      - Llamar `repository.findAll()`
      - Verificar que retorna 3+ elementos (puede haber datos previos)
   
   - [x] `delete_DeberiaEliminar[Entidad]_CuandoExiste()`
      - Guardar entidad
      - Llamar `repository.deleteById(id)`
      - Verificar que `repository.findById(id).isEmpty()` es true
   
   - [x] `update_DeberiaActualizar[Entidad]_CuandoExiste()`
      - Guardar entidad con nombre "Original"
      - Modificar nombre a "Actualizado"
      - Llamar `repository.save(entidadModificada)`
      - Buscar por ID y verificar nombre es "Actualizado"

3. **Tests de API (Controller)**
   - [x] `POST /api/[entidades] - DeberiaCrear[Entidad]_Status201()`
      - Enviar JSON válido via `restTemplate.postForEntity()`
      - Verificar status 201 Created
      - Verificar que respuesta contiene ID generado
      - Verificar que datos coinciden
   
   - [x] `GET /api/[entidades]/{id} - DeberiaRetornar[Entidad]_Status200()`
      - Crear entidad directamente en DB (via repository)
      - Hacer GET con el ID
      - Verificar status 200 OK
      - Verificar datos en respuesta
   
   - [x] `GET /api/[entidades] - DeberiaRetornarLista_Status200()`
      - Crear 2-3 entidades en DB
      - Hacer GET a la lista
      - Verificar status 200 OK
      - Verificar que lista contiene al menos las creadas
   
   - [x] `PUT /api/[entidades]/{id} - DeberiaActualizar[Entidad]_Status200()`
      - Crear entidad en DB
      - Enviar PUT con datos modificados
      - Verificar status 200 OK
      - Verificar que datos se actualizaron en DB
   
   - [x] `DELETE /api/[entidades]/{id} - DeberiaEliminar[Entidad]_Status204()`
      - Crear entidad en DB
      - Enviar DELETE
      - Verificar status 204 No Content
      - Verificar que no existe en DB
   
   - [x] `POST /api/[entidades] - DeberiaRetornar400_CuandoDatosInvalidos()`
      - Enviar JSON con datos inválidos (ej: nombre vacío)
      - Verificar status 400 Bad Request
      - Verificar mensaje de error en respuesta

4. **Verificaciones de DB Real**
   - [x] Confirmar que transacciones funcionan (rollback en tests)
   - [x] Verificar que constraints de DB se respetan (unique, not null, etc.)
   - [x] Verificar que auditoría funciona si está implementada (createdAt, updatedAt)

**Notas:**
- Usar `@BeforeEach` para limpiar datos si es necesario
- Usar builders o métodos helper para crear entidades de test
- No mockear nada - debe usar DB real (H2 o PostgreSQL de test)
- Cada test debe ser independiente y poder correr en cualquier orden

### Complejidad 2 - Relaciones Simples
**Categoria** (auto-referencia padre/hijo)
- [ ] Tests de integración (Repository: cascada padre/hijo)
- [ ] Tests de API (CRUD + validación jerarquía)

**Opcion, PlantillaCampo**
- [ ] Tests de integración básicos (Repository + API CRUD)

### Complejidad 3 - Dependencias
**OpcionValor, PlantillaCategoria, ProductoAtributo, ProductoEtiqueta, ProductDraft**
- [ ] Tests de integración (Repository: relaciones FK funcionan)
- [ ] Tests de API (CRUD con validación de dependencias)

### Complejidad 4 - Core del Negocio
**Producto**
- [ ] Tests unitarios (Service: validaciones de negocio críticas)
- [ ] Tests de integración (Repository: relaciones complejas + cascadas)
- [ ] Tests de API (CRUD completo + casos edge)

**Variante**
- [ ] Tests unitarios (Service: lógica de generación/validación)
- [ ] Tests de integración (Repository: relaciones + herencia)
- [ ] Tests de API (CRUD + generación automática)

**VarianteOpcion, VarianteOpcionValor, VarianteValor**
- [ ] Tests de integración (Repository: relaciones many-to-many)
- [ ] Tests de API (validación de combinaciones)

### Complejidad 5 - Lógica Crítica de Negocio
**PrecioVariante**
- [ ] Tests unitarios (Service: cálculos de precios con descuentos/impuestos)
- [ ] Tests de integración (Repository + API)

**InventarioVariante**
- [ ] Tests unitarios (Service: validaciones de stock críticas)
- [ ] Tests de integración (Repository: concurrencia/locks)
- [ ] Tests de API (operaciones de stock)

**MovimientoInventario**
- [ ] Tests unitarios (Service: lógica de movimientos + validaciones)
- [ ] Tests de integración (Repository: transaccionalidad + auditoría)
- [ ] Tests de API (registro de movimientos)

**ImagenVariante, VarianteFisico**
- [ ] Tests de integración básicos (Repository + API)

---
## 📋 BACKLOG COMPLETO (Para futuro - cuando la funcionalidad esté estable)

## Etiqueta (Complejidad: 1)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## Marca (Complejidad: 1)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## Vendedor (Complejidad: 1)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## Categoria (Complejidad: 2)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## Opcion (Complejidad: 2)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## PlantillaCampo (Complejidad: 2)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## OpcionValor (Complejidad: 3)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## PlantillaCategoria (Complejidad: 3)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## ProductoAtributo (Complejidad: 3)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## ProductoEtiqueta (Complejidad: 3)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## ProductDraft (Complejidad: 3)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## Producto (Complejidad: 4)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## Variante (Complejidad: 4)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## VarianteOpcion (Complejidad: 4)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## VarianteOpcionValor (Complejidad: 4)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## VarianteValor (Complejidad: 4)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## ImagenVariante (Complejidad: 5)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## PrecioVariante (Complejidad: 5)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## InventarioVariante (Complejidad: 5)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## VarianteFisico (Complejidad: 5)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

## MovimientoInventario (Complejidad: 5)
- [ ] Modelo
- [ ] Mapper/DTOs
- [ ] Service
- [ ] Repository (JPA)
- [ ] Controller / API
- [ ] End-to-End

SOLO EJEMPLOS PARA MODELADO, NO VAN EN EL FINAL{
    ## Tareas pendientes

    - [ ] Tarea sin completar
    - [x] Tarea completada
    - [ ] Otra tarea pendiente

    ## Lista simple
    - Implementar validación de usuarios
    - Crear endpoint de productos
    - Agregar tests unitarios

    ## Lista numerada
    1. Primera tarea
    2. Segunda tarea
    3. Tercera tarea

    ## Con TODO tradicional (si prefieres)

    - Revisar código
    - Optimizar consultas
}