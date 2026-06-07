package org.example.bai3.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", nullable = false, length = 512)
    private String refreshToken;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "is_expired", nullable = false)
    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}
