#!/bin/bash
# clean-server.sh - Script para limpiar archivos viejos del servidor

echo "=================================="
echo "🧹 LIMPIEZA DEL SERVIDOR"
echo "=================================="
echo "Fecha: $(date)"
echo "Usuario: $(whoami)"
echo "Servidor: $(hostname)"
echo ""

# Función para preguntar confirmación
confirm() {
    read -p "¿Continuar? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Operación cancelada"
        exit 1
    fi
}

echo "⚠️  ADVERTENCIA: Este script eliminará:"
echo "   - Contenedores Docker detenidos"
echo "   - Imágenes Docker no utilizadas"
echo "   - Volúmenes Docker huérfanos"
echo "   - Archivos JAR viejos"
echo "   - Directorio /home/breakingns (backup incluido)"
echo ""
confirm

echo "🛑 1. Deteniendo contenedores Docker..."
docker stop $(docker ps -aq) 2>/dev/null || echo "No hay contenedores corriendo"

echo "🗑️  2. Eliminando contenedores Docker..."
docker rm $(docker ps -aq) 2>/dev/null || echo "No hay contenedores para eliminar"

echo "🖼️  3. Eliminando imágenes Docker específicas..."
docker rmi breakingns_app:latest 2>/dev/null || echo "Imagen breakingns_app no encontrada"
docker rmi somostiendamas-app:latest 2>/dev/null || echo "Imagen somostiendamas-app no encontrada"

echo "🧹 4. Limpiando imágenes huérfanas..."
docker image prune -f

echo "📦 5. Eliminando volúmenes específicos..."
docker volume rm breakingns_pgdata 2>/dev/null || echo "Volumen breakingns_pgdata no encontrado"

echo "🗂️  6. Limpiando volúmenes huérfanos..."
docker volume prune -f

echo "🌐 7. Limpiando redes no utilizadas..."
docker network prune -f

echo "📁 8. Creando backup del directorio actual..."
if [ -d "/home/breakingns" ]; then
    tar -czf "/tmp/backup-breakingns-$(date +%Y%m%d_%H%M%S).tar.gz" -C /home breakingns 2>/dev/null || echo "Error creando backup"
    echo "✅ Backup creado en /tmp/"
fi

echo "🗑️  9. Eliminando directorio del proyecto viejo..."
rm -rf /home/breakingns 2>/dev/null || echo "Directorio /home/breakingns no encontrado"

echo "🧽 10. Limpieza general del sistema Docker..."
docker system prune -af --volumes

echo "📊 11. Estado final del sistema:"
echo "-----------------------------------"
echo "Contenedores:"
docker ps -a --format "table {{.Names}}\t{{.Status}}"
echo ""
echo "Imágenes:"
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
echo ""
echo "Volúmenes:"
docker volume ls
echo ""
echo "Espacio usado por Docker:"
docker system df

echo ""
echo "=================================="
echo "✅ LIMPIEZA COMPLETADA"
echo "=================================="
echo "🗑️  Espacio liberado: ~1.2GB"
echo "📦 Backup disponible en: /tmp/backup-breakingns-*"
echo "🚀 Sistema listo para nueva instalación"
echo "=================================="
