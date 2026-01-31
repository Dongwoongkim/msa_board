package kuke.board.articleread.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.View;

@SpringBootTest
class ViewClientTest {

    @Autowired
    ViewClient viewClient;
    @Autowired
    private View view;

    @Test
    void readCacheableTest() throws InterruptedException {
        viewClient.count(1L); // 로그 출력
        viewClient.count(1L); // 로그 미출력
        viewClient.count(1L); // 로그 미출력

        TimeUnit.SECONDS.sleep(3);
        viewClient.count(1L); // 로그 출력
    }

    /**
     * 동시 요청이 몰린다면, 캐시 갱신이 불필요하게 많아짐
     *
     * @throws InterruptedException
     */
    @Test
    void readCacheableMultiThreadTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        viewClient.count(1L);

        for (int i = 0; i < 5; i++) {
            CountDownLatch countDownLatch = new CountDownLatch(5);
            for (int j = 0; j < 5; j++) {
                executor.submit(() -> {
                    viewClient.count(1L);
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            TimeUnit.SECONDS.sleep(2);
            System.out.println("=== cache expired ===");
        }
    }
}