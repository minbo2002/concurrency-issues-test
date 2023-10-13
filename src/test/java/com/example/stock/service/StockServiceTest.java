package com.example.stock.service;

import com.example.stock.domain.Stock;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

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
    public void stockDecreaseTest1() {
        // given

        // when
        stockService.decrease(1L, 1L);  // 상품의 수량 1개 감소

        // then
        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 - 1 = 99

        assertEquals(99, stock.getQuantity());
    }

    @Test
    @DisplayName("재고 100개 동시 감소 요청 테스트")
    public void stockDecreaseTest2() throws InterruptedException {

        int threadCount = 100;  // 쓰레드 개수 100개
        ExecutorService executorService = Executors.newFixedThreadPool(32);    // 멀티쓰레드를 사용하기 위해 ExecutorService 사용
                                                                                       // ExecutorService : 동시로 실행하는 작업을 단순화하여 관리할 수 있도록 도와주는 유틸리티 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);// 100개의 요청이 모두 끝날때까지 기다리기위해

        for(int i=0; i<threadCount; i++) {

            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);  // 상품의 수량 1개 감소
                }finally {
                    latch.countDown();  // latch.countDown() : 다른 쓰레드에서 수행중인 작업이 완료될때까지 대기할 수 있도록 도와주는 클래스
                }
            });
        }

        latch.await();  // 모든 요청이 완료된다면

        Stock stock = stockRepository.findById(1L).orElseThrow();  // 100 -(1*100) = 0 예상

        assertEquals(0, stock.getQuantity());

    }
}