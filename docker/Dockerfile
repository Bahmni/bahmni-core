# Use a base image with Maven and JDK 8
FROM maven:3.8.4-jdk-8 AS build

# Install necessary packages for building
RUN apt-get update && \
    apt-get install -y \
        git \
        ruby \
        ruby-dev \
        build-essential \
        && \
    gem install compass -v 1.0.3 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory for the build

# Copy the Maven project and build
COPY . .
RUN mvn clean install -DskipTests

# Stage 2: Use a minimal base image with Tomcat to deploy the application
FROM openmrs/openmrs-reference-application-distro:latest

# Copy WAR file from the build stage to Tomcat's webapps directory
COPY --from=build bahmnicore-omod/target/*.omod  /usr/local/tomcat/webapps/app.omod

# Remove the default ROOT application to avoid conflicts
#RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Expose the default Tomcat port
EXPOSE 8080

# Start Tomcat server
CMD ["catalina.sh", "run"]
