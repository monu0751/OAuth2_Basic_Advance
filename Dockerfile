# Stage 1: Build phase (Optimises Keycloak configuration)
FROM quay.io/keycloak/keycloak:latest AS builder

# Enable health check and metrics endpoints
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# (Optional) If you want to bundle custom themes or plugins, uncomment below:
# COPY ./my-theme /opt/keycloak/themes/my-theme

RUN /opt/keycloak/bin/kc.sh build

# Stage 2: Runtime phase (Creates the final lightweight image)
FROM quay.io/keycloak/keycloak:latest
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Define default environment variables for credentials
ENV KC_BOOTSTRAP_ADMIN_USERNAME=admin
ENV KC_BOOTSTRAP_ADMIN_PASSWORD=admin123

# Expose Keycloak's default container port
EXPOSE 8080

# Tells Keycloak to start in development mode
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev"]
