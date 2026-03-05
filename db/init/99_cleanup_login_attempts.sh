#!/bin/bash
set -euo pipefail

echo "[99_cleanup_login_attempts] Limpiando tabla login_failed_attempts y desbloqueando usuarios..."

# Ejecutar SQL para limpiar intentos fallidos y desbloquear cuentas
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<'SQL'
-- Evitar error si la tabla no existe
TRUNCATE TABLE IF EXISTS login_failed_attempts;
-- Desbloquear usuarios si la columna existe y está en true
DO $$
BEGIN
	IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='usuario' AND column_name='cuenta_bloqueada') THEN
		EXECUTE 'UPDATE usuario SET cuenta_bloqueada = false WHERE cuenta_bloqueada = true';
	END IF;
END$$;
SQL

echo "[99_cleanup_login_attempts] OK"
