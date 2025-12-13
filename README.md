# VaultKeep 🛡️

**VaultKeep** is a deliberately vulnerable Spring Boot application designed to demonstrate **Application Security (AppSec)** concepts, **Secure Code Review**, and **DevSecOps** workflows.

The project serves as a sandbox for identifying common OWASP vulnerabilities (like SQL Injection) and implementing remediation strategies within a CI/CD pipeline.

## 🚀 Tech Stack
*   **Language:** Java 21 (LTS)
*   **Framework:** Spring Boot 3
*   **Build Tool:** Gradle
*   **Database:** H2 (In-Memory)
*   **Security:** Spring Security (Configured for research), Snyk (SAST)

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

## 🕵️ Vulnerability Showcase: SQL Injection

**Current Status:** 🔴 VULNERABLE

The `GET /api/notes/search` endpoint currently uses `EntityManager` with raw string concatenation, making it vulnerable to **SQL Injection**.

### 1. Setup (Seed Data)
Create a few notes, including a "Secret" one:
```bash
# Create a Public Note
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title": "Public Info", "content": "This is visible to the public."}'

# Create a Secret Note
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title": "TOP SECRET", "content": "You should not see this!"}'
```

### 2. The Attack (Proof of Concept)
An attacker can inject a payload to bypass the search filter and dump the entire database.

*   **Target:** Retrieve the "TOP SECRET" note while searching for "public".
*   **Payload:** `public%' OR '1'='1' --`
*   **Logic:** The query becomes `SELECT * FROM notes WHERE content LIKE '%public%' OR '1'='1'`. Since `'1'='1'` is always true, the database returns every row.

**Attack URL (Encoded):**
```
http://localhost:8080/api/notes/search?query=public%25%27%20OR%20%271%27%3D%271%27%20--
```
*(Note: The URL is encoded. The decoded payload is `public%' OR '1'='1' --`)*

**Result:**
The API returns **ALL** notes, including the "TOP SECRET" note, proving the injection was successful.

---

## 🛠️ Roadmap
- [x] **Phase 1:** Build MVP with SQL Injection vulnerability.
- [ ] **Phase 2:** Remediate SQLi using Spring Data JPA (`NoteRepository`).
- [ ] **Phase 3:** Implement CI/CD pipeline with Snyk SAST scanning.
- [ ] **Phase 4:** Add Authentication (Spring Security) and demonstrate IDOR.

---

## ⚠️ Disclaimer
This application contains **intentional security vulnerabilities**. It is for educational and demonstration purposes only. Do not deploy this code to a production environment without remediation.
