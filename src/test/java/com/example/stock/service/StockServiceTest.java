package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.facade.NamedLockStockFacade;
import com.example.stock.facade.OptimisticLockStockFacade;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    @Autowired
    private NamedLockStockFacade namedLockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    // 테스트 케이스 시작 전 더미데이터 생성
    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    // 테스트 케이스 종료되면 모든 데이터 삭제
    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    @DisplayName("재고 1개 감소 요청 테스트")
    public void decreaseStock() {
        // given

        // when
        stockService.decrease(1L, 1L);  // 상품의 수량 1개 감소

        // then
        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 - 1 = 99

        assertEquals(99, stock.getQuantity());
    }

    @Test
    @DisplayName("재고 100개 동시 감소 요청 테스트")
    public void decreaseStocks() throws InterruptedException {

        int threadCount = 100;  // 쓰레드 개수 100개

        /* 멀티쓰레드를 사용하기 위해 ExecutorService 사용
           ExecutorService : 동시로 실행하는 작업을 단순화하여 관리할 수 있도록 도와주는 유틸리티 클래스    */
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        /* CountDownLatch : 다른 쓰레드에서 수행중인 작업이 완료될때까지 대기할 수 있도록 도와주는 클래스   */
        CountDownLatch latch = new CountDownLatch(threadCount);  // latch 숫자 100개

        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);  // 상품의 수량 1개 감소
                }finally {
                    latch.countDown();  // latch.countDown() : latch 숫자 1개씩 감소
                }
            });
        }

        latch.await();  // latch.await() : latch의 숫자가 0이 될때까지 대기.  즉 모든 요청이 완료될때까지 대기

        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 -(1*100) = 0 예상

        assertEquals(0, stock.getQuantity());  // 결과  --> 쓰레드 race condition에 의한 테스트 실패
                                                       //      -->  2개 이상의 쓰레드가 공유데이터에 접근하여 동시에 값을 변경하려고 시도
    }

    @Test
    @DisplayName("synchronized 활용한 재고 100개 동시 감소 요청 테스트")
    public void decreaseStockWithSynchronized() throws InterruptedException {

        int threadCount = 100;  // 쓰레드 개수 100개

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);  // latch 숫자 100개

        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseWithSynchronized(1L, 1L);  // 상품의 수량 1개 감소
                }finally {
                    latch.countDown();  // latch.countDown() : latch 숫자 1개씩 감소
                }
            });
        }

        latch.await();  // latch.await() : latch의 숫자가 0이 될때까지 대기.  즉 모든 요청이 완료될때까지 대기

        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 -(1*100) = 0 예상

        assertEquals(0, stock.getQuantity());
    }

    @Test
    @DisplayName("pessimisticLock 활용한 재고 100개 동시 감소 요청 테스트")
    public void decreaseStockWithPessimisticLock() throws InterruptedException {

        int threadCount = 100;  // 쓰레드 개수 100개

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);  // latch 숫자 100개

        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseWithPessimisticLock(1L, 1L);  // 상품의 수량 1개 감소
                }finally {
                    latch.countDown();  // latch.countDown() : latch 숫자 1개씩 감소
                }
            });
        }

        latch.await();  // latch.await() : latch의 숫자가 0이 될때까지 대기.  즉 모든 요청이 완료될때까지 대기

        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 -(1*100) = 0 예상

        assertEquals(0, stock.getQuantity());
    }

    @Test
    @DisplayName("optimisticLock 활용한 재고 100개 동시 감소 요청 테스트")
    public void decreaseStockWithOptimisticLock() throws InterruptedException {

        int threadCount = 100;  // 쓰레드 개수 100개
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);  // latch 숫자 100개

        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockFacade.decreaseWithOptimisticLockFacade(1L, 1L);  // 상품의 수량 1개 감소
                }catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }finally {
                    latch.countDown();  // latch.countDown() : latch 숫자 1개씩 감소
                }
            });
        }

        latch.await();  // latch.await() : latch의 숫자가 0이 될때까지 대기.  즉 모든 요청이 완료될때까지 대기

        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 -(1*100) = 0 예상

        assertEquals(0, stock.getQuantity());
    }

    @Test
    @DisplayName("NamedLock 활용한 재고 100개 동시 감소 요청 테스트")
    public void decreaseStockWithNamedLock() throws InterruptedException {

        int threadCount = 100;  // 쓰레드 개수 100개

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);  // latch 숫자 100개

        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    namedLockStockFacade.decreaseWithNamedLockFacade(1L, 1L);  // 상품의 수량 1개 감소
                } finally {
                    latch.countDown();  // latch.countDown() : latch 숫자 1개씩 감소
                }
            });
        }

        latch.await();  // latch.await() : latch의 숫자가 0이 될때까지 대기.  즉 모든 요청이 완료될때까지 대기

        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 -(1*100) = 0 예상

        assertEquals(0, stock.getQuantity());
    }
}