package kuke.board.comment.api;

import java.util.List;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
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

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
            .uri("/v1/comments?articleId=1&page=1&pageSize=10")
            .retrieve()
            .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.println("comment.getCommentId() = " + comment.getCommentId());
            } else {
                System.out.println("\tcomment.getCommentId() = " + comment.getCommentId());
            }
        }

        /**
         * 1번 페이지 수행 결과
         * response.getCommentCount() = 101
         * comment.getCommentId() = 255621438515490816
         * 	comment.getCommentId() = 255621438536462342
         * comment.getCommentId() = 255621438515490817
         * 	comment.getCommentId() = 255621438536462340
         * comment.getCommentId() = 255621438515490818
         * 	comment.getCommentId() = 255621438536462336
         * comment.getCommentId() = 255621438515490819
         * 	comment.getCommentId() = 255621438536462341
         * comment.getCommentId() = 255621438515490820
         * 	comment.getCommentId() = 255621438536462338
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
            .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponse>>() {
            });

        System.out.println("firstPage");
        for (CommentResponse commentResponse : response1) {
            if (commentResponse.getCommentId().equals(commentResponse.getParentCommentId())) {
                System.out.println("comment.getCommentId() = " + commentResponse.getCommentId());
            } else {
                System.out.println("\tcomment.getCommentId() = " + commentResponse.getCommentId());
            }
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
            .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                .formatted(lastParentCommentId, lastCommentId))
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponse>>() {
            });

        System.out.println("secondPage");
        for (CommentResponse commentResponse : response2) {
            if (commentResponse.getCommentId().equals(commentResponse.getParentCommentId())) {
                System.out.println("comment.getCommentId() = " + commentResponse.getCommentId());
            } else {
                System.out.println("\tcomment.getCommentId() = " + commentResponse.getCommentId());
            }
        }

        /**
         * firstPage
         * comment.getCommentId() = 255621438515490816
         * 	comment.getCommentId() = 255621438536462342
         * comment.getCommentId() = 255621438515490817
         * 	comment.getCommentId() = 255621438536462340
         * comment.getCommentId() = 255621438515490818
         *
         * secondPage
         * 	comment.getCommentId() = 255621438536462336
         * comment.getCommentId() = 255621438515490819
         * 	comment.getCommentId() = 255621438536462341
         * comment.getCommentId() = 255621438515490820
         * 	comment.getCommentId() = 255621438536462338
         */
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