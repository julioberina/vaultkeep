# VaultKeep 🛡️

**VaultKeep** is a deliberately vulnerable Spring Boot application designed to demonstrate **Application Security (AppSec)** concepts, **Secure Code Review**, and **DevSecOps** workflows.

The project serves as a sandbox for identifying common OWASP vulnerabilities (like SQL Injection and IDOR) and implementing remediation strategies within a CI/CD pipeline.

## 🚀 Tech Stack
*   **Language:** Java 21 (LTS)
*   **Framework:** Spring Boot 3
*   **Build Tool:** Gradle
*   **Database:** H2 (In-Memory)
*   **Security:** Spring Security 6, JJWT (JSON Web Token), BCrypt, Snyk (SCA), Burp Suite (DAST)

## 🎯 Project Goals
1.  **Demonstrate Vulnerabilities:** Intentionally implement "bad code" (e.g., raw SQL concatenation, Broken Access Control) to simulate real-world security flaws.
2.  **Exploitation:** Verify flaws using manual penetration testing techniques (Burp Suite, Postman).
3.  **Remediation:** Refactor code using secure patterns (e.g., JPA Parameterized Queries, Ownership Checks) to fix the flaws.
4.  **Automation:** Integrate security scanning into the GitHub Actions pipeline.

---

## 🛡️ Security Status: Identity & Access Management (JWT)

**Current Status:** 🟢 REMEDIATED (Day 8 Complete)

The application has moved from basic authentication to a stateless **JWT (JSON Web Token)** architecture to support secure, scalable API interactions.

### 1. The Architecture
Implemented a custom security filter chain that intercepts every request to validate identity before reaching the controller layer.
*   **Authentication Filter:** A custom `OncePerRequestFilter` that extracts and validates Bearer tokens from the `Authorization` header.
*   **Symmetric Signing:** Uses **HMAC-SHA256** with a managed secret key to ensure token integrity and prevent tampering.
*   **Statelessness:** The server does not store session state, reducing the attack surface for Session Fixation and CSRF.

### 2. Secure Implementation Details
*   **Password Hashing:** Integrated `BCryptPasswordEncoder` (strength 10) to ensure passwords are never stored in plain text.
*   **Auth Controller:** Implemented `/api/auth/register` and `/api/auth/login` endpoints to handle user onboarding and secure token issuance.
*   **Secret Management:** Implemented RFC 7518 compliant secret management. The application requires a 256-bit (32-byte) secret key, externalized via environment variables to prevent credential exposure in source control.
*   **Error Handling:** Implemented a custom `AuthenticationEntryPoint` to return clean, type-safe JSON error responses (401 Unauthorized) instead of leaking server internals.

### 3. Verification (Postman)
*   **Registration:** Verified that new users are persisted with hashed passwords in the H2 database.
*   **Login:** Verified that valid credentials return a signed JWT.
*   **Access:** Confirmed that protected endpoints (e.g., `/api/notes`) require a valid `Authorization: Bearer <token>` header.

---

## 🛡️ Security Status: SQL Injection (CWE-89)

**Current Status:** 🟢 REMEDIATED

The application features a search functionality that was used to demonstrate the difference between vulnerable string concatenation and secure parameterized queries.

### 1. The Vulnerability (Lab Environment)
A dedicated endpoint `GET /api/notes/search/vulnerable` was created to demonstrate **Boolean-based SQL Injection**. The code bypassed the safe Repository layer to use raw `EntityManager` queries:

```java
// BAD CODE (Vulnerable to SQLi)
String sql = "SELECT * FROM notes WHERE content LIKE '%" + query + "%'";
return entityManager.createNativeQuery(sql, Note.class).getResultList();
```

### 2. Exploitation (Burp Suite)
The vulnerability was confirmed using **Burp Suite Professional/Community**:
*   **Indicator:** Inputting a single quote (`'`) triggered a **500 Internal Server Error**, indicating a syntax break in the backend database.
*   **Payload:** Using the payload `' OR '1'='1` in the query parameter allowed for a full database dump, bypassing all search filters.

### 3. The Remediation (Production)
The production endpoint `GET /api/notes/search` was secured using **Spring Data JPA Query Derivation**. This ensures that all user input is treated as data, never as executable code.

```java
// SECURE CODE (Fixed)
return noteRepository.findByContentContainingIgnoreCaseAndOwner(query, authentication.getName());
```

---

## 🛡️ Security Status: Broken Access Control (IDOR)

**Current Status:** 🟢 REMEDIATED

The `GET /api/notes/{id}` endpoint has been secured using **Database-Level Ownership Checks**.

### 1. The Vulnerability (Historical)
In the previous version, the application checked for a valid session but failed to verify if the requested note belonged to the authenticated user.

```java
// BAD CODE (Vulnerable)
Note note = noteRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Note not found"));
return ResponseEntity.ok(note);
```

### 2. The Remediation (Current)
The code was refactored to enforce ownership at the database query level. The `NoteRepository` was updated to filter by both ID and Owner.

```java
// SECURE CODE (Fixed)
Note note = noteRepository.findByIdAndOwner(id, userDetails.getUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
return ResponseEntity.ok(note);
```

---


---

## 🛡️ Security Status: Privilege Escalation & Hardening

**Current Status:** 🟢 REMEDIATED

The application's registration flow was hardened to prevent **Broken Function Level Authorization** (Privilege Escalation).

### 1. The Vulnerability (Mass Assignment)
The initial registration endpoint allowed users to specify their own roles in the JSON request. An attacker could self-promote to an administrator by simply adding `"roles": ["admin"]` to their signup payload.

### 2. The Remediation
*   **DTO Hardening:** Removed the `roles` field from the `RegisterRequest` record to eliminate the attack vector at the entry point.
*   **Logic Hardening:** Refactored the `AuthController` to hardcode the default `ROLE_USER` assignment, ensuring that administrative privileges can only be granted through secure, internal channels.
*   **Validation:** Added regex patterns to ensure usernames are alphanumeric, preventing potential injection or spoofing attempts.

## 🔄 DevSecOps Pipeline

**Current Status:** 🟢 ACTIVE

A **GitHub Actions** workflow (`.github/workflows/devsecops.yml`) automates security testing on every push.
*   **SCA (Software Composition Analysis):** Integrated **Snyk** to find and block vulnerable dependencies.
*   **Security Gate:** The pipeline is configured to **fail the build** if any **High** severity vulnerabilities are detected.

---

## 🛠️ Roadmap
- [x] **Phase 1-2:** Build MVP and implement basic SQLi remediation.
- [x] **Phase 3:** Integrate CI/CD pipeline with Snyk Security scanning.
- [x] **Phase 4-5:** Implement Spring Security and remediate IDOR vulnerabilities.
- [x] **Phase 6:** Advanced SQLi Lab: Manual exploitation with Burp Suite & Parameterization.
- [x] **Phase 7:** JWT Infrastructure & Identity Management.
- [x] **Phase 8:** **Auth Controller (Login/Register) & Password Hashing (Current)**
- [x] **Phase 9:** Role-Based Access Control (RBAC) & Input Validation.
- [ ] **Phase 10:** Final Security Audit & `SECURITY.md` Documentation.

---

## ⚠️ Disclaimer
This application is for educational purposes only. It contains intentional security vulnerabilities in specific branches and endpoints to demonstrate AppSec principles.