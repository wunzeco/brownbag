FROM python:2.7

MAINTAINER Ogonna Iwunze 'oiwunze@equalexperts.com'

WORKDIR /opt
COPY    . /opt
ENV     ES_CONN eshost
RUN     pip install -r requirements.txt

ENTRYPOINT ["python", "data_loader.py"]
EXPOSE     9090

