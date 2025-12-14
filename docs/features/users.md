# Users Module

The Users module manages customer profiles linked to external identity providers (Keycloak). It handles profile
creation, retrieval, and address management while ensuring email uniqueness across the platform.

## Key Features

- **Profile Management**: Create, read, and update user profiles
- **External Identity Integration**: Links profiles to Keycloak subjects without exposing implementation details
- **Address Management**: Store and update default delivery addresses
- **Email Uniqueness**: Enforces unique email addresses across all users

## Domain Model

### User

The core entity representing a customer profile.

- **Id**: Unique internal identifier (UUID-based)
- **External Subject**: Reference to the identity provider subject (implementation detail, not exposed in API)
- **Name**: User's display name
- **Email**: Unique email address with format validation
- **Default Address**: Optional delivery address

### Address

A value object representing a physical address.

- **Street**: Street name
- **House Number**: Building/unit number
- **Postal Code**: Postal/ZIP code
- **City**: City name
- **Country**: Country (from common-country module)

## API Usage

All endpoints require authentication. The user's identity is derived from the JWT token (subject claim).

### Create Profile

`POST /api/v1/users`

Creates a new user profile linked to the authenticated user's identity.

**Request:**

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "address": {
    "street": "Kalverstraat",
    "houseNumber": "1",
    "postalCode": "1012 NX",
    "city": "Amsterdam",
    "countryCode": "NETHERLANDS"
  }
}
```

**Response (201 Created):**

```json
{
  "id": "USER-abc123",
  "name": "John Doe",
  "email": "john@example.com",
  "defaultAddress": {
    "street": "Kalverstraat",
    "houseNumber": "1",
    "postalCode": "1012 NX",
    "city": "Amsterdam",
    "countryCode": "NETHERLANDS"
  }
}
```

**Errors:**

- `409 Conflict`: Email already exists or user already has a profile
- `400 Bad Request`: Invalid country code or validation failure

### Get Profile

`GET /api/v1/users`

Retrieves the profile for the authenticated user.

**Response (200 OK):**

```json
{
  "id": "USER-abc123",
  "name": "John Doe",
  "email": "john@example.com",
  "defaultAddress": {
    "street": "Kalverstraat",
    "houseNumber": "1",
    "postalCode": "1012 NX",
    "city": "Amsterdam",
    "countryCode": "NETHERLANDS"
  }
}
```

**Errors:**

- `404 Not Found`: User profile does not exist

### Update Address

`PUT /api/v1/users/address`

Updates the default delivery address.

**Request:**

```json
{
  "street": "Alexanderplatz",
  "houseNumber": "1",
  "postalCode": "10178",
  "city": "Berlin",
  "countryCode": "GERMANY"
}
```

**Response (200 OK):**

```json
{
  "id": "USER-abc123",
  "name": "John Doe",
  "email": "john@example.com",
  "defaultAddress": {
    "street": "Alexanderplatz",
    "houseNumber": "1",
    "postalCode": "10178",
    "city": "Berlin",
    "countryCode": "GERMANY"
  }
}
```

**Errors:**

- `404 Not Found`: User profile does not exist
- `400 Bad Request`: Invalid country code or validation failure

## Error Responses

All errors follow RFC 7807 Problem Details format:

```json
{
  "type": "urn:problem:user:email-already-exists",
  "title": "Email Already Exists",
  "status": 409,
  "detail": "Email 'john@example.com' is already registered"
}
```

### Error Types

| Type                                    | Status | Description                |
|-----------------------------------------|--------|----------------------------|
| `urn:problem:user:not-found`            | 404    | User profile not found     |
| `urn:problem:user:email-already-exists` | 409    | Email already registered   |
| `urn:problem:user:already-exists`       | 409    | User already has a profile |
| `urn:problem:user:invalid-country`      | 400    | Invalid country code       |
| `urn:problem:user:validation-failed`    | 400    | General validation error   |

## Supported Countries

The following country codes are supported:

- `NETHERLANDS`
- `GERMANY`
- `BELGIUM`
- `FRANCE`

## Security

- All endpoints require a valid JWT token
- User identity is derived from the `sub` claim
- Users can only access and modify their own profile
- No user ID is required in requests - identity is implicit from the token
