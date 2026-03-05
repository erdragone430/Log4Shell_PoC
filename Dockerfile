# Dockerfile for Log4Shell vulnerable application
# Using Java 8 for better exploit compatibility
FROM maven:3.6.3-jdk-8 AS builder

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application with Java 8
RUN mvn clean package

# Final image - Using Java 8 for vulnerability
FROM openjdk:8u181-jre-alpine

WORKDIR /app

# Copy compiled JAR
COPY --from=builder /app/target/log4shell-poc-1.0-SNAPSHOT.jar /app/vulnerable-app.jar

# Expose application port
EXPOSE 8080

# Start the application with JNDI trust flags
CMD ["java", "-Dcom.sun.jndi.ldap.object.trustURLCodebase=true", "-Dcom.sun.jndi.rmi.object.trustURLCodebase=true", "-jar", "vulnerable-app.jar"]
