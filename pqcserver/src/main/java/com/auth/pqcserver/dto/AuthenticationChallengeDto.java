package com.auth.pqcserver.dto;

import lombok.Data;

@Data
public class AuthenticationChallengeDto {
    private String challenge;
    private Long userid;

    public AuthenticationChallengeDto(String challenge, Long id) {
        this.challenge = challenge;
        this.userid = id;
    }
}
