FROM 064267045885.dkr.ecr.ap-south-1.amazonaws.com/dev:alpine-node-java-yran-apm
#FROM 192.168.22.41:5000/alpine-openjre-confd-apm:1
#FROM 192.168.22.41:5000/openjdk-apm:1
#FROM 15.206.17.129:5000/alpine-openjre-8-apm:1

MAINTAINER durga.prasad@gaiansolutions.com

ADD docker/startup.sh /

RUN mkdir -p /etc/conf/gaian/pushnotification-service

EXPOSE 8080
#CMD [""]
ENTRYPOINT ["sh", "/startup.sh"]

# add the jar file
ADD src/main/resources/logback-spring.xml /etc/conf/gaian/pushnotification-service/logback-spring.xml

ADD target/pushnotification-service.jar pushnotification-service.jar
