package kuke.board.articleread.data;


import java.util.random.RandomGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class DataInitializer {

    RestClient articleServiceClient = RestClient.create("http://localhost:9000");
    RestClient commentServiceClient = RestClient.create("http://localhost:9001");
    RestClient articleLikeServiceClient = RestClient.create("http://localhost:9002");
    RestClient viewServiceClient = RestClient.create("http://localhost:9003");

    @Test
    void initialize() {
        for (int i = 0; i < 30; i++) {
            Long articleId = createArticle();

            long commentCount = RandomGenerator.getDefault().nextLong(10);
            long likeCount = RandomGenerator.getDefault().nextLong(10);
            long viewCount = RandomGenerator.getDefault().nextLong(200);

            createComment(articleId, commentCount);
            createLike(articleId, likeCount);
            createViewCount(articleId, viewCount);
        }
    }

    private void createViewCount(Long articleId, long viewCount) {
        for (int i = 0; i < viewCount; i++) {
            viewServiceClient.post()
                .uri("/v1/article-views/articles/{articleId}/users/{userId}", articleId, i + 1)
                .retrieve();
        }
    }

    private void createLike(Long articleId, long likeCount) {
        for (int i = 0; i < likeCount; i++) {
            articleLikeServiceClient.post()
                .uri("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1", articleId, i + 1)
                .retrieve();
        }
    }

    private void createComment(Long articleId, long commentCount) {
        for (int i = 0; i < commentCount; i++) {
            commentServiceClient.post()
                .uri("/v2/comments")
                .body(new CommentCreateRequest(articleId, "content", 1L))
                .retrieve();
        }
    }

    Long createArticle() {
        return articleServiceClient.post()
            .uri("/v1/articles")
            .body(new ArticleCreateRequest("title", "content", 1L, 1L))
            .retrieve()
            .body(ArticleResponse.class)
            .getArticleId();
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
    static class ArticleResponse {

        private Long articleId;
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {

        private Long articleId;
        private String content;
        //        private String parentCommentPath;
        private Long writerId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleViewCreateRequest {

        private Long articleId;
        private String content;
        private String parentCommentPath;
        private Long writerId;
    }
}
