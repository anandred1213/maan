# Refresh Token Implementation - Complete Guide

## Overview
Implemented JWT Access Token + Refresh Token authentication mechanism for enhanced security and user experience.

---

## IMPLEMENTATION SUMMARY

### Token Types:
1. **Access Token (JWT)** - Short-lived (15 minutes)
   - Used for API authentication
   - Stored in memory/localStorage (client-side)
   - Expires quickly for security

2. **Refresh Token** - Long-lived (7 days)
   - Used to get new access tokens
   - Stored in database
   - Can be revoked on logout

---

## FILES CREATED:

### 1. Entity Layer
- **RefreshToken.java** - Database entity for storing refresh tokens
  - Fields: id, token (UUID), userEmail, expiryDate
  - Table: refresh_tokens

### 2. Repository Layer
- **RefreshTokenRepository.java** - JPA repository
  - findByToken(String token)
  - deleteByUserEmail(String userEmail)

### 3. Service Layer
- **RefreshTokenService.java** - Business logic
  - createRefreshToken(userEmail) - Generate new refresh token
  - verifyExpiration(token) - Validate token not expired
  - deleteByUserEmail(userEmail) - Logout functionality

### 4. DTO Layer
- **TokenRefreshRequest.java** - Request DTO for /auth/refresh
- **TokenRefreshResponse.java** - Response DTO with new tokens
- **AuthResponse.java** (MODIFIED) - Now returns both tokens

### 5. Controller Layer
- **AuthController.java** (MODIFIED) - Added new endpoints:
  - POST /auth/refresh - Refresh access token
  - POST /auth/logout - Invalidate refresh token

---

## API ENDPOINTS

### 1. Login (Modified)
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Refresh Token (New)
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 3. Logout (New)
```http
POST /auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}

Response: "Logged out successfully"
```

---

## AUTHENTICATION FLOW

### Initial Login:
```
1. User → POST /auth/login (email, password)
2. Server validates credentials
3. Server generates:
   - Access Token (15 min expiry)
   - Refresh Token (7 days expiry, saved to DB)
4. Server → Returns both tokens
5. Client stores:
   - Access Token in memory/localStorage
   - Refresh Token in secure storage
```

### Using Access Token:
```
1. Client → API Request with Header: Authorization: Bearer <accessToken>
2. JwtAuthenticationFilter validates token
3. If valid → Request proceeds
4. If expired → 401 Unauthorized
```

### Refreshing Access Token:
```
1. Client detects 401 (access token expired)
2. Client → POST /auth/refresh with refresh token
3. Server validates refresh token:
   - Exists in database?
   - Not expired?
4. If valid → Generate new access token
5. Server → Returns new access token + same refresh token
6. Client updates access token
7. Client retries original request
```

### Logout:
```
1. Client → POST /auth/logout with refresh token
2. Server deletes refresh token from database
3. Client clears all tokens
4. User must login again
```

---

## CONFIGURATION

### application.properties
```properties
# Access Token - 15 minutes (900000 milliseconds)
jwt.expiration=900000

# Refresh Token - 7 days (604800000 milliseconds)
jwt.refresh-expiration=604800000
```

### Environment Variables
```bash
JWT_EXPIRATION=900000           # 15 minutes
JWT_REFRESH_EXPIRATION=604800000 # 7 days
```

---

## DATABASE SCHEMA

### refresh_tokens table
```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    INDEX idx_token (token),
    INDEX idx_user_email (user_email)
);
```

---

## CLIENT-SIDE IMPLEMENTATION EXAMPLE

### JavaScript/React Example:
```javascript
// Store tokens after login
const login = async (email, password) => {
  const response = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  const { accessToken, refreshToken } = await response.json();
  
  // Store tokens
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
};

// API call with auto-refresh
const apiCall = async (url, options = {}) => {
  let accessToken = localStorage.getItem('accessToken');
  
  // Try with current access token
  let response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  // If 401, refresh token and retry
  if (response.status === 401) {
    const refreshToken = localStorage.getItem('refreshToken');
    
    const refreshResponse = await fetch('/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    
    if (refreshResponse.ok) {
      const { accessToken: newAccessToken } = await refreshResponse.json();
      localStorage.setItem('accessToken', newAccessToken);
      
      // Retry original request
      response = await fetch(url, {
        ...options,
        headers: {
          ...options.headers,
          'Authorization': `Bearer ${newAccessToken}`
        }
      });
    } else {
      // Refresh failed, redirect to login
      window.location.href = '/login';
    }
  }
  
  return response;
};

// Logout
const logout = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  
  await fetch('/auth/logout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });
  
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.location.href = '/login';
};
```

---

## SECURITY CONSIDERATIONS

### ✅ Implemented:
1. Refresh tokens stored in database (can be revoked)
2. Refresh tokens have expiration (7 days)
3. Access tokens short-lived (15 minutes)
4. UUID for refresh tokens (unpredictable)
5. Logout invalidates refresh token
6. Validation on every refresh request

### 🔒 Additional Recommendations:
1. Use HTTPS in production
2. Store refresh token in httpOnly cookie (more secure than localStorage)
3. Implement token rotation (new refresh token on each refresh)
4. Add device/IP tracking for refresh tokens
5. Implement rate limiting on /auth/refresh
6. Add refresh token family tracking (detect token reuse)

---

## TESTING

### Test Login:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Test Refresh:
```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"550e8400-e29b-41d4-a716-446655440000"}'
```

### Test Logout:
```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"550e8400-e29b-41d4-a716-446655440000"}'
```

---

## BENEFITS

1. **Better Security** - Short-lived access tokens reduce risk
2. **Better UX** - Users don't need to login frequently
3. **Revocable** - Can invalidate refresh tokens on logout
4. **Scalable** - Stateless access tokens, stateful refresh tokens
5. **Industry Standard** - OAuth 2.0 pattern

---

## MIGRATION NOTES

### Breaking Changes:
- AuthResponse now returns `accessToken` and `refreshToken` instead of just `token`
- Clients must update to handle both tokens
- Access token expiry reduced from 24h to 15min

### Database Migration:
```sql
-- Run this to create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);
```

---

## TROUBLESHOOTING

### Issue: "Refresh token expired"
- Solution: User must login again
- Cause: Refresh token older than 7 days

### Issue: "Invalid refresh token"
- Solution: Check token exists in database
- Cause: Token deleted (logout) or never existed

### Issue: Access token expires too quickly
- Solution: Adjust jwt.expiration in properties
- Default: 15 minutes (recommended)

---

End of Refresh Token Implementation Guide
