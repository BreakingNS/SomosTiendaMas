# audit-server.ps1 - Script para auditar archivos del servidor antes de la limpieza

Write-Host "==================================" -ForegroundColor Green
Write-Host "🔍 AUDITORÍA DEL SERVIDOR" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host "Fecha: $(Get-Date)"
Write-Host "Usuario: $env:USERNAME"
Write-Host "Servidor: $env:COMPUTERNAME"
Write-Host ""

Write-Host "📦 CONTENEDORES DOCKER:" -ForegroundColor Yellow
Write-Host "----------------------------------"
try {
    docker ps -a --format "table {{.Names}}`t{{.Image}}`t{{.Status}}`t{{.Ports}}"
} catch {
    Write-Host "Docker no está disponible o no hay contenedores" -ForegroundColor Red
}
Write-Host ""

Write-Host "🖼️ IMÁGENES DOCKER:" -ForegroundColor Yellow
Write-Host "----------------------------------"
try {
    docker images --format "table {{.Repository}}`t{{.Tag}}`t{{.Size}}`t{{.CreatedAt}}"
} catch {
    Write-Host "No se pudieron obtener las imágenes Docker" -ForegroundColor Red
}
Write-Host ""

Write-Host "🗂️ VOLÚMENES DOCKER:" -ForegroundColor Yellow
Write-Host "----------------------------------"
try {
    docker volume ls
} catch {
    Write-Host "No se pudieron obtener los volúmenes Docker" -ForegroundColor Red
}
Write-Host ""

Write-Host "📁 ARCHIVOS JAR:" -ForegroundColor Yellow
Write-Host "----------------------------------"
Get-ChildItem -Path C:\ -Recurse -Filter "*.jar" -ErrorAction SilentlyContinue | 
    Where-Object { $_.LastWriteTime -gt (Get-Date).AddDays(-30) } | 
    Select-Object FullName, Length, LastWriteTime | Format-Table
Write-Host ""

Write-Host "📁 DIRECTORIOS DE PROYECTOS:" -ForegroundColor Yellow
Write-Host "----------------------------------"
Get-ChildItem -Path C:\ -Recurse -Directory -ErrorAction SilentlyContinue | 
    Where-Object { $_.Name -like "*tienda*" -or $_.Name -like "*SomosTiendaMas*" } | 
    Select-Object FullName, CreationTime | Format-Table
Write-Host ""

Write-Host "🔌 PUERTOS EN USO (8080, 8443, 5432):" -ForegroundColor Yellow
Write-Host "----------------------------------"
$ports = @(8080, 8443, 5432)
foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Host "Puerto $port está en uso por proceso ID: $($connection.OwningProcess)"
    } else {
        Write-Host "Puerto $port está libre"
    }
}
Write-Host ""

Write-Host "📄 ARCHIVOS DOCKER-COMPOSE:" -ForegroundColor Yellow
Write-Host "----------------------------------"
Get-ChildItem -Path C:\ -Recurse -Name "docker-compose.yml", "docker-compose.yaml" -ErrorAction SilentlyContinue
Write-Host ""

Write-Host "💾 ESPACIO EN DISCO:" -ForegroundColor Yellow
Write-Host "----------------------------------"
Get-WmiObject -Class Win32_LogicalDisk | 
    Select-Object DeviceID, 
                  @{Name="Size(GB)";Expression={[math]::Round($_.Size/1GB,2)}}, 
                  @{Name="FreeSpace(GB)";Expression={[math]::Round($_.FreeSpace/1GB,2)}}, 
                  @{Name="Usage%";Expression={[math]::Round((($_.Size-$_.FreeSpace)/$_.Size)*100,2)}} | 
    Format-Table

Write-Host "🐳 ESPACIO USADO POR DOCKER:" -ForegroundColor Yellow
Write-Host "----------------------------------"
try {
    docker system df
} catch {
    Write-Host "No se pudo obtener información de Docker" -ForegroundColor Red
}

Write-Host "==================================" -ForegroundColor Green
Write-Host "✅ AUDITORÍA COMPLETADA" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
