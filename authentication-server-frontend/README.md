# Traefik Forward Authentication Server

## Features

- Forward-Auth Server for Traefik (https://traefik.io)
- CAS1.0/CAS2.0 Server (without Proxy)

## Installation

### 1. Download

You can simply download a pre-compiled binary from the Release page.

#### 1.1. Docker

Instead of downloading a binary and run it, you can use the Docker image from the docker hub.

### 2. Configuration

You need to define at least 3 environment variables (either through environment var, .env-file or defining them in settings.yml).

- `JWT_SECRET` - a crypt key to sign the JWT tokens generated
- `COOKIE_SECRET` - a crypt key to encrypt the cookie generated
- `COOKIE_DOMAIN` - the domain of the authentication server

You can use a database for dynamic users and routes by defining these vars:

- `DATABASE_ENABLED` - set this to `true` to use a database
- `DATABASE_DRIVER` - sets the database driver (`postgres`, `mysql` or `mariadb`)
- `DATABASE_NAME` - sets the name of the database
- `DATABASE_HOST` - sets the host of the database server
- `DATABASE_PORT` - sets the port of the database server (defaults to the default DBMS port)
- `DATABASE_USER` - sets the user to access the database
- `DATABASE_PASS` - sets the user to access the database
- `DATABASE_SCHEMA` - sets the name of the database

The Database `CREATE TABLE` statements can be found in `db_DRIVER.sql`.

#### 2.1. settings.yml

You can create (or rename the example `settings.sample.yml`) a `settings.yml` and create static users and routing roles.
Take a look at the example.

There is also a way to define environment variables from there. Take a look at the sample.

#### 2.2. .env

Just define them in this way:

```
KEY=VALUE

COOKIE_SECRET=HndjNCKödoJCNyxnSwD
[...]
```

### 2.3. Basic Configuration

You can disable and enable certain things by setting these to either true or false:

- `DISABLE_MD5` - (def. `false`) disables apache's MD5 based password check.
- `DISABLE_CRYPT` - (def `false`) disables apache's crypt(3) based password check.
- `ENABLE_DEBUG` - (def `false`) enables Debug logging (!! PASSWORDS WILL BE LOGGED IF ENABLED!)
- `DATABASE_ENABLED` - (def `false`) enables the database integration.

### 3. Usage as Forward-Auth Server with Traefik

For this assume this application is reachable through `auth.example.com`.

Just add the label `traefik.frontend.auth.forward.address=https://auth.example.com/auth`.

### 4. Usage as CAS Server

You can simply use the following endpoints in your CAS configuration:

- `https://auth.example.com/cas/login` - Login Endpoint
- `https://auth.example.com/cas/validate` - Validate Endpoint (CAS1.0)
- `https://auth.example.com/cas/serviceValidate` - Validate Endpoint (CAS2.0)
- `https://auth.example.com/cas/logout` - Logout from CAS (revokes the Cookie)

#### 4.1. CAS in Gitlab

For Gitlab you need to modify your `gitlab.rb` and add the following:

```
gitlab_rails['omniauth_providers'] = [
  {
      "name"=> "cas3",
      "label"=> "LOGIN_BUTTON_NAME",
      "args"=> {
          "url"=> 'https://auth.example.com',
          "login_url"=> '/cas/login',
          "service_validate_url"=> '/cas/validate',
          "logout_url"=> '/cas/logout'
      }
  }
]

gitlab_rails['omniauth_allow_single_sign_on'] = true
gitlab_rails['omniauth_block_auto_created_users'] = false
```

see https://docs.gitlab.com/ee/integration/cas.html for more information.

### 5. Building and Packaging

#### 5.1. Requirements

- Node 10 (there is a pkg-related bug in Node12)

#### 5.2. Preparation

Install the dependencies by running `npm install`

#### 5.3. Building the Frontend

Run `npm run build` to build the VueJS frontend.

Output directory will be `dist/`

#### 5.4. Packaging the Binary

First of all, you need to install pkg (globally) by running `npm install -g pkg`.
You can skip this if you already have pkg installed.

After installing you can simply run `pkg .`.

This will create a binary for the 3 major platforms (windows, macos and linux).