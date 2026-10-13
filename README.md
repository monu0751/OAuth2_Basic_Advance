# OAuth2_Basic

Spring Boot application that uses **Google OIDC** for authentication, reads the authenticated user information from the Google **ID token**, creates the application's own **JWT access token**, and then uses that JWT to access secured endpoints.

## What this project does

1. User starts login with Google.
2. Google authenticates the user and returns an **ID token**.
3. The application validates and decodes the OIDC user information.
4. The application generates its **own JWT access token**.
5. The JWT is used to call protected endpoints such as `/api/user` and `/api/admin`.
6. A refresh token is stored in an HTTP-only cookie and can be used to rotate access tokens.

## Tech stack

- Java 25
- Spring Boot
- Spring Security
- Spring Security OAuth2 Client
- JWT (`jjwt`)
- MySQL + Spring Data JPA

## Authentication flow

### Google OIDC login

The application starts the Google login flow through:

```text
/oauth2/authorization/google
```

After successful login:

- Google returns the OIDC user information
- the application reads claims such as:
  - `sub`
  - `name`
  - `email`
  - `picture`
- the application creates or updates its local user record
- the application generates its own JWT access token

### JWT-based API access

The generated JWT access token is then sent in the `Authorization` header:

```text
Authorization: Bearer <access-token>
```

That token is required to access protected endpoints.

## API endpoints

### Public / auth endpoints

- `GET /api/public` - public endpoint
- `POST /auth/login` - login with username/password and return JWT + refresh cookie
- `POST /auth/refresh` - rotate refresh token and issue a new JWT
- `POST /auth/logout` - revoke refresh token and clear cookie
- `GET /oauth2/authorization/google` - start Google login flow

### Secured endpoints

- `GET /api/user` - accessible to `USER` or `ADMIN`
- `GET /api/admin` - accessible only to `ADMIN`

## Configuration

### `application.yaml`

Set your values in `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: OAuth2_Basic

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${google_client_id}
            client-secret: ${google_client_secret}
            scope:
              - openid
              - profile
              - email

  datasource:
    url: jdbc:mysql://localhost:3306/oauth2_basic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: rootpassword
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

jwt:
  secret: ${JWT_SECRET}
  expiration: 30000
  refresh-expiration: 120000
```

### Environment variables

Set these before running the application:

```powershell
$env:google_client_id="your-google-client-id"
$env:google_client_secret="your-google-client-secret"
$env:JWT_SECRET="your-long-random-secret"
```

## Google OAuth2 setup

Create Google OAuth2 credentials in Google Cloud Console and add this redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

Also make sure the OAuth consent screen is configured and your Google account is added as a test user if required.

## Database setup

Create the MySQL database before starting the app:

```sql
CREATE DATABASE oauth2_basic;
```

## Running the application

### Using Maven

```powershell
cd "C:\Users\Abhinav\OneDrive\Documents\GitHub\OAuth2_Basic"
mvn clean compile
mvn spring-boot:run
```

### Verify login flow

Open the Google login entry point in your browser:

```text
http://localhost:8080/oauth2/authorization/google
```

After successful login, the application should generate its own JWT access token and use it for secure API access.

## Example request

```http
GET /api/user HTTP/1.1
Host: localhost:8080
Authorization: Bearer <your-jwt-access-token>
```

## Important notes

- This project uses **Google OIDC** for login.
- It then issues its **own JWT** for API authorization.
- The app is configured to use `SessionCreationPolicy.STATELESS` for JWT-based API security.
- Refresh tokens are stored in an HTTP-only cookie.

## Project structure

- `controllers/` - auth and API controllers
- `config/` - Spring Security and app configuration
- `service/` - JWT, refresh token, and OIDC user services
- `security/` - JWT filter, handlers, and token utilities
- `entity/` - JPA entities
- `repository/` - database repositories

## Troubleshooting

- **401 on `/oauth2/authorization/google`**: permit `/oauth2/**` and `/login/oauth2/**` in `SecurityConfig`.
- **404 after Google login**: configure a valid success URL or controller mapping.
- **`OidcUserService` not found**: ensure `spring-boot-starter-oauth2-client` is in `pom.xml` and reload Maven.
- **Database connection error**: make sure MySQL is running and `oauth2_basic` exists.

## License

No license has been added yet.
