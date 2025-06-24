package com.adacho.scaduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.adacho.repository.GptCacheRepository;

@Component
public class CacheCleaner {

    private final GptCacheRepository gptCacheRepository;

    public CacheCleaner(GptCacheRepository gptCacheRepository) {
        this.gptCacheRepository = gptCacheRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // 1시간 마다
    public void clearOldCache() {
    	gptCacheRepository.deleteOlderThanHalfdays(6);
        System.out.println("[캐시 청소] 오래된 GPT 캐시 삭제 완료");
    }
}
