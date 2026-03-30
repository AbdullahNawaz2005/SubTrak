package com.subtrak.dto.request;

import com.subtrak.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 60)
    public String name;

    @Email
    @NotBlank
    public String email;

    @Size(min = 8)
    @NotBlank
    @ValidPassword
    public String password;
}
