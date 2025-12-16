# VaultKeep 🛡️

**VaultKeep** is a deliberately vulnerable Spring Boot application designed to demonstrate **Application Security (AppSec)** concepts, **Secure Code Review**, and **DevSecOps** workflows.

The project serves as a sandbox for identifying common OWASP vulnerabilities (like SQL Injection) and implementing remediation strategies within a CI/CD pipeline.

## 🚀 Tech Stack
*   **Language:** Java 21 (LTS)
*   **Framework:** Spring Boot 3
*   **Build Tool:** Gradle
*   **Database:** H2 (In-Memory)
*   **Security:** Spring Security 6 (Configured for research), Snyk (SCA)

## 🎯 Project Goals
1.  **Demonstrate Vulnerabilities:** Intentionally implement "bad code" (e.g., raw SQL concatenation) to simulate real-world security flaws.
2.  **Exploitation:** Verify flaws using manual penetration testing techniques.
3.  **Remediation:** Refactor code using secure patterns (e.g., JPA Parameterized Queries) to fix the flaws.
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

### 3. Verification
To verify the fix, you must first seed the database and then attempt the attack.

**Step 1: Seed Data**
```bash
# Create a Public Note
curl -X POST http://localhost:8080/api/notes   -H "Content-Type: application/json"   -d '{"title": "Public Info", "content": "This is visible to the public."}'

# Create a Secret Note
curl -X POST http://localhost:8080/api/notes   -H "Content-Type: application/json"   -d '{"title": "TOP SECRET", "content": "You should not see this!"}'
```

**Step 2: Attempt Attack**
**Attack Payload:** `public%' OR '1'='1' --`

**Attack URL (Encoded):**
```
http://localhost:8080/api/notes/search?query=public%25%27%20OR%20%271%27%3D%271%27%20--
```

**Result:**
*   **Before Fix:** Returned ALL notes (including "TOP SECRET").
*   **After Fix:** Returns an **Empty List** (or only notes literally containing the attack string). The injection fails.

---

## 🛡️ Security Status: IDOR (Insecure Direct Object Reference)

**Current Status:** 🔴 VULNERABLE (Intentional)

The application now implements **Spring Security** with Basic Auth, but the `GET /api/notes/{id}` endpoint contains a critical **IDOR** vulnerability.

### 1. The Setup (Authentication)
I implemented `InMemoryUserDetailsManager` with two users:
*   **User:** `user` / `password`
*   **Admin:** `admin` / `admin`

I also implemented a **Trusted Identity** pattern where notes are automatically assigned an `owner` based on the logged-in user:
```java
// Securely assigning ownership
note.setOwner(authentication.getName());
```

### 2. The Vulnerability
While the "List View" (`GET /api/notes`) correctly filters notes by owner, the "Detail View" (`GET /api/notes/{id}`) **does not check ownership**. It simply fetches the note by ID, regardless of who is asking.

```java
// VULNERABLE CODE
@GetMapping("{id}")
public Note getNoteById(@PathVariable Long id) {
    // 🚨 No check to see if the 'owner' matches the 'authentication.getName()'
    return noteRepository.findById(id).orElseThrow(...);
}
```

### 3. Verification (The Attack)
To reproduce this vulnerability, log in as `user` and attempt to steal a note belonging to `admin`.

**Step 1: Create Notes (Seed Data)**
```bash
# 1. Create a note as 'user' (This will get ID: 1)
curl -u user:password -X POST http://localhost:8080/api/notes \
     -H "Content-Type: application/json" \
     -d '{"title": "User Note", "content": "I am a standard user."}'

# 2. Create a SECRET note as 'admin' (This will get ID: 2)
curl -u admin:admin -X POST http://localhost:8080/api/notes \
     -H "Content-Type: application/json" \
     -d '{"title": "ADMIN SECRETS", "content": "Launch codes: 12345"}'
```

**Step 2: Verify Access Control (The "Good" Path)**
Check that `user` can only see their own notes in the list.
```bash
curl -u user:password http://localhost:8080/api/notes
# Result: Returns ONLY the "User Note". The Admin note is hidden.
```

**Step 3: Execute IDOR Attack**
Now, as `user`, try to access the Admin's note directly by guessing its ID (`2`).
```bash
curl -u user:password http://localhost:8080/api/notes/2
```

**Result:**
*   **Expected (Secure):** 403 Forbidden or 404 Not Found.
*   **Actual (Vulnerable):** Returns the **ADMIN SECRETS** note. 🚨

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
- [ ] **Phase 5:** Remediate IDOR using `@PostAuthorize` or Service-layer checks.

---

## ⚠️ Disclaimer
This application is for educational and demonstration purposes only. While parts of the application are remediated, it currently contains intentional security vulnerabilities (IDOR) for research purposes.
