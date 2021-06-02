FROM alpine:3.13

VOLUME /mnt/data

WORKDIR /app

RUN apk update && \
    apk add --no-cache bash curl && \
    rm -rf /var/cache/apk/*

COPY ./download.sh /app/

CMD ["/bin/bash", "/app/download.sh"]
