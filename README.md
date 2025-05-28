
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

---

## POST `/2FA/enable`

Enables Two-Factor Authentication (2FA) for a user after verifying phone and password.

**Request Body (JSON):**

```json
{
  "phone": "string",
  "password": "string"
}
```

**Responses:**

* **202 Accepted**
  OTP sent via SMS to confirm 2FA activation. Response message:

  ```json
  {
    "message": "Confirm the OTP sent to you via SMS to complete the two-factor authentication confirmation"
  }
  ```

* **400 Bad Request**

    * Invalid password
    * 2FA activation already requested (OTP already exists for user)

* **403 Forbidden**
  Attempt to enable 2FA for an unverified account.

* **404 Not Found**
  User with provided phone not found.

---

## PATCH `/2FA/verify?otp={otp}`

Verifies the OTP to complete the Two-Factor Authentication activation.

**Query Parameter:**

* `otp` — the One-Time Password sent via SMS.

**Responses:**

* **202 Accepted**
  Returns access and refresh tokens after successful OTP verification:

  ```json
  {
    "token": "jwt-token",
    "refreshToken": "refresh-token"
  }
  ```

* **403 Forbidden**
  OTP validation failed (e.g., expired or invalid OTP).

* **404 Not Found**
  OTP or User not found.

---

---

## POST `/login`

Authenticate a user using phone number and password.

**Request Body (JSON):**

```json
{
  "phone": "string",
  "password": "string"
}
```

**Responses:**

* **200 OK**

* If Two-Factor Authentication (2FA) is **disabled**, returns JWT access token and refresh token:

  ```json
  {
    "token": "jwt-token",
    "refreshToken": "refresh-token"
  }
  ```
* If 2FA is **enabled**, initiates OTP sending and returns a message indicating that 2FA verification is required:

  ```json
  {
    "message": "Two-factor authentication code sent."
  }
  ```

* **400 Bad Request**
  Wrong password or invalid input.

* **403 Forbidden**
  Attempt to login with an unverified account.

* **404 Not Found**
  User with provided phone number not found.

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