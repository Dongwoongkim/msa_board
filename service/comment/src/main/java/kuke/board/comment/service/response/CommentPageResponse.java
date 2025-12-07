package kuke.board.comment.service.response;

import java.util.List;
import lombok.Getter;

@Getter
public class CommentPageResponse {

    private List<CommentResponse> comments;
    private Long commentCount;

    public static CommentPageResponse of(List<CommentResponse> comments, Long commentCount) {
        CommentPageResponse commentPageResponse = new CommentPageResponse();
        commentPageResponse.comments = comments;
        commentPageResponse.commentCount = commentCount;
        return commentPageResponse;
    }
}
