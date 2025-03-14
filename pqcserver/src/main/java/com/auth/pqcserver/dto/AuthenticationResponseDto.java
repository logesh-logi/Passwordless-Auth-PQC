package com.auth.pqcserver.dto;

import lombok.Data;

@Data
public class AuthenticationResponseDto {
    private String signatureDilithium;
    private String signatureRSA;
    private Long userid;
}
