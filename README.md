# E-Commerce CI/CD Mini Project

A starter project demonstrating GitHub → Jenkins → Maven tests → SonarQube Quality Gate → Docker → Kubernetes → Prometheus/Grafana.

## Prerequisites
- JDK 17, Maven 3.9+, Git
- Docker Desktop with Kubernetes enabled, or another Kubernetes cluster
- Jenkins with Pipeline, Git, Maven Integration, SonarQube Scanner, Credentials Binding and JUnit plugins
- SonarQube server and a project token
- Docker Hub account (or another image registry)
- kubectl configured for the target cluster
- Prometheus and Grafana (the provided Compose file is a local monitoring starter)

## Run the app locally
```bash
mvn clean verify
mvn spring-boot:run
```
Open `http://localhost:8080/` and `http://localhost:8080/api/products`.
Health: `http://localhost:8080/actuator/health`
Metrics: `http://localhost:8080/actuator/prometheus`

## Configure Jenkins
1. Put this project in a GitHub repository.
2. Configure JDK tool named `jdk17` and Maven tool named `maven3` under Manage Jenkins → Tools.
3. Add a SonarQube server named `SonarQube` under Manage Jenkins → System; configure its token credential.
4. In SonarQube, create project key `ecommerce-store`, define a Quality Gate, and set it as the project gate. For a simple demo, require no new blocker/critical issues and coverage condition only if a coverage tool is configured.
5. In SonarQube Administration → Configuration → Webhooks, add `http://JENKINS_URL/sonarqube-webhook/` (trailing slash matters). Jenkins must be reachable by SonarQube.
6. Add Jenkins username/password credential ID `dockerhub-creds` for the registry.
7. Add Jenkins secret-file credential ID `kubeconfig` containing a kubeconfig that can deploy to your test cluster.
8. Update `IMAGE_NAME` in `Jenkinsfile` to your Docker Hub username/repository.
9. Configure a Multibranch Pipeline or Pipeline job to use the repository's `Jenkinsfile`.

Important: the Jenkins agent must have Docker CLI/daemon access and `kubectl` installed. The sample pipeline uses Linux `sh` steps. For a Windows-only Jenkins agent, convert commands to `bat`/PowerShell and configure Docker/Kubernetes access accordingly.

## Kubernetes deployment
The Jenkins pipeline substitutes the Docker image tag, applies the Deployment and Service, then waits for rollout.
For a manual deployment, replace `IMAGE_PLACEHOLDER` in `k8s/deployment.yaml` with your published image and run:
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl get pods
kubectl get svc ecommerce
kubectl port-forward service/ecommerce 8080:8080
```
Then open `http://localhost:8080/api/products`.

## Monitoring
1. Start monitoring from the `monitoring` folder: `docker compose up -d`.
2. Open Prometheus at `http://localhost:9090` and Grafana at `http://localhost:3000`.
3. Grafana's default local login is usually `admin` / `admin`; change it at first login.
4. In Grafana, add a Prometheus data source URL `http://prometheus:9090` when Grafana runs in the same Compose network.
5. Create panels using queries such as:
   - JVM memory: `jvm_memory_used_bytes`
   - HTTP request count: `http_server_requests_seconds_count`
   - Process CPU: `process_cpu_usage`
   - Availability (target up): `up{job="ecommerce"}`

The target in `prometheus.yml` is an example only. Adjust it to an address reachable from the Prometheus container. Kubernetes networking differs by setup; for reliable monitoring in a cluster, deploy Prometheus using a Kubernetes-compatible chart/operator or add a scrape configuration that can reach the Service/Pods.

## Demonstration checklist
- Commit a code change and push to GitHub.
- Show Jenkins checking out, compiling and running tests.
- Show SonarQube analysis and a passing Quality Gate.
- Temporarily introduce a failing quality condition or test to demonstrate that release stages are skipped.
- Show the Docker image tag in the registry.
- Show Kubernetes pods and successful rollout.
- Open the app and `/actuator/health`.
- Show Prometheus targets and Grafana metric panels.

## Notes
This is a learning starter, not a production-hardened deployment. Use HTTPS, secret management, image scanning, non-root containers, least-privilege Kubernetes RBAC, resource limits, and protected branches for production.
