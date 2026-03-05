#!/usr/bin/env bash
set -euo pipefail

# Script para limpiar TODO el estado de Docker en un host (contenedores, imágenes,
# volúmenes y redes custom). Uso con precaución.
#
# Uso:
# 1) Recomendado: hacer backup de volúmenes importantes antes de ejecutar.
# 2) Copiar al servidor: scp scripts/clean_docker_server.sh user@server:/tmp/
# 3) Ejecutar (interactivo): sudo bash /tmp/clean_docker_server.sh
# 4) Ejecutar (sin preguntar): sudo bash /tmp/clean_docker_server.sh -y

FORCE=0
while getopts ":y" opt; do
  case ${opt} in
    y ) FORCE=1 ;;
    \? ) echo "Uso: $0 [-y]"; exit 1 ;;
  esac
done

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker no encontrado en este host. Abortando." >&2
  exit 1
fi

if [ "$FORCE" -ne 1 ]; then
  cat <<-EOF
  ATENCIÓN: Este script eliminará TODOS los contenedores, imágenes,
  volúmenes y redes custom en este host Docker.
  Si tienes datos críticos (bases de datos, volúmenes), haz backup antes.
  Para continuar escribe: yes
EOF
  read -r REPLY
  if [ "$REPLY" != "yes" ]; then
    echo "Abortado por usuario."; exit 1
  fi
fi

echo "Deteniendo y eliminando contenedores..."
CONTAINERS=$(docker ps -aq || true)
if [ -n "$CONTAINERS" ]; then
  docker stop $CONTAINERS || true
  docker rm -f $CONTAINERS || true
else
  echo "No hay contenedores a eliminar."
fi

echo "Eliminando imágenes..."
IMAGES=$(docker images -aq || true)
if [ -n "$IMAGES" ]; then
  docker rmi -f $IMAGES || true
else
  echo "No hay imágenes a eliminar."
fi

echo "Eliminando volúmenes..."
VOLUMES=$(docker volume ls -q || true)
if [ -n "$VOLUMES" ]; then
  docker volume rm -f $VOLUMES || true
else
  echo "No hay volúmenes a eliminar."
fi

echo "Eliminando redes custom..."
NETWORKS=$(docker network ls --filter "type=custom" -q || true)
if [ -n "$NETWORKS" ]; then
  docker network rm $NETWORKS || true
else
  echo "No hay redes custom a eliminar."
fi

echo "Ejecutando prune final (imágenes, contenedores, build cache, volúmenes)..."
docker system prune -af --volumes || true

echo "Limpieza completa."

exit 0
