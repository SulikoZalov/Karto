
---

# Auth API Endpoints

Base URL: `/karto/auth`

---

## POST `/registration`

Registers a new user.

**Request Body (JSON):**
```json
{
  "firstname": "string",
  "surname": "string",
  "phone": "string",
  "email": "string",
  "password": "string",
  "passwordConfirmation": "string",
  "birthDate": "YYYY-MM-DD"
}
```

**Responses:**
- `202 Accepted` — Registration accepted.
- `400 Bad Request` — Invalid input, passwords do not match, or weak password.
- `409 Conflict` — Email or phone already exists.

---

## POST `/oidc`

Authenticates a user via an OIDC ID token.

**Request Headers:**
- `X-ID-TOKEN: string`

**Responses:**
```json
{
  "token": "jwt-token",
  "refreshToken": "refresh-token"
}
```
- `200 OK` — Authentication successful.
- `400 Bad Request` — Invalid token attributes.
- `403 Forbidden` — Invalid ID token.

---

## GET `/resend-otp`

Resends the OTP to the user.

**Query Parameters:**
- `phoneNumber: string`

**Responses:**
- `200 OK` — OTP resent.
- `400 Bad Request` — Invalid phone number format.
- `404 Not Found` — User or OTP not found.

---

## PATCH `/late-verification`

Adds a phone number after initial email registration.

**Request Body (JSON):**
```json
{
  "email": "string",
  "phone": "string"
}
```

**Responses:**
- `202 Accepted` — Phone number accepted for verification.
- `400 Bad Request` — Invalid email or phone format.
- `404 Not Found` — User not found.
- `409 Conflict` — Phone number already used.

---

## PATCH `/verification`

Verifies user registration via OTP.

**Query Parameters:**
- `otp: string`

**Responses:**
- `202 Accepted` — Verification successful.
- `400 Bad Request` — Invalid OTP or already verified.
- `404 Not Found` — OTP or user not found.
- `410 Gone` — OTP expired.

---

## POST `/login`

Authenticates using phone number and password.

**Request Body (JSON):**
```json
{
  "phone": "string",
  "password": "string"
}
```

**Responses:**
```json
{
  "token": "jwt-token",
  "refreshToken": "refresh-token"
}
```
- `200 OK` — Authentication successful.
- `400 Bad Request` — Wrong password or invalid input.
- `404 Not Found` — User not found.

---

## PATCH `/refresh-token`

Refreshes the JWT using a refresh token.

**Request Headers:**
- `Refresh-Token: string`

**Responses:**
```json
{
  "token": "new-jwt-token"
}
```
- `200 OK` — Token refreshed.
- `400 Bad Request` — Empty or expired refresh token.
- `404 Not Found` — Refresh token not found.

---