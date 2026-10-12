# OAuth2_Basic
A Spring Boot 4 application that combines:
- **Local username/password authentication**
- **JWT access tokens**
- **Refresh token rotation in HTTP-only cookies**
- **Google OIDC user login support**
- **Keycloak / JWT resource-server style protection for secured API endpoints**
- **MySQL persistence for users and refresh tokens**
This project is built with Spring Security and is intended as a learning/demo application for comparing and combining different authentication flows.
## Features
- Login with local username/password
- Issue JWT access tokens after login
- Store refresh tokens in an HTTP-only cookie
- Refresh access tokens using refresh-token rotation
- Revoke refresh tokens on logout
- Load and map Google OIDC users
- Persist OIDC users in MySQL
- Protect endpoints with roles such as `USER` and `ADMIN`
- Return custom JSON responses for `401` and `403`
## Tech stack
- Java 25
- Spring Boot 4.1.1
- Spring Security
- Spring Data JPA
- MySQL
- OAuth2 Client
- OAuth2 Resource Server
- JJWT
- Lombok
## Project structure
Key classes in the project:
- `src/main/java/com/example/OAuth2_Basic/controllers/AuthController.java`
  - Local login, refresh, and logout endpoints
- `src/main/java/com/example/OAuth2_Basic/controllers/TestController.java`
  - Sample protected endpoints and OIDC user access
- `src/main/java/com/example/OAuth2_Basic/controllers/SecurityTestController.java`
  - Keycloak/JWT protected API test endpoint
- `src/main/java/com/example/OAuth2_Basic/service/CustomOidcUserService.java`
  - Loads Google OIDC users and stores them locally
- `src/main/java/com/example/OAuth2_Basic/service/EndUserService.java`
  - Creates or updates OIDC users in the database
- `src/main/java/com/example/OAuth2_Basic/service/RefreshTokenService.java`
  - Refresh-token lifecycle management
- `src/main/java/com/example/OAuth2_Basic/security/JwtService.java`
  - JWT creation and validation helpers
- `src/main/java/com/example/OAuth2_Basic/security/JwtAuthenticationFilter.java`
  - Reads Bearer access tokens from requests
- `src/main/java/com/example/OAuth2_Basic/config/SecurityConfig.java`
  - Spring Security configuration
## Authentication flows
### 1. Local username/password + JWT
1. Client posts username and password to `/auth/login`
2. Spring authenticates the user
3. The app returns:
   - a JWT access token in the response body
   - a refresh token in an HTTP-only cookie
4. Client uses the access token in the `Authorization: Bearer ...` header
5. When the access token expires, the client calls `/auth/refresh`
6. `/auth/logout` revokes the refresh token and clears the cookie
### 2. Google OIDC login
The project includes Google OIDC support via `spring-boot-starter-oauth2-client` and `CustomOidcUserService`.
Typical flow:
1. User opens the Google login entry point
2. Spring redirects the user to Google
3. Google authenticates the user
4. Spring receives the OIDC user information
5. `CustomOidcUserService` maps the Google profile to a local user record
### 3. Keycloak / resource-server JWT flow
The project also contains JWT resource-server style configuration and test endpoints for Keycloak-style access tokens.
Typical flow:
1. Client obtains a JWT from Keycloak or another issuer
2. Client sends the JWT in the `Authorization` header
3. Secured endpoints validate the token and read claims
## API endpoints
### Local auth
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
### Public / sample endpoints
- `GET /api/public`
- `GET /api/user`
- `GET /api/admin`
- `GET /api/me`
### OIDC / Google login
- `GET /oauth2/authorization/google`
- `GET /login/oauth2/code/google`
### Keycloak / JWT secured test endpoint
- `GET /auth_oidc/api/secure`
## Configuration
### `application.yaml`
The project uses MySQL, JWT secret settings, Google OAuth2 client registration, and logging options.
Example keys you need to configure:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oauth2_basic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: rootpassword
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
jwt:
  secret: ${JWT_SECRET}
  expiration: 30000
  refresh-expiration: 120000
```
## Prerequisites
- Java 25
- Maven
- MySQL server running locally
- A MySQL database named `oauth2_basic`
- Google OAuth client credentials if you want to test OIDC login
- Keycloak or another JWT issuer if you want to test the resource-server flow
## Database setup
Create the database first:
```sql
CREATE DATABASE oauth2_basic;
```
The app uses JPA to create and update tables automatically based on entities.
## Google OAuth2 setup
If you want to test Google login locally, create a Google OAuth client and register this redirect URI:
```text
http://localhost:8080/login/oauth2/code/google
```
Set these environment variables before starting the app:
```powershell
$env:google_client_id="your-google-client-id"
$env:google_client_secret="your-google-client-secret"
$env:JWT_SECRET="your-long-random-secret"
```
## Running the application
From the project root:
```powershell
mvn clean spring-boot:run
```
Or build first:
```powershell
mvn clean package
java -jar target/OAuth2_Basic-0.0.1-SNAPSHOT.jar
```
## Debugging
If you want to inspect security flow, enable debug logging:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
```
## Notes
- The project combines multiple auth styles for learning purposes.
- JWT access tokens are used for API access.
- Refresh tokens are stored in HTTP-only cookies.
- Google OIDC support is handled separately from the local JWT login.
- Keycloak/JWT support is available for secured API testing.
## License
No license has been specified.
