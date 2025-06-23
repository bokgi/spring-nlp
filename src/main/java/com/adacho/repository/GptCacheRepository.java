package com.adacho.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.adacho.entity.GptCache;

public interface GptCacheRepository extends JpaRepository<GptCache, String> {
    Optional<GptCache> findByHash(String hash);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM gpt_cache WHERE created_at < NOW() - INTERVAL ?1 DAY", nativeQuery = true)
    void deleteOlderThanDays(int days);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM gpt_cache WHERE created_at < NOW() - INTERVAL 1 MINUTE", nativeQuery = true)
    void deleteOlderThanOneMinute();

}
