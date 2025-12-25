package com.julioberina.vaultkeep.payload.response;

import java.util.Set;

public record RegisterRequest(String username, String password, Set<String> roles) {}
