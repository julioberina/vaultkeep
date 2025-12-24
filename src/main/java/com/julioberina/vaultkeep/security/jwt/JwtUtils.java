package com.julioberina.vaultkeep.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${vaultkeep.app.jwtSecret}")
	private String jwtSecret;

	@Value("${vaultkeep.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	public String generateJwtToken(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Instant now = Instant.now();
		Instant jwtExpire = now.plus(jwtExpirationMs, ChronoUnit.MILLIS);

		List<String> roles = userPrincipal.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.toList();

		return Jwts.builder()
			.setSubject(userPrincipal.getUsername())
			.claim("roles", roles)
			.setIssuedAt(Date.from(now))
			.setExpiration(Date.from(jwtExpire))
			.signWith(key(), SignatureAlgorithm.HS256)
			.compact();
	}

	private Key key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}

	public String getUsernameFromJwtToken(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(key())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	public boolean validateJwtToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(key())
				.build()
				.parse(token);

			return true;
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}
