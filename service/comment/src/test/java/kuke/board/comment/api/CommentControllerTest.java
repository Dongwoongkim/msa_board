package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class CommentControllerTest {

    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse reseponse1 = createComment(new CommentCreateRequest(1L, "my Content1", null, 1L));
        CommentResponse reseponse2 = createComment(new CommentCreateRequest(1L, "my Content2", reseponse1.getCommentId(), 1L));
        CommentResponse reseponse3 = createComment(new CommentCreateRequest(1L, "my Content2", reseponse1.getCommentId(), 1L));

        /**
         * commentId = 255611882070941696
         * 	ㄴcommentId = 255611882452623360
         * 	ㄴcommentId = 255611882494566400
         */
        System.out.println("commentId = %s".formatted(reseponse1.getCommentId()));
        System.out.println("\tcommentId = %s".formatted(reseponse2.getCommentId()));
        System.out.println("\tcommentId = %s".formatted(reseponse3.getCommentId()));
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
            .uri("/v1/comments/{commentId}", 255611882070941696L)
            .retrieve()
            .body(CommentResponse.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        /**
         * commentId = 255611882070941696 (1)
         * 	ㄴcommentId = 255611882452623360
         * 	ㄴcommentId = 255611882494566400
         */
        restClient.delete()
            .uri("/v1/comments/{commentId}", 255611882494566400L)
            .retrieve();
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
            .uri("/v1/comments")
            .body(request)
            .retrieve()
            .body(CommentResponse.class);
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {

        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}