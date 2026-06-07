package org.example.bai3.repository;

import org.example.bai3.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByRefreshToken(String refreshToken);

    /** All active (not revoked, not expired) tokens for a given user */
    List<UserToken> findByUserIdAndRevokedFalseAndExpiredFalse(Long userId);
}
