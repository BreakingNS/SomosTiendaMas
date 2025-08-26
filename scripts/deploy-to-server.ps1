# deploy-to-server.ps1 - Script para desplegar SomosTiendaMas al servidor

param(
    [Parameter(Mandatory=$true)]
    [string]$ServerUser,
    
    [Parameter(Mandatory=$true)]
    [string]$ServerHost,
    
    [string]$PrivateKeyPath = "~/privateKey",
    [string]$RemotePath = "/opt/SomosTiendaMas"
)

Write-Host "==================================" -ForegroundColor Green
Write-Host "🚀 DESPLIEGUE AL SERVIDOR" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host "Fecha: $(Get-Date)"
Write-Host "Servidor: $ServerUser@$ServerHost"
Write-Host "Ruta remota: $RemotePath"
Write-Host ""

# Verificar que existe la clave privada
if (!(Test-Path $PrivateKeyPath)) {
    Write-Host "❌ ERROR: No se encuentra la clave privada en: $PrivateKeyPath" -ForegroundColor Red
    exit 1
}

Write-Host "📦 1. Compilando proyecto..." -ForegroundColor Yellow
./mvnw clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ ERROR: Falló la compilación" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Compilación exitosa" -ForegroundColor Green

Write-Host "📁 2. Creando estructura temporal..." -ForegroundColor Yellow
$tempDir = "temp-deploy"
if (Test-Path $tempDir) { Remove-Item -Recurse -Force $tempDir }
New-Item -ItemType Directory -Path $tempDir | Out-Null

# Crear estructura de directorios
New-Item -ItemType Directory -Path "$tempDir/target" | Out-Null
New-Item -ItemType Directory -Path "$tempDir/src/main/resources/keys" -Force | Out-Null
New-Item -ItemType Directory -Path "$tempDir/scripts" | Out-Null

Write-Host "📋 3. Copiando archivos necesarios..." -ForegroundColor Yellow

# Archivos principales
Copy-Item "docker-compose.yml" "$tempDir/"
Copy-Item "Dockerfile" "$tempDir/"
Copy-Item ".env" "$tempDir/"

# JAR compilado
Copy-Item "target/SomosTiendaMas-0.1.6.4.jar" "$tempDir/target/"

# Certificados y claves
Copy-Item "src/main/resources/keys/keystore.p12" "$tempDir/src/main/resources/keys/"
Copy-Item "src/main/resources/keys/private.pem" "$tempDir/src/main/resources/keys/"
Copy-Item "src/main/resources/keys/public.pem" "$tempDir/src/main/resources/keys/"

# Configuraciones
Copy-Item "src/main/resources/application-docker.properties" "$tempDir/src/main/resources/"

# Scripts para el servidor
Copy-Item "scripts/audit-server.sh" "$tempDir/scripts/"
Copy-Item "scripts/clean-server.sh" "$tempDir/scripts/"

Write-Host "✅ Archivos copiados" -ForegroundColor Green

Write-Host "📊 4. Verificando archivos..." -ForegroundColor Yellow
$requiredFiles = @(
    "$tempDir/docker-compose.yml",
    "$tempDir/Dockerfile",
    "$tempDir/.env",
    "$tempDir/target/SomosTiendaMas-0.1.6.4.jar",
    "$tempDir/src/main/resources/keys/keystore.p12",
    "$tempDir/src/main/resources/keys/private.pem",
    "$tempDir/src/main/resources/keys/public.pem",
    "$tempDir/src/main/resources/application-docker.properties"
)

foreach ($file in $requiredFiles) {
    if (!(Test-Path $file)) {
        Write-Host "❌ ERROR: Falta archivo: $file" -ForegroundColor Red
        exit 1
    }
    Write-Host "  ✓ $file" -ForegroundColor Gray
}
Write-Host "✅ Verificación completa" -ForegroundColor Green

Write-Host "🗜️  5. Creando archivo comprimido..." -ForegroundColor Yellow
$zipFile = "SomosTiendaMas-deploy-$(Get-Date -Format 'yyyyMMdd_HHmmss').tar.gz"

