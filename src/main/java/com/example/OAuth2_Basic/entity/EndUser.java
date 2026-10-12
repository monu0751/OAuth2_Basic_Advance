package com.example.OAuth2_Basic.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "end_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EndUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Column(nullable = false)
    private String role;

    public EndUser(String google, String providerUserId, String email, String name, String picture, String user) {
        this.provider = google;
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.role = user;
    }
}
