FROM maven:3.3.9-jdk-8

MAINTAINER Gezim Sejdiu <g.sejdiu@gmail.com>

ENV DLLEARNER_VERSION=1.3.0

RUN apt-get update && apt-get install -y openjfx wget
RUN wget https://github.com/SmartDataAnalytics/DL-Learner/releases/download/${DLLEARNER_VERSION}/dllearner-${DLLEARNER_VERSION}.zip

RUN   unzip dllearner-${DLLEARNER_VERSION}.zip \
      && mv dllearner-${DLLEARNER_VERSION} dllearner \
      && rm dllearner-${DLLEARNER_VERSION}.zip
      
WORKDIR dllearner/

CMD ["/bin/bash"]