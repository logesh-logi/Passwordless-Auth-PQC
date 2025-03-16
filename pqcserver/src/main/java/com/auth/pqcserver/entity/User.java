package com.auth.pqcserver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String challenge;
    private String email;

    public User() {}

    public User(String username, String challenge, String email) {
        this.username = username;
        this.challenge = challenge;
        this.email = email;
    }

}
