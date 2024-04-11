FROM eclipse-temurin:17-jdk-alpine

ENV APP_HOME /app
#Possibility to set JVM options (https://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html)
#Create base app folder
RUN mkdir $APP_HOME
#Create folder with application logs
##RUN mkdir -p $APP_HOME/src/main/resources/result

WORKDIR $APP_HOME
COPY --from=domensport-app /build/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar ./app.jar"]
