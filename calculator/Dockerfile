# calculator/Dockerfile
# build using the repo gradle wrapper (build only calculator module)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# copy entire repository
COPY . .

# ensure gradle wrapper is executable
RUN chmod +x ./gradlew

# build only the calculator module (skip tests to speed up builds)
RUN ./gradlew :calculator:bootJar --no-daemon -x test

# runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy generated calculator fat jar
COPY --from=build /workspace/calculator/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
