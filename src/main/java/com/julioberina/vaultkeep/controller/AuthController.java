package com.julioberina.vaultkeep.controller;

import com.julioberina.vaultkeep.model.ERole;
import com.julioberina.vaultkeep.model.Role;
import com.julioberina.vaultkeep.model.User;
import com.julioberina.vaultkeep.payload.response.JwtResponse;
import com.julioberina.vaultkeep.payload.response.LoginRequest;
import com.julioberina.vaultkeep.payload.response.MessageResponse;
import com.julioberina.vaultkeep.payload.response.RegisterRequest;
import com.julioberina.vaultkeep.repository.RoleRepository;
import com.julioberina.vaultkeep.repository.UserRepository;
import com.julioberina.vaultkeep.security.jwt.JwtUtils;
import com.julioberina.vaultkeep.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;

	@PostMapping("login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.toList();

		return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles));
	}

	@PostMapping("register")
	public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		if (userRepository.existsByUsername(registerRequest.username()))
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));

		User user = new User(registerRequest.username(), passwordEncoder.encode(registerRequest.password()));
		Role userRole = roleRepository.findByName(ERole.ROLE_USER)
			.orElseThrow(() -> new RuntimeException("Error: Default Role (ROLE_USER) not found in database."));
		user.addRole(userRole);

		userRepository.save(user);
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
}
