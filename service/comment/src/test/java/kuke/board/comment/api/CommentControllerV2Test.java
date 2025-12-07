package kuke.board.comment.api;

import java.util.List;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

class CommentControllerV2Test {

    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my content1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my content2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my content3", response2.getPath(), 1L));

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());

    }

    CommentResponse create(CommentCreateRequestV2 request) {
        return restClient.post()
            .uri("/v2/comments")
            .body(request)
            .retrieve()
            .body(CommentResponse.class);
    }

    @Test
    void read() {
        /**
         * response1.getPath() = 00001
         * response1.getCommentId() = 256048005131923456
         * 	response2.getPath() = 0000100000
         * 	response2.getCommentId() = 256048005278724096
         * 		response3.getPath() = 000010000000000
         * 		response3.getCommentId() = 256048005329055744
         */
        CommentResponse response = restClient.get()
            .uri("/v2/comments/{commentId}", 256048005131923456L)
            .retrieve()
            .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
            .uri("/v2/comments/{commentId}", 256048005131923456L)
            .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
            .uri("/v2/comments?articleId=1&pageSize=10 &page=1")
            .retrieve()
            .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /**
         * response.getCommentCount() = 101
         * comment.getCommentId() = 256050223271096327
         * comment.getCommentId() = 256050223313039369
         * comment.getCommentId() = 256050223313039370
         * comment.getCommentId() = 256050223313039375
         * comment.getCommentId() = 256050223313039379
         * comment.getCommentId() = 256050223317233731
         * comment.getCommentId() = 256050223329816630
         * comment.getCommentId() = 256050223329816640
         * comment.getCommentId() = 256050223329816649
         * comment.getCommentId() = 256050223329816659
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response = restClient.get()
            .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponse>>() {
            });

        System.out.println("firstPage");
        for (CommentResponse commentResponse : response) {
            System.out.println("commentResponse.getCommentId() = " + commentResponse.getCommentId());
        }

        String lastPath = response.getLast().getPath();
        List<CommentResponse> response2 = restClient.get()
            .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
            .retrieve()
            .body(new ParameterizedTypeReference<List<CommentResponse>>() {
            });

        System.out.println("secondPage");
        for (CommentResponse commentResponse : response2) {
            System.out.println("commentResponse.getCommentId() = " + commentResponse.getCommentId());
        }

    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {

        private Long articleId;
        private String content;
        private String parentCommentPath;
        private Long writerId;
    }
}