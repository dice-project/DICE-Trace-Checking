FROM ubuntu:14.04
MAINTAINER Francesco Marconi <francesco.marconi@polimi.it>

# Setting the environment
ENV USERHOME  /root
ENV DEBIAN_FRONTEND noninteractive
ENV USER root

WORKDIR /tmp
# Update the repos and install all the used packages
RUN apt-get update && apt-get install -y --force-yes --no-install-recommends \
    curl \
    git \
    python \
    python-dev \
    build-essential \
    && \
    apt-get autoclean && \
    apt-get autoremove && \
    rm -rf /var/lib/apt/lists/*

#installing pip
    WORKDIR /tmp
    RUN curl -k -O https://bootstrap.pypa.io/get-pip.py && \
        python get-pip.py && \ 
        rm get-pip.py

#environment variables for DICE-TraCT
    ENV PROJECT_FOLDER /opt/DICE-Trace-Checking/
    ENV SERVER_CODE_FOLDER ${PROJECT_FOLDER}dicetract/

# get DICE-TraCT from github
   # RUN  git clone https://github.com/dice-project/DICE-Trace-Checking  $PROJECT_FOLDER
# load code from current directory
    ADD . $PROJECT_FOLDER

#load all needed packages from pip
    RUN pip install -r ${PROJECT_FOLDER}requirements.txt

    WORKDIR $SERVER_CODE_FOLDER

    CMD ["python", "dicetractservice.py"]

