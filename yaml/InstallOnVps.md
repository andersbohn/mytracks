## One-time setup

### 1. Create mytracks DB user and database (in existing postgres pod)

```shell
POSTGRES_POD=$(kubectl get pod -l app=postgres -o jsonpath='{.items[0].metadata.name}')
kubectl exec -it $POSTGRES_POD -- psql -U quarkus -d postgres -c \
  "CREATE USER mytracks WITH PASSWORD 'your-password';"
kubectl exec -it $POSTGRES_POD -- psql -U quarkus -d postgres -c \
  "CREATE DATABASE mytracks OWNER mytracks;"
```

### 2. Create k8s secret

```shell
kubectl create secret generic mytracks-secret \
  --from-literal=db-username=mytracks \
  --from-literal=db-password=your-password \
  --from-literal=oauth-client-id=YOUR_GOOGLE_CLIENT_ID \
  --from-literal=oauth-client-secret=YOUR_GOOGLE_CLIENT_SECRET \
  --from-literal=allowed-emails=you@example.com
```

### 3. Register OAuth2 redirect URI in Google Cloud Console

Add to authorized redirect URIs:
```
https://vpsh1.andersbohn.dk/mytracks/login/oauth2/code/google
```

### 4. Deploy app and ingress

```shell
kubectl apply -f yaml/app-deployment.yaml
kubectl apply -f yaml/traefik-ingress.yaml
```

### 5. Verify

```shell
kubectl rollout status deployment/mytracks
curl -I https://vpsh1.andersbohn.dk/mytracks/actuator/health/readiness
```

---

## CI/CD

Deploys automatically when a version tag is pushed:

```shell
git tag 1.0.0
git push origin 1.0.0
```

GitHub Actions builds the Docker image, pushes to GHCR, and applies the deployment on the VPS.

### Required GitHub environment secrets (`vps1`)

| Secret | Description |
|---|---|
| `VPS_HOST` | VPS hostname |
| `VPS_USER` | SSH user |
| `VPS_SSH_KEY` | SSH private key |
| `GHCR_TOKEN` | GitHub token with `write:packages` |

### k3s kubectl access for CI user

```shell
sudo chmod 640 /etc/rancher/k3s/k3s.yaml
sudo chown $USER /etc/rancher/k3s/k3s.yaml
```