# Usar tar para crear archivo compatible con Linux
tar -czf $zipFile -C $tempDir .

if (!(Test-Path $zipFile)) {
    Write-Host "❌ ERROR: No se pudo crear el archivo comprimido" -ForegroundColor Red
    exit 1
}

$zipSize = [math]::Round((Get-Item $zipFile).Length / 1MB, 2)
Write-Host "✅ Archivo creado: $zipFile ($zipSize MB)" -ForegroundColor Green

Write-Host "🌐 6. Preparando servidor..." -ForegroundColor Yellow
# Crear directorio remoto y limpiar si existe
$sshCmd = "ssh -i $PrivateKeyPath $ServerUser@$ServerHost"
& $sshCmd "sudo mkdir -p $RemotePath && sudo chown $($ServerUser):$($ServerUser) $RemotePath"

Write-Host "📤 7. Subiendo archivos al servidor..." -ForegroundColor Yellow
scp -i $PrivateKeyPath $zipFile "$breakingns:192.168.1.100:/tmp/"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ ERROR: Falló la subida de archivos" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Archivos subidos" -ForegroundColor Green

Write-Host "📂 8. Descomprimiendo en servidor..." -ForegroundColor Yellow
& $sshCmd @"
cd $RemotePath && 
sudo tar -xzf /tmp/$zipFile && 
sudo chown -R $($ServerUser):$($ServerUser) . && 
sudo chmod +x scripts/*.sh &&
rm /tmp/$zipFile
"@

Write-Host "🧹 9. Limpiando archivos temporales..." -ForegroundColor Yellow
Remove-Item -Recurse -Force $tempDir
Remove-Item $zipFile

Write-Host "📋 10. Creando script de inicio en servidor..." -ForegroundColor Yellow
$startScript = @'
#!/bin/bash
# start-production.sh - Script para iniciar el proyecto en producción

echo "=================================="
echo "🚀 INICIANDO SOMOSTIENDAMAS"
echo "=================================="
echo "Fecha: $(date)"
echo "Directorio: $(pwd)"
echo ""

echo "🔧 1. Verificando Docker..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "❌ Docker no está corriendo"
    exit 1
fi

echo "✅ Docker está funcionando"

echo "📦 2. Construyendo imágenes..."
docker-compose build --no-cache

echo "🗄️  3. Iniciando servicios..."
docker-compose up -d

echo "⏳ 4. Esperando que los servicios estén listos..."
sleep 10

echo "📊 5. Estado de los servicios:"
docker-compose ps

echo "📝 6. Últimos logs:"
docker-compose logs --tail=20 app

echo ""
echo "=================================="
echo "✅ DESPLIEGUE COMPLETADO"
echo "=================================="
echo "🌐 Aplicación disponible en: https://$HOSTNAME:8443"
echo "📊 Monitorear logs: docker-compose logs -f app"
echo "🛑 Detener servicios: docker-compose down"
echo "=================================="
'@

# Subir script de inicio al servidor
$startScript | Out-File -FilePath "start-production.sh" -Encoding UTF8
scp -i $PrivateKeyPath "start-production.sh" "$breakingns:192.168.1.100:$RemotePath/scripts/"
Remove-Item "start-production.sh"

& $sshCmd "chmod +x $RemotePath/scripts/start-production.sh"

Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "✅ DESPLIEGUE COMPLETADO" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host "📁 Archivos en servidor: $RemotePath"
Write-Host "🚀 Para iniciar: ssh -i $PrivateKeyPath $ServerUser@$ServerHost"
Write-Host "   cd $RemotePath && ./scripts/start-production.sh"
Write-Host ""
Write-Host "📊 Comandos útiles:"
Write-Host "   Ver logs: docker-compose logs -f app"
Write-Host "   Detener: docker-compose down"
Write-Host "   Estado: docker-compose ps"
Write-Host "==================================" -ForegroundColor Green