package com.iglesia;

public record UserResponse(
        Long id,
        String email,
        String role
) {}