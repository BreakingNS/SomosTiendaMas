FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="nahuel@somostiendamas.com"
LABEL description="SomosTiendaMas - E-commerce Application"
LABEL version="0.1.6.4"

WORKDIR /app

# ✅ AGREGAR: Copiar JAR compilado
COPY target/SomosTiendaMas-0.1.6.4.jar app.jar

# ✅ AGREGAR: Copiar claves SSL y JWT
COPY src/main/resources/keys/ /app/keys/

# ✅ AGREGAR: Crear usuario no-root
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 --ingroup spring spring && \
    chown -R spring:spring /app

USER spring

# ✅ AGREGAR: Variables de entorno
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8443

# ✅ AGREGAR: Exponer puerto HTTPS
EXPOSE 8443

# ✅ CORREGIR: Entry point simplificado
ENTRYPOINT ["java", "-jar", "app.jar"]