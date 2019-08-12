FROM maven:3.3.9-jdk-8

MAINTAINER Gezim Sejdiu <g.sejdiu@gmail.com>

ENV DLLEARNER_VERSION=1.3.0

RUN apt-get update && apt-get install -y openjfx wget
#RUN git clone https://github.com/SmartDataAnalytics/DL-Learner.git /DL-Learner
RUN wget https://github.com/SmartDataAnalytics/DL-Learner/releases/download/${DLLEARNER_VERSION}/dllearner-${DLLEARNER_VERSION}.zip
#RUN cd /DL-Learner && git checkout develop
#RUN cd /DL-Learner && mvn clean install -Dmaven.test.skip=true
#RUN cd /dl-learner && ./buildRelease.sh
#RUN cp interfaces/target/dl-learner.jar /dl-learner.jar

RUN   unzip dllearner-${DLLEARNER_VERSION}.zip \
      && mv dllearner-${DLLEARNER_VERSION} dllearner \
      && rm dllearner-${DLLEARNER_VERSION}.zip
      
WORKDIR dllearner/

#CMD ["java", "-Xmx2G", "-jar", "/dl-learner.jar", "-s", "/examples/father.conf"]
CMD ["/bin/bash"]