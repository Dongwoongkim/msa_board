package kuke.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
    // 2. 레디스에 데이터 없었다면, count 메소드 내부 로직이 호출. viewService로 원본 데이터 요청 후 그리고 레디스에 데이터를 넣고 응답
    // 3. 레디스에 데이터 있었다면, 그대로 반환
    @Cacheable(key = "#articleId", value = "articleViewCount")
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
