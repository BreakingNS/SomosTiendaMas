-- Eliminar movimientos duplicados por (order_ref, tipo, variante_id)
-- Mantiene la fila más antigua (por created_at, id) y borra el resto.
BEGIN;

DELETE FROM movimiento_inventario
WHERE id IN (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (
      PARTITION BY order_ref, tipo, variante_id
      ORDER BY created_at ASC, id ASC
    ) AS rn
    FROM movimiento_inventario
    WHERE order_ref IS NOT NULL
  ) sub WHERE rn > 1
);

COMMIT;

-- Nota: ejecutar un BACKUP previo de la tabla antes de correr esto en producción.
