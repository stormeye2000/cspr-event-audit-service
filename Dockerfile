FROM gradle:jdk17 as gradleimage
COPY . /home/gradle/source
WORKDIR /home/gradle/source
RUN ./gradlew clean build -x test

CMD ["java", "-jar", "build/libs/event-audit-service.jar", "--spring.profiles.active=dev"]
