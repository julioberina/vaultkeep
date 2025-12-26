package com.julioberina.vaultkeep.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin")
public class AdminController {
	@GetMapping("dashboard")
	public ResponseEntity<String> getAdminDashboard() {
		return ResponseEntity.ok("Welcome to the Admin Dashboard. This is sensitive data.");
	}
}
