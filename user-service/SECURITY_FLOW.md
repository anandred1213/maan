# Security Flow Documentation

## Overview
The revision microservice uses JWT (JSON Web Token) based authentication with Spring Security for stateless API security.

## Complete Authentication Flow

### 1. User Login (Authentication)

```
Client → POST /auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

↓

AuthController.login()
  ↓
  Validates credentials with UserService
  ↓
  If valid → JwtUtil.generateToken(email)
    ↓
    Creates JWT with:
    - Subject: user email
    - Issued At: current timestamp
    - Expiration: current time + 24 hours
    - Signature: HMAC-SHA with secret key
  ↓
  Returns JWT token to client

Client receives:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Accessing Protected Endpoints (Authorization)

```
Client → GET /users/123
Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

↓

Request enters Spring Security Filter Chain

↓

JwtAuthenticationFilter.doFilterInternal()
  ↓
  Step 1: Extract "Authorization" header
  ↓
  Step 2: Check if header starts with "Bearer "
  ↓
  Step 3: Extract token (remove "Bearer " prefix)
  ↓
  Step 4: JwtUtil.validateToken(token)
    ↓
    Parse token with secret key
    ↓
    Verify signature
    ↓
    Check expiration date
    ↓
    Return true if valid, false otherwise
  ↓
  Step 5: If valid, extract email from token
  ↓
  Step 6: Create Authentication object
    - Principal: user email
    - Credentials: null (stateless)
    - Authorities: empty (can add roles)
  ↓
  Step 7: Set authentication in SecurityContext
  ↓
  Continue to next filter

↓

SecurityFilterChain checks authorization rules
  ↓
  Is user authenticated? YES
  ↓
  Does endpoint require specific role? Check @PreAuthorize
  ↓
  If authorized → Continue to controller

↓

UserController.getUser(id)
  ↓
  Process request
  ↓
  Return response

Client receives response
```

## Security Components

### 1. SecurityConfig
**Purpose**: Configure Spring Security settings

**Key Configurations**:
- Disable CSRF (stateless API)
- Public endpoints: `/auth/**`, `/actuator/**`
- All other endpoints require authentication
- Stateless session management (no server-side sessions)
- Add JWT filter before default authentication filter

### 2. JwtAuthenticationFilter
**Purpose**: Intercept requests and validate JWT tokens

**Responsibilities**:
- Extract JWT from Authorization header
- Validate token signature and expiration
- Extract user information from token
- Set authentication in SecurityContext
- Runs once per request

### 3. JwtUtil
**Purpose**: Handle JWT token operations

**Methods**:
- `generateToken(email)`: Create new JWT token
- `validateToken(token)`: Verify token validity
- `extractEmail(token)`: Get user email from token
- `extractClaims(token)`: Parse token payload
- `isTokenExpired(token)`: Check expiration

### 4. AuthController
**Purpose**: Handle authentication requests

**Endpoints**:
- `POST /auth/login`: Authenticate user and return JWT

## Token Structure

```
JWT Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyLCJleHAiOjE2MTYzMjU0MjJ9.signature

Parts:
1. Header (Base64): {"alg":"HS256","typ":"JWT"}
2. Payload (Base64): {"sub":"user@example.com","iat":1616239022,"exp":1616325422}
3. Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

## Configuration Properties

```properties
# JWT Secret Key (256-bit minimum for HS256)
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437

# Token Expiration (milliseconds) - 24 hours
jwt.expiration=86400000
```

## Security Best Practices Implemented

1. **Stateless Authentication**: No server-side sessions, JWT contains all info
2. **Token Expiration**: Tokens expire after 24 hours
3. **Secure Password Storage**: BCrypt hashing for passwords
4. **HTTPS Required**: Should be used in production
5. **Secret Key Management**: Use environment variables in production
6. **CORS Configuration**: Configurable allowed origins
7. **Role-Based Access**: @PreAuthorize annotations on endpoints

## API Usage Examples

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

Response:
{
  "id": 1,
  "name": "John Doe",
  "email": "user@example.com"
}
```

### Without Token (Unauthorized)
```bash
curl -X GET http://localhost:8080/users/1

Response: 401 Unauthorized
```

## Error Scenarios

| Scenario | Response | Status Code |
|----------|----------|-------------|
| Invalid credentials | Empty body | 401 |
| Missing token | Empty body | 401 |
| Invalid token | Empty body | 401 |
| Expired token | Empty body | 401 |
| Valid token, no permission | Forbidden | 403 |

## Production Security Checklist

- [ ] Use strong JWT secret (minimum 256 bits)
- [ ] Store JWT secret in environment variables
- [ ] Enable HTTPS/TLS
- [ ] Set appropriate token expiration
- [ ] Implement refresh token mechanism
- [ ] Add rate limiting
- [ ] Log authentication attempts
- [ ] Implement account lockout after failed attempts
- [ ] Use secure password requirements
- [ ] Regular security audits
- [ ] Keep dependencies updated
