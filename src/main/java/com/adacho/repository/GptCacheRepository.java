package com.adacho.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.adacho.entity.GptCache;

public interface GptCacheRepository extends JpaRepository<GptCache, String> {
    Optional<GptCache> findByHash(String hash);
}
