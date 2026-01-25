package kuke.board.articleread.service.response;

import java.time.LocalDateTime;
import kuke.board.articleread.repository.ArticleQueryModel;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ArticleReadResponse {

    // article
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // comment
    private Long articleCommentCount;

    // article-like
    private Long articleLikeCount;

    // view
    private Long articleViewCount;

    public static ArticleReadResponse from(ArticleQueryModel articleQueryModel, Long articleViewCount) {
        ArticleReadResponse response = new ArticleReadResponse();
        response.articleId = articleQueryModel.getArticleId();
        response.title = articleQueryModel.getTitle();
        response.content = articleQueryModel.getContent();
        response.boardId = articleQueryModel.getBoardId();
        response.writerId = articleQueryModel.getWriterId();
        response.createdAt = articleQueryModel.getCreatedAt();
        response.modifiedAt = articleQueryModel.getModifiedAt();
        response.articleCommentCount = articleQueryModel.getArticleCommentCount();
        response.articleLikeCount = articleQueryModel.getArticleLikeCount();
        response.articleViewCount = articleViewCount;

        return response;
    }
}
