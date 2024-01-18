package com.example.stock.facade;

import com.example.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedissonLockStockFacade {

    private final RedissonClient redissonClient;

    private final StockService stockService;

    public void decreaseWithRedissonLockFacade(Long id, Long quantity) {
        RLock lock = redissonClient.getLock(id.toString());  // redissonClient 활용하여 Lock 객체 가져오기

        try {
            // Lock 획득
            boolean availableLock = lock.tryLock(20, 1, TimeUnit.SECONDS);// 몇초동안 Lock획득을 시도할것인지,  몇초동안 점유할것인지

            // Lock 획득 실패시
            if(!availableLock) {
                log.info("lock 획득 실패");
                return;
            }
            stockService.decreaseWithRedissonLock(id, quantity);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
