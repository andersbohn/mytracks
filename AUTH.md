# Authentication

## Local development (no Google SSO)

Set `auth.mock-email` in `application-local.properties` (already done):

```properties
auth.mock-email=you@example.com
```

Run:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Every request is automatically authenticated as the configured email — no token or cookie needed.

### 1b — Local GET

```sh
curl http://localhost:8080/mytracks/api/me
```

---

## Production (VPS / k3s with Google OAuth2)

Prerequisites:
- k8s secret `mytracks-secret` with: `oauth-client-id`, `oauth-client-secret`, `frontend-url`, `cors-allowed-origins`, `db-username`, `db-password`
- Ingress at `https://vpsh1.andersbohn.dk/mytracks`
- `auth.mock-email` must be empty (default in `application.properties`)

### 2a — Register / login with Google (creates a session)

Used by browser clients or any client that wants to reuse a session cookie.

1. Obtain a Google ID token — e.g. via Google Sign-In SDK in the browser, or:
   ```sh
   gcloud auth print-identity-token --audiences=<oauth-client-id>
   ```
2. POST the token to register/login:
   ```sh
   curl -c cookies.txt -X POST https://vpsh1.andersbohn.dk/mytracks/api/auth/google \
     -H "Content-Type: application/json" \
     -d '{"credential": "<google-id-token>"}'
   ```
   Response: `200 OK` with user details. A `Set-Cookie: JSESSIONID=...` header is returned.

3. Use the session cookie for subsequent requests:
   ```sh
   curl -b cookies.txt https://vpsh1.andersbohn.dk/mytracks/api/me
   ```

Registration is idempotent — the same Google account always maps to the same user record.

### 2b — Stateless API call with Bearer token

For scripts and CLI use — no session needed, token validated on every request.

```sh
TOKEN=$(gcloud auth print-identity-token --audiences=<oauth-client-id>)
curl -H "Authorization: Bearer $TOKEN" https://vpsh1.andersbohn.dk/mytracks/api/me
```

The user is auto-registered on first call. No session is created or required.

---

## How it works — filter chain

```
Incoming request
  │
  ├─ Authorization: Bearer <token> present?
  │     └─ GoogleBearerTokenFilter
  │           valid token   → authenticate (stateless, no session created)
  │           invalid token → 401, stop
  │
  ├─ auth.mock-email set? (local dev only)
  │     └─ MockAuthFilter
  │           → authenticate as configured email, no token needed
  │
  └─ JSESSIONID cookie present?
        └─ Spring session filter
              → restore auth from session
              (session created by POST /api/auth/google or OAuth2 redirect)
```

**Public endpoints** (no auth required): `/actuator/health/**`, `/api/auth/status`, `/api/auth/google`
