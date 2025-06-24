FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/SomosTiendaMas-0.1.6.2.jar app.jar
COPY src/main/resources/keys ./keys
ENV DB_URL=jdbc:postgresql://db:5432/somostiendamas
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=nahuel
ENV DB_DRIVER=org.postgresql.Driver
ENV PRIVATE_KEY_PATH=/app/keys/private.pem
ENV PUBLIC_KEY_PATH=/app/keys/public.pem
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]