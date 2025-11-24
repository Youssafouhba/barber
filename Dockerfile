# --- Étape 1 : Build (Compilation) ---
# On utilise une image avec Maven pour créer le .jar
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# On package l'application en sautant les tests pour aller plus vite en prod
RUN mvn clean package -DskipTests

# --- Étape 2 : Run (Exécution) ---
# On utilise une image légère juste pour lancer le Java
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# On récupère le .jar généré à l'étape 1
COPY --from=build /app/target/*.jar app.jar

# On expose le port 8080 (interne au conteneur)
EXPOSE 8080

# La commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]