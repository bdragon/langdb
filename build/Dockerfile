FROM postgres:17-alpine

ENV POSTGRES_DB=langdb
ENV POSTGRES_USER=langdb

ENV LC_ALL=en_US.UTF-8
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8

ENV TZ=UTC

COPY ./data/sql/langdb.sql /docker-entrypoint-initdb.d/
