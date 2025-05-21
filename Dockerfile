# Use a smaller base image that includes OpenJDK 11
FROM openjdk:11-jdk-slim

LABEL maintainer="b.gamard@sismics.com"

# Set environment variables
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_HOME=/usr/local/openjdk-11 \
    JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Xmx1g" \
    JETTY_VERSION=11.0.20 \
    JETTY_HOME=/opt/jetty \
    DEBIAN_FRONTEND=noninteractive

# Install only required dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        unzip wget tzdata ffmpeg mediainfo procps && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Configure timezone
RUN dpkg-reconfigure -f noninteractive tzdata

# Install Jetty
RUN wget -nv -O /tmp/jetty.tar.gz \
    "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-home/${JETTY_VERSION}/jetty-home-${JETTY_VERSION}.tar.gz" && \
    tar xzf /tmp/jetty.tar.gz -C /opt && \
    mv /opt/jetty* /opt/jetty && \
    useradd jetty -U -s /bin/false && \
    chown -R jetty:jetty /opt/jetty && \
    mkdir /opt/jetty/webapps && \
    chmod +x /opt/jetty/bin/jetty.sh

EXPOSE 8080

# Install the application
RUN mkdir /app && cd /app && \
    java -jar /opt/jetty/start.jar --add-modules=server,http,webapp,deploy

# Add webapp files
ADD docs.xml /app/webapps/docs.xml
ADD docs-web/target/docs-web-*.war /app/webapps/docs.war

WORKDIR /app

# Default command
CMD ["java", "-jar", "/opt/jetty/start.jar"]
