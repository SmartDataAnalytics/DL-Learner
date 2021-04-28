FROM maven:3.6.2-jdk-8

MAINTAINER Gezim Sejdiu <g.sejdiu@gmail.com>

ENV DLLEARNER_VERSION=1.5.0

RUN apt-get update && apt-get install -y openjfx wget
RUN wget https://github.com/SmartDataAnalytics/DL-Learner/releases/download/${DLLEARNER_VERSION}/dllearner-${DLLEARNER_VERSION}.zip

RUN   unzip dllearner-${DLLEARNER_VERSION}.zip \
      && mv dllearner-${DLLEARNER_VERSION} dllearner \
      && rm dllearner-${DLLEARNER_VERSION}.zip
      
WORKDIR dllearner/

CMD ["/bin/bash"]