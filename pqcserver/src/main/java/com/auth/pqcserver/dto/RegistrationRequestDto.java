package com.auth.pqcserver.dto;

import lombok.Data;

@Data
public class RegistrationRequestDto {
    private String username;
    private String email;

    public RegistrationRequestDto() {}
}
