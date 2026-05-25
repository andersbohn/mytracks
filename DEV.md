# Agentic MVP for a little project

## Learning some more agentic work

My first pretty successful attempt I wanted to learn Quarkus and used first IDEA Junie for month, until the free version was over. 
Then switched to paid Claude Code.

This repo is public, so I will try not to put too stupid or ugly things here. 

## Rough idea for "mytracks"

Just want to have my Garmin tracks from sport and mountaineering in a place where I can access it more freely. 
And experiment with some cool new features, ideas for analyzing attributes or routes or something geographic based on 
the actual  tracks. 

## Platform like with Quarkus 

Plan is to deploy a little private - but multiuser-prepared - webapp on my german VPS using K3s. And having github CI do this when tagging with a proper version number. 

## Local dev & registration endpoints

Start Postgres via compose, then run the app with the `local` profile (mock auth pre-wired to `andersbohn@gmail.com`):

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Check auth status (returns authenticated user from mock filter):
```bash
curl http://localhost:8080/api/auth/status
```

To test the unauthenticated path, hit the endpoint without the local profile active (or unset `auth.mock-email`):
```bash
curl http://localhost:8080/api/auth/status
# → {"authenticated":false,"user":null,"loginUrl":"/oauth2/authorization/google"}
```

Real Google SSO login (needs valid `OAUTH2_CLIENT_ID` / `OAUTH2_CLIENT_SECRET`):
1. Open `http://localhost:8080/oauth2/authorization/google` in a browser
2. Complete Google login — user is created on first login
3. Redirects to `http://localhost:3000` (configure `app.frontend-url` / `APP_FRONTEND_URL` for other targets)
4. Confirm registration: `curl http://localhost:8080/api/auth/status`