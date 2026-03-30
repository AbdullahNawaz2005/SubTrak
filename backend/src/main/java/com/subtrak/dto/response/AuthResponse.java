package com.subtrak.dto.response;

import lombok.Builder;

@Builder
public class AuthResponse {
    public String accessToken;
    public String refreshToken;
    public UserResponse user;
}
