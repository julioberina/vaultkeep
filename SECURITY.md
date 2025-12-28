# Security Policy

## Supported Versions

The following versions of VaultKeep are currently being supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

**Note:** VaultKeep is a deliberately vulnerable application for educational purposes. However, if you find a vulnerability in the *security infrastructure* (e.g., the JWT implementation, RBAC logic, or CI/CD pipeline) that is not intended as a lab exercise, please report it.

To report a vulnerability:
1. Open a GitHub Issue with the prefix `[SECURITY]`.
2. Provide a detailed description of the flaw.
3. Include steps to reproduce or a Proof of Concept (PoC).

## Security Mindset
This project follows the principle of **Defense in Depth**. While some endpoints are intentionally left vulnerable for educational labs, the core framework is hardened using:
*   **SCA:** Snyk dependency scanning.
*   **SAST:** Manual secure code review and static analysis.
*   **Identity:** Stateless JWT with HMAC-SHA256 signing.
*   **Authorization:** Role-Based Access Control (RBAC).