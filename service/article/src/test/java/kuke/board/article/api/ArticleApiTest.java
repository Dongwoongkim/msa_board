package kuke.board.article.api;

import java.util.List;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

public class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
            "title", "content", 1L, 1L)
        );
        System.out.println("response: " + response);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(253419062624256000L);
        System.out.println("response: " + response);
    }

    @Test
    void updateTest() {
        update(253419062624256000L);
        ArticleResponse response = read(253419062624256000L);
        System.out.println("response: " + response);
    }

    @Test
    void deleteTest() {
        delete(253419062624256000L);
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
            .uri("/v1/articles?boardId=1&pageSize=30&page=50010")
            .retrieve()
            .body(ArticlePageResponse.class);

        /**
         *  ( (N - 1) / K ) + 1 ) * M * K + 1
         *  ( (50_000 - 1) / 10 ) + 1 ) * 30 * 10 + 1 = 1,500,001개
         *  사용자가 50_000 ~ 50_010번 페이지에 머무르는 경우, 게시글이 1,500,001개의 게시글 존재 여부만 판단하면 됨
         *  1_500_001개 이상 -> "다음" 버튼까지 활성화
         *  1_500_001개 미만 -> 50_010번 페이지까지만 활성화
         */
        System.out.println("response.getArticleCount(): " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()) {
            System.out.println("articleId = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleResponse> article1 = restClient.get()
            .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
            });

        System.out.println("FIRST PAGE");
        for (ArticleResponse articleResponse : article1) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }

        Long lastArticleId = article1.getLast().getArticleId();

        List<ArticleResponse> article2 = restClient.get()
            .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=" + lastArticleId)
            .retrieve()
            .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
            });

        System.out.println("SECOND PAGE");
        for (ArticleResponse articleResponse : article2) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
            .uri("/v1/articles")
            .body(request)
            .retrieve()
            .body(ArticleResponse.class);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
            .uri("/v1/articles/{articleId}", articleId)
            .retrieve()
            .body(ArticleResponse.class);
    }

    void delete(Long articleId) {
        restClient.delete()
            .uri("/v1/articles/{articleId}", articleId)
            .retrieve();
    }

    void update(Long articleId) {
        restClient.put()
            .uri("/v1/articles/{articleId}", articleId)
            .body(new ArticleUpdateRequest("title2", "content2"))
            .retrieve()
            .body(ArticleResponse.class);
    }

    @Test
    void countTest() {
        ArticleResponse response = create(new ArticleCreateRequest("title", "content", 1L, 2L));

        Long count1 = restClient.get()
            .uri("/v1/articles/boards/{boardId}/count", 2L)
            .retrieve()
            .body(Long.class);

        System.out.println("count = " + count1);

        restClient.delete()
            .uri("/v1/articles/{articleId}", response.getArticleId())
            .retrieve();

        Long count2 = restClient.get()
            .uri("/v1/articles/boards/{boardId}/count", 2L)
            .retrieve()
            .body(Long.class);

        System.out.println("count = " + count2);
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {

        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {

        private String title;
        private String content;
    }
}
