package com.auth.pqcserver.dto;

import lombok.Data;

@Data
public class RegistrationResultDto {
    private boolean success;
    private String message;
    private String token;
}