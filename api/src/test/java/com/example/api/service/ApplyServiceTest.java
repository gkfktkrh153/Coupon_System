package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    public void 한번만응모(){
        applyService.apply(1L);
        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);
    }
    @Test
    public void 여러명응모() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);// 병렬작업을 도와주는 Java의 API
        CountDownLatch latch = new CountDownLatch(threadCount); // 다른 쓰레드의 작업을 기다리는 클래스

        for(int i =0 ; i < threadCount; i++){
            long userId = i;
            executorService.submit(() ->
            {
                try{
                    applyService.apply(userId);
                } finally {
                    latch.countDown();
                }

            });

        }
        latch.await();
        // 두 개 이상의 프로세스가 공유자원에 동시 접근 -> 레이스 컨디션 발생
        //
        long count = couponRepository.count();

        assertThat(count).isEqualTo(100);
    }
}