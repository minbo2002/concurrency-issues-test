package com.example.stock.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;

    // lock 메서드
    public Boolean lock(Long key) {
        return redisTemplate    // setnx 명령어 사용
                .opsForValue()
                .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));  // key에 stockId를 value에 "lock" 문자열 넣는다
    }

    // unlock 메서드
    public Boolean unlock(Long key) {
        return redisTemplate.delete(generateKey(key));  // key에 해당하는 데이터 삭제
    }

    public String generateKey(Long key) {
        return key.toString();
    }
}
