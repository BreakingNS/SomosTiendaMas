FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="nahuel@somostiendamas.com"
LABEL description="SomosTiendaMas - E-commerce Application Beta"
LABEL version="0.1.6.4"

WORKDIR /app

# Copiar JAR compilado
COPY target/SomosTiendaMas-0.1.6.4.jar app.jar

# Copiar claves SSL y JWT
COPY src/main/resources/keys/ /app/keys/

# Crear directorio de logs
RUN mkdir -p /app/logs

# Crear usuario no-root para seguridad
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 --ingroup spring spring && \
    chown -R spring:spring /app

USER spring

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8443

# Exponer puerto HTTPS
EXPOSE 8443

# Entry point
ENTRYPOINT ["java", "-jar", "app.jar"]