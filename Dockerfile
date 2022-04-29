FROM maven:3.6.3-jdk-11 as builder

COPY ./ /app

WORKDIR /app

RUN mvn clean package -Pproduction

FROM openjdk:11
COPY --from=builder /app/target /app
WORKDIR /app
RUN ls
CMD [ "java", "-jar", "spark-0.0.1.jar" ]