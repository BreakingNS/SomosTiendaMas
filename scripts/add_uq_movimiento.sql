-- Agrega constraint UNIQUE para prevenir duplicados basados en order_ref, tipo y variante_id
-- Requiere que no existan duplicados (ejecutar primero cleanup_duplicate_movimientos.sql)
ALTER TABLE movimiento_inventario
  ADD CONSTRAINT uq_mov_order_tipo_var UNIQUE (order_ref, tipo, variante_id);

-- Nota: si el DBMS rechaza la operación por datos existentes, ejecutar el script de limpieza y reintentar.
