FROM openjdk:7

RUN apt-get -y update && apt-get -y install ant

ENV APPPATH /vagrant

COPY . $APPPATH
WORKDIR $APPPATH

RUN ant -Dant.build.javac.target=1.5 -Dant.build.javac.source=1.5 clean all
RUN rm -rf cls

EXPOSE 8574 8668

CMD java -cp $APPPATH/lib:$APPPATH/lib/* -server org.snipsnap.server.AppServer

# OK needs debugging