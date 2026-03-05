#!/bin/bash
set -euo pipefail

# Ruta esperada del dump (montada por docker-compose en /docker-entrypoint-initdb.d)
DUMP_FILE="/docker-entrypoint-initdb.d/plantilla_beta1.dump"

if [ ! -f "$DUMP_FILE" ]; then
  echo "[restore.sh] No se encontró $DUMP_FILE — omitiendo restore."
  exit 0
fi

echo "[restore.sh] Restaurando $DUMP_FILE en la base $POSTGRES_DB (usuario $POSTGRES_USER)"

# Detectar extensión y elegir método (pg_restore para dumps en formato personalizado, psql para SQL)
case "$DUMP_FILE" in
  *.sql|*.sql.gz)
    if [[ "$DUMP_FILE" == *.gz ]]; then
      gzip -dc "$DUMP_FILE" | psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"
    else
      psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$DUMP_FILE"
    fi
    ;;
  *.dump|*.pgdump|*.dump.gz)
    if [[ "$DUMP_FILE" == *.gz ]]; then
      gzip -dc "$DUMP_FILE" > /tmp/dump_uncompressed.dump
      pg_restore --verbose --no-owner --role="$POSTGRES_USER" -d "$POSTGRES_DB" /tmp/dump_uncompressed.dump
    else
      pg_restore --verbose --no-owner --role="$POSTGRES_USER" -d "$POSTGRES_DB" "$DUMP_FILE"
    fi
    ;;
  *)
    echo "[restore.sh] Formato desconocido, intentando pg_restore por defecto"
    pg_restore --verbose --no-owner --role="$POSTGRES_USER" -d "$POSTGRES_DB" "$DUMP_FILE" || {
      echo "[restore.sh] pg_restore falló; intenta convertir el dump a SQL o revisar formato." >&2
      exit 1
    }
    ;;
esac

echo "[restore.sh] Restauración finalizada."
