package kuke.board.articleread.client;

import jakarta.annotation.PostConstruct;
import kuke.board.articleread.cache.OptimizedCacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewClient {

    private RestClient restClient;

    @Value("${endpoints.kuke-board-view-service.url}")
    private String viewServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(viewServiceUrl);
    }

    // @Cacheable
    // 1. 레디스에서 데이터 조회
    // 2. 레디스에 데이터 없었다면(캐시 미스), count 메소드 내부 로직이 호출. viewService로 원본 데이터 응답 후 레디스에 데이터를 넣음
    // 3. 레디스에 데이터 있었다면(캐시 히트), 그대로 반환
    /* 동작 순서
    1. 캐시 확인: 메소드가 호출되면 먼저 설정된 캐시 저장소(여기서는 Redis)에서 articleViewCount::#articleId 키를 가진 데이터가 있는지 확인한다.
    2. 캐시 히트 (Hit): 데이터가 있다면 메소드 본문(count 내부 로직)을 실행하지 않고 즉시 캐시된 값을 반환한다.
    3. 캐시 미스 (Miss): 데이터가 없다면, 메소드 본문을 실행한다. (이때 restClient를 통해 실제 API를 호출)
    4. 결과 저장 및 반환: 메소드가 반환한 값(Long)을 Redis에 저장하고, 호출한 곳(viewService)으로 값을 전달한다.
     */
//    @Cacheable(key = "#articleId", value = "articleViewCount")
    @OptimizedCacheable(type = "articleViewCount", ttlSeconds = 1)
    public long count(Long articleId) {
        log.info("[ViewClient.count] articleId = {}", articleId);
        try {
            return restClient.get()
                .uri("/v1/article-view/articles/{articleId}/count", articleId)
                .retrieve()
                .body(Long.class);
        } catch (Exception e) {
            log.error("[ViewClient.count] articleId = {}", articleId, e);
            return 0;
        }
    }
}
