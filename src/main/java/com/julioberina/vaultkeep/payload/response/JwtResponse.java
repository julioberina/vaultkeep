package com.julioberina.vaultkeep.payload.response;

import java.util.List;

public record JwtResponse(String token, Long id, String username, List<String> roles) {}
