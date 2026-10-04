# Accounts and login device records

Signup now calls `POST /api/v1/auth/signup` with name, email and password. Login calls
`POST /api/v1/auth/login` with email and password. `GET /api/v1/auth/me` restores the profile;
`POST /api/v1/auth/logout` revokes the session. Passwords require 12–256 characters at signup.
Emails are trimmed and lowercased, with a database uniqueness constraint.

- `user`: user_id, name, email, password_hash, created_at.
- `user_device_info`: id, user_id (foreign key), ip_address, user_agent, device_type,
  mobile, state, country, event_type (SIGNUP/LOGIN), created_at. One row per successful event.
- `user_session`: token_hash, user_id (foreign key), expires_at. Sessions expire after seven days.

Passwords use salted PBKDF2-HMAC-SHA256 with 600,000 iterations. The browser receives
an HttpOnly, SameSite=Strict cookie; only a SHA-256 hash of the random session token is stored.
Profile responses exclude password hashes, session tokens, and device records.
JPA operations and password hashing run on a dedicated bounded scheduler. Password hashing
runs outside database transactions so it does not hold a connection while doing CPU work.

## Configuration

Obtain a GeoLite2 **City** `.mmdb` file from your MaxMind account and keep it updated.
Set `GEOIP_DATABASE_PATH` to its absolute path inside the running server/container.
No IPs are sent to an external service. Restart after replacing the file.
The reader follows [MaxMind's Java database API](https://maxmind.github.io/GeoIP2-java/).
An unset path disables location lookup; a configured unreadable file fails startup.
Private, unmapped, or failed lookups store null state/country without rejecting signup.
IP-derived locations are approximate; VPNs can change the apparent location.

`APP_TRUSTED_PROXIES` is a comma-separated list of exact proxy IP addresses, empty by default.
Configure your reverse proxy to overwrite or append the real connection IP in
`X-Forwarded-For`. Only configured peers are trusted; the chain is walked right to left.
Keep Spring forwarded-header transformation disabled so this code sees the actual peer.
Device type is inferred from User-Agent / Sec-CH-UA-Mobile and is not proof of hardware.
`mobile` denotes a phone; tablets have device_type TABLET, missing device data stays UNKNOWN.

Cookies default to Secure, including deployments with TLS termination at a proxy.
For local HTTP development only, set `APP_SECURE_COOKIE=false`.
Use the same origin for the UI and API (the Angular development proxy already forwards `/api`).

Existing local Hibernate `ddl-auto=update` creates these new tables on startup.
For managed databases, apply `db/sql/001_accounts.sql` before starting the app.
No existing expenses or accounts are migrated or reassigned by this change.

## Scope

This adds accounts and authentication event tracking. Existing expense endpoints still
use their pre-existing shared bearer-token policy and shared expense records. Account
sessions do not authorize expense mutations. Per-user expense ownership is separate work.
No live database was modified during implementation. Before public rollout, configure
request rate limits for signup/login at your reverse proxy and set the device-history
retention period appropriate to your deployment.

## Verification

`mvn -pl web -am test` runs backend tests. `cd ui && npm run build` checks the Angular build.
After database setup, create an account through Get started, reload to restore its profile,
sign out and log in again. Check for one user and two linked device rows with SIGNUP/LOGIN.
Repeat signup with the same email (409), try a wrong password (401), and verify neither
creates another device row. Use a public IP and configured City database to verify location.

## Architecture and capacity

The dependency direction for accounts is `web → core ← db`:

- `web/AccountController` handles HTTP, cookies, error responses and scheduling.
  `DeviceInfoCollector` extracts request information and uses one shared, cached GeoIP reader.
- `core/AccountService` validates input and handles password hashing and session generation.
  It depends on the `AccountDAO` interface and immutable core records, with no JPA or HTTP types.
- `db/AccountDaoImpl` implements the persistence interface. Signup's profile, device event,
  and session commit atomically. Login's device event and session commit atomically.
  A unique database constraint resolves simultaneous signups for the same email.

| Index | Purpose |
| --- | --- |
| `user(user_id)` primary key | Profile identity and foreign-key joins |
| `user(email)` unique | Indexed normalized email lookup and duplicate prevention |
| `user_device_info(user_id, created_at)` | A user's recent login history, ordered by timestamp |
| `user_device_info(created_at)` | Bounded retention cleanup |
| `user_session(token_hash)` primary key | Single-session authentication and revocation |
| `user_session(expires_at)` | Bounded expiration cleanup |
| `user_session(user_id)` | User/session relationship and future user-wide revocation |

The session/profile query joins and projects only profile fields in one query; it does
not load password hashes or device history. No unbounded history endpoint is exposed.
Future history APIs should use cursor pagination and a deterministic timestamp/id order.
Indexes add write cost; do not add indexes for country or device type unless an actual
query needs them. Use `EXPLAIN` on production-like MySQL data to validate access paths.

Configuration (Spring properties; environment-variable forms are also supported):

| Property | Default | Effect |
| --- | --- | --- |
| `app.accounts.workers` | 4 | Maximum concurrent auth workers per instance |
| `app.accounts.queued-per-worker` | 25 | Bounded waiting work per worker; rejection returns HTTP 503 |
| `app.accounts.cleanup-batch-size` | 1000 | Max records per table deleted per cleanup pass (1–1000) |
| `app.accounts.cleanup-delay-ms` | 60000 | Delay between cleanup passes |
| `app.accounts.device-retention-days` | 0 | Zero preserves device history; positive values enable age-based deletion |

Expired sessions are rejected immediately even before cleanup runs. Cleanup uses indexed,
paged ID selection and bounded bulk deletion, avoiding one large delete transaction.
Multiple app instances may select the same expired IDs; repeated deletion is harmless,
but for large fleets run maintenance on one instance to avoid redundant work.

The database-backed sessions work across app instances without sticky sessions. Size the
Hikari connection pool and auth worker count against the database connection budget and
measured hashing cost across all replicas. Keep the queue bounded rather than hiding
saturation behind growing memory use. Use a shared reverse-proxy/gateway rate limiter for
signup/login; the per-instance worker bound is overload protection, not a distributed
abuse limiter. Monitor login latency, HTTP 503 counts, pool utilization, and cleanup backlog.

No capacity number is claimed: this change includes unit and H2 persistence tests, not a
production MySQL concurrency/load benchmark. Before increasing traffic, test on representative
hardware with the real database, proxy chain and GeoLite2 file, including duplicate-email
races, login bursts, session lookup throughput, and cleanup under sustained writes.
