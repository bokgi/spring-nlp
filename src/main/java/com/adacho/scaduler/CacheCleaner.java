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

    @Scheduled(cron = "*/30 * * * * *") // 매일 새벽 3시
    public void clearOldCache() {
        //gptCacheRepository.deleteOlderThanDays(1); // 1일 지난 캐시 삭제
    	gptCacheRepository.deleteOlderThanOneMinute();
        System.out.println("[캐시 청소] 오래된 GPT 캐시 삭제 완료");
    }
}
