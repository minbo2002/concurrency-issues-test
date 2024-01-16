package com.example.stock.facade;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(id)) {  // lock 획득에 실패하면
            Thread.sleep(100);  // 0.1초 텀을두고 lock 획득 재시도 (redis 부하 줄일수 있음)
        }

        try {
            stockService.decrease(id, quantity);
        } finally {
            redisLockRepository.unlock(id);  // lock 해제
        }
    }
}
