package com.auth.pqcserver.dto;

import lombok.Data;

// Challenge sent to client to initiate registration
@Data
public class RegistrationChallengeDto {
    private String challenge;
    private Long userid;

    public RegistrationChallengeDto(String challenge, Long id) {
        this.challenge = challenge;
        this.userid = id;
    }
}