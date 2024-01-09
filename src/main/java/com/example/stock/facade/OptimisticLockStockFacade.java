package com.example.stock.facade;

import com.example.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimisticLockStockFacade {

    private final StockService stockService;

    // Optimistic Lock에서 업데이트에 실패했을때 재시도하는 메서드
    public void decreaseWithOptimisticLockFacade(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                stockService.decreaseWithOptimisticLock(id, quantity);

                break;

            }catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
