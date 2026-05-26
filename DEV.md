# mytracks — dev notes

## Rough idea

Garmin tracks from sport and mountaineering in a place I can access freely, with some analysis and geographic features down the line.

## Stack

Spring Boot backend + React (Vite) frontend, deployed on a German VPS using K3s. GitHub CI pushes on version tags.

---

## Local development

### Backend

Start Postgres, then run with the `local` profile (mock auth pre-wired to `andersbohn@gmail.com`, context path `/mytracks`):

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Verify:
```bash
curl http://localhost:8080/mytracks/api/auth/status
# → {"authenticated":true,"user":{...},"loginUrl":null,"googleClientId":"local-mock"}
```

### Frontend (mytracks-ui)

```bash
cd frontend
npm install   # first time only
npm run dev
```

Open **http://localhost:5173/** in a browser.

- Mock auth is active → the app skips the Google Sign-In button and goes straight to the profile page.
- Vite proxies all `/mytracks/*` requests to `localhost:8080` — no CORS config needed.
- The Google Sign-In button only renders when `googleClientId` contains `.apps.googleusercontent.com` (i.e. not in local dev).

### Login page (local)

Not normally reachable locally because mock auth makes every request authenticated. To see it, temporarily blank out `auth.mock-email` in `application-local.properties` and restart the backend. The frontend will then show the Google button (or a "local dev" message since the client ID is `local-mock`).

---

## Production (VPS / k3s)

| URL | What |
|-----|------|
| `https://vpsh1.andersbohn.dk/mytracks-ui/` | React frontend |
| `https://vpsh1.andersbohn.dk/mytracks/api/auth/status` | Auth status (public) |
| `https://vpsh1.andersbohn.dk/mytracks/api/me` | Current user (requires auth) |

### Deploy

CI builds and deploys both images on every version tag:

```bash
git tag v0.x.y && git push --tags
```

### First-time / manual deploy of frontend

If the `mytracks-ui` pod and ingress path are not yet applied (e.g. after the initial frontend addition), run on the VPS:

```bash
# pull and apply the frontend deployment
kubectl apply -f ~/deploy/mytracks/yaml/frontend-deployment.yaml -n mytracks

# update ingress to add /mytracks-ui path
kubectl apply -f ~/deploy/mytracks/yaml/traefik-ingress.yaml

# verify pod comes up
kubectl get pods -n mytracks
```

If the yamls are not on the VPS yet, scp them first:
```bash
scp yaml/frontend-deployment.yaml yaml/traefik-ingress.yaml <user>@<vps>:~/deploy/mytracks/yaml/
```
Then substitute the placeholders before applying:
```bash
sed -i "s/OWNER_PLACEHOLDER/<owner>/g" ~/deploy/mytracks/yaml/frontend-deployment.yaml
sed -i "s/TAG_PLACEHOLDER/<tag>/g" ~/deploy/mytracks/yaml/frontend-deployment.yaml
```

### Google Sign-In flow (production)

1. Open `https://vpsh1.andersbohn.dk/mytracks-ui/`
2. Click **Sign in with Google** — completes OAuth2 in a popup
3. On success, the app calls `POST /mytracks/api/auth/google` with the credential
4. User is registered (first time) or looked up (returning) — session cookie set
5. Profile page loads

### Bearer token (API / scripts)

```bash
# must be logged in as personal Google account, not a service account
gcloud auth login
TOKEN=$(gcloud auth print-identity-token \
  --audiences="82072461507-5uh5pjnqglvb9hfgaeqhvitf2n8eb6lk.apps.googleusercontent.com")
curl -H "Authorization: Bearer $TOKEN" \
  https://vpsh1.andersbohn.dk/mytracks/api/me
```

Note: `gcloud auth print-identity-token` uses whichever account is active. Run `gcloud config list` to confirm it is your personal account, not a service account — service account tokens will register a separate user with the service account email.
