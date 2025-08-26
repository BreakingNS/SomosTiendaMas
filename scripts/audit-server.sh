#!/bin/bash
# audit-server.sh - Script para auditar archivos del servidor antes de la limpieza

echo "=================================="
echo "🔍 AUDITORÍA DEL SERVIDOR"
echo "=================================="
echo "Fecha: $(date)"
echo "Usuario: $(whoami)"
echo "Servidor: $(hostname)"
echo ""

echo "📦 CONTENEDORES DOCKER:"
echo "----------------------------------"
docker ps -a --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
echo ""

echo "🖼️ IMÁGENES DOCKER:"
echo "----------------------------------"
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
echo ""

echo "🗂️ VOLÚMENES DOCKER:"
echo "----------------------------------"
docker volume ls
echo ""

echo "🌐 REDES DOCKER:"
echo "----------------------------------"
docker network ls
echo ""

echo "📁 ARCHIVOS JAR (últimos 30 días):"
echo "----------------------------------"
find /home -name "*.jar" -mtime -30 -ls 2>/dev/null || echo "No se encontraron archivos JAR"
find /opt -name "*.jar" -mtime -30 -ls 2>/dev/null || echo "No se encontraron archivos JAR en /opt"
find /var -name "*.jar" -mtime -30 -ls 2>/dev/null || echo "No se encontraron archivos JAR en /var"
echo ""

echo "📁 DIRECTORIOS DE PROYECTOS:"
echo "----------------------------------"
find /home -maxdepth 3 -type d -name "*tienda*" -o -name "*SomosTiendaMas*" 2>/dev/null || echo "No se encontraron directorios del proyecto"
find /opt -maxdepth 3 -type d -name "*tienda*" -o -name "*SomosTiendaMas*" 2>/dev/null || echo "No se encontraron directorios del proyecto en /opt"
echo ""

echo "🔧 SERVICIOS SYSTEMD (Spring/Java):"
echo "----------------------------------"
systemctl list-units --type=service --state=running | grep -i java || echo "No hay servicios Java ejecutándose"
systemctl list-units --type=service --all | grep -i tienda || echo "No hay servicios relacionados con tienda"
echo ""

echo "🔌 PUERTOS EN USO (8080, 8443, 5432):"
echo "----------------------------------"
netstat -tulnp | grep -E ":8080|:8443|:5432" || echo "Ninguno de los puertos principales está en uso"
echo ""

echo "📄 ARCHIVOS DOCKER-COMPOSE:"
echo "----------------------------------"
find /home -name "docker-compose.yml" -o -name "docker-compose.yaml" 2>/dev/null || echo "No se encontraron archivos docker-compose"
find /opt -name "docker-compose.yml" -o -name "docker-compose.yaml" 2>/dev/null || echo "No se encontraron archivos docker-compose en /opt"
echo ""

echo "🗄️ BASES DE DATOS PostgreSQL:"
echo "----------------------------------"
if command -v psql >/dev/null 2>&1; then
    sudo -u postgres psql -l 2>/dev/null || echo "No se pudo acceder a PostgreSQL"
else
    echo "PostgreSQL no está instalado localmente"
fi
echo ""

echo "💾 ESPACIO EN DISCO:"
echo "----------------------------------"
df -h | grep -E "/$|/home|/opt|/var"
echo ""

echo "🐳 ESPACIO USADO POR DOCKER:"
echo "----------------------------------"
docker system df
echo ""

echo "=================================="
echo "✅ AUDITORÍA COMPLETADA"
echo "=================================="
