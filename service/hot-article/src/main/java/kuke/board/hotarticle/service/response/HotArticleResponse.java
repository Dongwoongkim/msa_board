package kuke.board.hotarticle.service.response;

import java.time.LocalDateTime;
import kuke.board.hotarticle.client.ArticleClient.ArticleResponse;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class HotArticleResponse {

    private Long articleId;
    private String title;
    private LocalDateTime createAt;

    public static HotArticleResponse from(ArticleResponse articleResponse) {
        HotArticleResponse hotArticleResponse = new HotArticleResponse();
        hotArticleResponse.articleId = articleResponse.getArticleId();
        hotArticleResponse.title = articleResponse.getTitle();
        hotArticleResponse.createAt = articleResponse.getCreatedAt();
        return hotArticleResponse;
    }
}
