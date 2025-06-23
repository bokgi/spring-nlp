package com.adacho.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.adacho.entity.GptCache;

public interface GptCacheRepository extends JpaRepository<GptCache, String> {
    Optional<GptCache> findByHash(String hash);
    
    @Modifying
    @Query("DELETE FROM GptCache c WHERE c.createdAt < CURRENT_TIMESTAMP - INTERVAL ?1 DAY")
    void deleteOlderThanDays(int days);
    
    @Modifying
    @Query("DELETE FROM GptCache c WHERE c.createdAt < CURRENT_TIMESTAMP - INTERVAL 3 MINUTE")
    void deleteOlderThanOneMinute();

}
