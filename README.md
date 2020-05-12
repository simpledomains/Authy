# Authy

![Docker Pulls](https://img.shields.io/docker/pulls/sealife/forward-auth-server?style=for-the-badge)
![Docker Pulls](https://img.shields.io/docker/stars/sealife/forward-auth-server?style=for-the-badge)

![Docker Pulls](https://img.shields.io/microbadger/layers/sealife/forward-auth-server/develop?style=for-the-badge)
![Docker Pulls](https://img.shields.io/microbadger/image-size/sealife/forward-auth-server/develop?style=for-the-badge)

## Features

- CAS 2.0 (without Proxy-Feature)
- Forward-Auth Endpoint (to be used with Traefik)

## Installation

### Docker

See `docker-compose.example.yml` for an example.

## Configuration

The Software can fully be customized by environment variables set in the `docker-compose.yml`.

| Variable | Datatype allowed | Description |
| -------- | ---------------- | ----------- |
| APP_SECRETS_JWT | String | Sets the key to be used when signing the CAS Token |
| CAS_SYSTEM_DOMAIN | String (URL) | Sets the URL to the Authy Service e.g.: `https://auth.example.com` |
| CAS_COOKIE_DOMAIN | String | Sets the domain used for the cookie (equals to @-Record of your domain) |
| SPRING_DATASOURCE_URL | String | Sets the JDBC-Url to the Database, if empty, a local H2 in memory DB will be used! |
| SPRING_DATASOURCE_USERNAME | String | Sets the username to be used when accessing the database |
| SPRING_DATASOURCE_PASSWORD | String | .... |
| SPRING_JPA_HIBERNATE_DDL_AUTO | one of(`update`, `create`) | Creates the DDL scripts to create the Database tables. **ATTENTION**: User requires DDL permissions on the database. |
| SPRING_JPA_SHOW_SQL | Boolean | Logs all SQL Statements to STDOUT |
| CAS_FRONTEND_DARKMODE | Boolean | Can be set to `true` to enable the Dark-Mode |

## Usage

### As a Forward-Auth Server in Traefik 1.7.x

To use Authy as a Forward-Auth Server in Traefik 1.7.x you simply need to add a label to your applications:

```
traefik.frontend.auth.forward.address=https://auth.example.com/auth
```

### As a Forward-Auth Server in Traefik 2.x

TBD

### As a CAS Server in any Software supporting CAS 1.0/2.0/3.0

You can simply use ```https://auth.example.com/cas``` as your CAS endpoint.

In some system's your asked to put in protocol or port, host and path.
If this is the case, you do this: 

- protocol: https
- port: 443
- host: auth.example.com
- path: /cas

## Client-Certificate Authentication

The client certificate authentication is used to authenticate without a username or password - just with a SSL certificate installed locally.
Before using this method you need to create your own CA certificate.
After some configuration your users should be able to manage user certificates from the UI.

### Create CA

1. Create a CA private key.
    ```bash
   openssl genrsa -aes256 -out ca.pem 8192
    ```
2. Create the public CA certificate
    ```bash
   openssl req -x509 -new -nodes -extensions v3_ca \
        -key ca.pem \
        -days 3096 \
        -out ca.pub.pem \
        -sha512
    ```

### Configuration

To enable the CC-Authentication-Workflow you need to specify the location of the public-key of your CA in ``server.ssl.client-auth-ca-cert``.

```yaml
server:
  ssl:
    client-auth-ca-cert: /data/ca.pub.pem
cas:
  frontend:
    clientCertAuth: true    
```