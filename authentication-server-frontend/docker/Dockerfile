FROM ubuntu:latest

COPY authy-linux /bin/authy-linux
RUN chmod +x /bin/authy-linux
WORKDIR /data

EXPOSE 3000

ENTRYPOINT ["/bin/authy-linux"]