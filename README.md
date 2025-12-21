# VaultKeep 🛡️

**VaultKeep** is a deliberately vulnerable Spring Boot application designed to demonstrate **Application Security (AppSec)** concepts, **Secure Code Review**, and **DevSecOps** workflows.

The project serves as a sandbox for identifying common OWASP vulnerabilities (like SQL Injection and IDOR) and implementing remediation strategies within a CI/CD pipeline.

## 🚀 Tech Stack
*   **Language:** Java 21 (LTS)
*   **Framework:** Spring Boot 3
*   **Build Tool:** Gradle
*   **Database:** H2 (In-Memory)
*   **Security:** Spring Security 6, Snyk (SCA), Burp Suite (DAST)

## 🎯 Project Goals
1.  **Demonstrate Vulnerabilities:** Intentionally implement "bad code" (e.g., raw SQL concatenation, Broken Access Control) to simulate real-world security flaws.
2.  **Exploitation:** Verify flaws using manual penetration testing techniques (Burp Suite, Postman).
3.  **Remediation:** Refactor code using secure patterns (e.g., JPA Parameterized Queries, Ownership Checks) to fix the flaws.
4.  **Automation:** Integrate security scanning into the GitHub Actions pipeline.

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
*   **Tooling:** Used **Burp Proxy** to intercept requests and **Burp Repeater** to perform manual payload injection.

### 3. The Remediation (Production)
The production endpoint `GET /api/notes/search` was secured using **Spring Data JPA Query Derivation**. This ensures that all user input is treated as data, never as executable code.

```java
// SECURE CODE (Fixed)
// Enforces both Parameterization and Ownership (IDOR Protection)
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
// Only checks if the note exists, not who owns it
Note note = noteRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Note not found"));
return ResponseEntity.ok(note);
```

### 2. The Remediation (Current)
The code was refactored to enforce ownership at the database query level. The `NoteRepository` was updated to filter by both ID and Owner.

```java
// SECURE CODE (Fixed)
// Returns empty (404) if the note exists but belongs to someone else
Note note = noteRepository.findByIdAndOwner(id, userDetails.getUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
return ResponseEntity.ok(note);
```

### 3. Verification
To verify the fix, you must create two users and attempt to cross-access data.

**Step 1: Setup Users**
*   **User A:** `user` / `password`
*   **User B:** `admin` / `admin`

**Step 2: Create Notes**
1.  Log in as **User A** and create a note (e.g., ID: 1).
2.  Log in as **User B** and create a note (e.g., ID: 2).

**Step 3: Attempt Attack**
Log in as **User A** and try to access User B's note:
```bash
curl -u user:password http://localhost:8080/api/notes/2
```

**Result:**
*   **Before Fix:** Returned User B's note (200 OK).
*   **After Fix:** Returns **404 Not Found**. The server denies the existence of the resource to unauthorized users, preventing ID Enumeration.

---

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
- [ ] **Phase 7:** Role-Based Access Control (RBAC) for Admin features.
- [ ] **Phase 8:** Dependency Scanning & Snyk Integration (Advanced).
- [ ] **Phase 9:** Logging, Monitoring, and Rate Limiting (Brute Force Protection).
- [ ] **Phase 10:** Final Security Audit & `SECURITY.md` Documentation.

---

## ⚠️ Disclaimer
This application is for educational purposes only. It contains intentional security vulnerabilities in specific branches and endpoints to demonstrate AppSec principles.