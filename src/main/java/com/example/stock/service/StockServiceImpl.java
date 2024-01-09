package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Transactional
    @Override
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값을 저장
        stockRepository.saveAndFlush(stock);
    }

    // 데이터에 1개의 쓰레드만 접근가능하도록 자바에서 지원하는 방법으로 synchronized 방법을 활용
    // @Transactional 어노테이션은 사용하지 않는다.
    // synchronized는 1개의 프로세스 안에서만 보장됨. 하지만 서버가 여러개인 여러 프로세스에서는 동시에 접근 가능하다. 따라서 synchronized 방법으로는 race condition을 해결할 수 없다.
    @Override
    public synchronized void decreaseWithSynchronized(Long id, Long quantity) {

        /* 트랜잭션이 시작해서 메서드를 호출하고 메서드 실행이 종료가 되면 트랜잭션이 종료된다.
           트랜잭션 종료시점에 DB에 변경된 내용을 반영하는데, 이때 DB에 반영이 되기전에 다른 쓰레드가 접근하여 decrease 메서드를 호출하면 문제가 발생한다.
           따라서 @Transactional 어노테이션을 사용하지 않고 synchronized 키워드를 사용하여 동시에 접근하는 것을 막는다.
         */

        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값을 저장
        stockRepository.saveAndFlush(stock);
    }

    // 트랜잭션이 시작될때 Shared Lock이나 Exclusive Lock을 걸어서 다른 트랜잭션이 접근하지 못하도록 하는 비관적락(Pessimistic Lock) 방법을 활용
    @Transactional
    @Override
    public void decreaseWithPessimisticLock(Long id, Long quantity) {

        // Stock 조회
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값을 저장
        stockRepository.save(stock);
    }

    @Transactional
    @Override
    public void decreaseWithOptimisticLock(Long id, Long quantity) {

        // Stock 조회
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값을 저장
        stockRepository.save(stock);
    }

}
