package com.example.stock.service;

public interface StockService {

    void decrease(Long id, Long quantity);

    void decreaseWithSynchronized(Long id, Long quantity);

    void decreaseWithPessimisticLock(Long id, Long quantity);

    void decreaseWithOptimisticLock(Long id, Long quantity);

    void decreaseWithNamedLock(Long id, Long quantity);
}
