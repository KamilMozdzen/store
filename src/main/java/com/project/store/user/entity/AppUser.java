package com.project.store.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "app_users")
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, name = "password_hash",length = 255)
    private String passwordHash;

    @Column(nullable = false, name = "first_name", length = 80)
    private String firstName;

    @Column(nullable = false , name = "last_name", length = 80)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 30)
    private UserRole role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AppUser(String email, String passwordHash, String firstName, String lastName, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.createdAt = Instant.now();
    }
}
