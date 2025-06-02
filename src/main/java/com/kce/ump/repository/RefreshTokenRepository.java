package com.kce.ump.repository;

import com.kce.ump.model.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByUserId(String userId);
    RefreshToken findByToken(String token);
    void deleteByToken(String token);
}
