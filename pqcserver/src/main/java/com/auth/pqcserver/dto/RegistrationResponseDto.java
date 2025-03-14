package com.auth.pqcserver.dto;

import lombok.Data;

@Data
public class RegistrationResponseDto {
    private String signatureDilithium;
    private String signatureRSA;

    private String publicKeyDilithium;
    private String publicKeyRSA;

    private Long userid;

    public RegistrationResponseDto() {}

}
