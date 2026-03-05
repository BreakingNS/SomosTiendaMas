#!/usr/bin/env bash
set -euo pipefail

# Convierte enlaces absolutos a localhost en rutas root-relative dentro de
# todos los HTML bajo src/main/resources/static.
# Crea copias .bak de los archivos originales por seguridad.

ROOT="src/main/resources/static"
if [ ! -d "$ROOT" ]; then
  echo "No se encuentra $ROOT desde el directorio $(pwd)"
  exit 1
fi

echo "Buscando archivos HTML en $ROOT..."

# Reemplaza 'http(s)://localhost[:port]/' por '/'
find "$ROOT" -type f -name '*.html' -print0 | xargs -0 sed -i.bak -E 's#https?://localhost(:[0-9]+)?/?#/#g'

echo "Reemplazos realizados. Copias de seguridad (*.bak) creadas para cada archivo modificado."
echo "Verifica los archivos y, si todo está bien, ejecuta: find $ROOT -name '*.bak' -delete"
