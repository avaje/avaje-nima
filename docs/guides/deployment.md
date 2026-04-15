# Deployment

How to deploy nima applications to Docker and Kubernetes.

## Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM openjdk:21-slim
WORKDIR /app
COPY target/myapp.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseG1GC"
ENTRYPOINT ["java", "$JAVA_OPTS", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t myapp:latest .
docker run -p 8080:8080 myapp:latest
```

## Kubernetes Deployment

Create `deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: myapp
        image: myapp:latest
        ports:
        - containerPort: 8080
        env:
        - name: SERVER_PORT
          value: "8080"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

Deploy:

```bash
kubectl apply -f deployment.yaml
```

## Environment-Specific Configuration

Use different profiles for each environment:

- `application-dev.yaml` - Development
- `application-staging.yaml` - Staging  
- `application-prod.yaml` - Production

Set via environment variable:

```bash
export CONFIG_PROFILE=prod
java -jar myapp.jar
```

## Health Checks

Implement health endpoints:

```java
@Controller
@Path("/health")
public class HealthController {
  
  @Get
  public HealthResponse health() {
    return new HealthResponse("UP");
  }
}
```

## Next Steps

- Build [native images](native-image.md) for faster startup
- See [troubleshooting](troubleshooting.md)
