package com.auth.pqcserver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Public key data or attestation object stored as a large object
    @Lob
    private String publicKeyDilithium;

    @Lob
    private String publicKeyRSA;


    @ManyToOne
    private User user;

    public Credential() {}

    public Credential(String publicKeyDilithium, String publicKeyRSA, User user) {
        this.publicKeyDilithium = publicKeyDilithium;
        this.publicKeyRSA = publicKeyRSA;
        this.user = user;
    }

}
