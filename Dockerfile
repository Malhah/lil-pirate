FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src ./src
COPY res ./res

RUN mkdir -p out && \
    find src -type f -name "*.java" > sources.txt && \
    javac -d out @sources.txt

CMD ["sh", "-c", "echo Build completed successfully && find out -type f | head"]