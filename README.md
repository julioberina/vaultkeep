# VaultKeep 🛡️

**VaultKeep** is a deliberately vulnerable Spring Boot application designed to demonstrate **Application Security (AppSec)** concepts, **Secure Code Review**, and **DevSecOps** workflows.

The project serves as a sandbox for identifying common OWASP vulnerabilities (like SQL Injection and IDOR) and implementing remediation strategies within a CI/CD pipeline.

## 🚀 Tech Stack
*   **Language:** Java 21 (LTS)
*   **Framework:** Spring Boot 3
*   **Build Tool:** Gradle
*   **Database:** H2 (In-Memory)
*   **Security:** Spring Security 6 (Configured for research), Snyk (SCA)

## 🎯 Project Goals
1.  **Demonstrate Vulnerabilities:** Intentionally implement "bad code" (e.g., raw SQL concatenation, Broken Access Control) to simulate real-world security flaws.
2.  **Exploitation:** Verify flaws using manual penetration testing techniques (Burp Suite, cURL).
3.  **Remediation:** Refactor code using secure patterns (e.g., JPA Parameterized Queries, Ownership Checks) to fix the flaws.
4.  **Automation:** Integrate security scanning into the GitHub Actions pipeline.

---

## ⚡ Quick Start

### Prerequisites
*   Java 21 installed
*   Git

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/julioberina/vaultkeep.git
    cd vaultkeep
    ```
2.  Run the application:
    ```bash
    ./gradlew bootRun
    ```
3.  The API will be available at `http://localhost:8080`.

---

## 🛡️ Security Status: SQL Injection

**Current Status:** 🟢 REMEDIATED

The `GET /api/notes/search` endpoint has been secured using **Spring Data JPA Parameterized Queries**.

### 1. The Vulnerability (Historical)
In the initial version (see commit history), the application used `EntityManager` with raw string concatenation:
```java
// BAD CODE (Vulnerable)
String sql = "SELECT * FROM notes WHERE content LIKE '%" + query + "%'";
```

### 2. The Remediation (Current)
The code was refactored to use the `NoteRepository` interface, which automatically handles parameter binding and escaping:
```java
// SECURE CODE (Fixed)
return noteRepository.findByContentContaining(query);
```

---

## 🛡️ Security Status: Broken Access Control (IDOR)

**Current Status:** 🟢 REMEDIATED

The `GET /api/notes/{id}` endpoint has been secured using **Database-Level Ownership Checks**.

### 1. The Vulnerability (Historical)
In the previous version (branch `feature/idor-vulnerability`), the application checked for a valid session but failed to verify if the requested note belonged to the authenticated user. This allowed any logged-in user to access any note by simply changing the ID in the URL (Insecure Direct Object Reference).

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

A **GitHub Actions** workflow (`.github/workflows/devsecops.yml`) has been implemented to automate security testing on every push and pull request.

### Pipeline Architecture
1.  **Build Environment:** Ubuntu Latest with Java 21 (Temurin).
2.  **Build Automation:** Compiles the application using Gradle.
3.  **Security Scanning:** Integrated **Snyk** to perform Software Composition Analysis (SCA) on project dependencies.
4.  **Security Gate:** The pipeline is configured to **fail the build** if any **High** severity vulnerabilities are detected (`--severity-threshold=high`).

This ensures that no critical vulnerabilities can be merged into the `main` branch, effectively "Shifting Security Left."

---

## 🛠️ Roadmap
- [x] **Phase 1:** Build MVP with SQL Injection vulnerability.
- [x] **Phase 2:** Remediate SQLi using Spring Data JPA (`NoteRepository`).
- [x] **Phase 3:** Implement CI/CD pipeline with Snyk Security scanning.
- [x] **Phase 4:** Add Authentication (Spring Security) and demonstrate IDOR.
- [x] **Phase 5:** Remediate IDOR using Repository-level ownership checks.
- [ ] **Phase 6:** Implement Role-Based Access Control (RBAC) for Admin features.

---

## ⚠️ Disclaimer
This application is for educational and demonstration purposes only. While the current version is remediated, previous commits contain intentional security vulnerabilities.
