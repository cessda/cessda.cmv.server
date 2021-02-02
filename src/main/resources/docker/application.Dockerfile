#
# @code-generation-comment@
#
FROM openjdk:11-jre-slim

RUN apt-get -y update && apt-get install -y wget && rm -rf /var/lib/apt/lists
RUN adduser user
WORKDIR /@project.artifactId@

COPY maven/@project.build.finalName@.jar ./application.jar
COPY entrypoint.sh ./entrypoint.sh
RUN touch ./application.properties

RUN chmod 400 ./application.jar \
 && chmod 400 ./application.properties \
 && chmod 500 ./entrypoint.sh

RUN chown -R user:user ./
USER user:user

HEALTHCHECK CMD exit $(echo $(echo $(wget http://localhost:@server.port@@server.servlet.context-path@/actuator/health -q -O -) | grep -cv UP))
ENV JAVA_OPTS=""
ENTRYPOINT ["/bin/sh", "./entrypoint.sh"]
EXPOSE @server.port@
