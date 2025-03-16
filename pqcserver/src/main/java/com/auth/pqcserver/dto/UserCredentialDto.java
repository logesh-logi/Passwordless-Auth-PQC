package com.auth.pqcserver.dto;

import lombok.Data;

@Data
public class UserCredentialDto {
    private String username;
    private String email;
    private String publicKeyRSA;
    private String publicKeyDilithium;

    public UserCredentialDto(String username, String email, String publicKeyRSA, String publicKeyDilithium) {
        this.publicKeyDilithium = publicKeyDilithium;
        this.username = username;
        this.email = email;
        this.publicKeyRSA = publicKeyRSA;
    }
}
