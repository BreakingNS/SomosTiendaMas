#!/bin/bash
set -euo pipefail

echo "[99_cleanup_login_attempts] Limpiando tabla login_failed_attempts y desbloqueando usuarios..."

# Ejecutar SQL para limpiar intentos fallidos y desbloquear cuentas
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<'SQL'
TRUNCATE TABLE login_failed_attempts;
UPDATE usuario SET cuenta_bloqueada = false WHERE cuenta_bloqueada = true;
SQL

echo "[99_cleanup_login_attempts] OK"
