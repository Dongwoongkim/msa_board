package kuke.board.comment.service;

import static java.util.function.Predicate.not;

import java.util.List;
import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        Comment parentComment = findParent(request);

        Comment comment = commentRepository.save(
            Comment.create(
                snowflake.nextId(),
                request.getContent(),
                parentComment == null ? null : parentComment.getCommentId(),
                request.getArticleId(),
                request.getWriterId()
            )
        );

        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();

        if (parentCommentId == null) {
            return null;
        }

        return commentRepository.findById(parentCommentId)
            .filter(not(Comment::getDeleted))
            .filter(Comment::isRoot)
            .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(commentRepository.findById(commentId)
            .orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
            .filter(not(Comment::getDeleted))
            .ifPresent(comment -> {
                if (hasChildren(comment)) {
                    comment.delete(); // 논리 delete
                } else {
                    delete(comment); // 물리 delete
                }
            });
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    public void delete(Comment comment) {
        commentRepository.delete(comment);

        // 루트 댓글이 아닌경우 -> 논리적으로 삭제된 부모 댓글 재귀적으로 물리 delete
        if (!comment.isRoot()) {
            commentRepository.findById(comment.getParentCommentId())
                .filter(Comment::getDeleted)
                .filter(not(this::hasChildren))
                .ifPresent(this::delete);
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
            commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize)
                .stream()
                .map(CommentResponse::from)
                .toList(),
            commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
            commentRepository.findAllInfiniteScroll(articleId, limit) :
            commentRepository.findAllInfiniteScroll(articleId, limit, lastParentCommentId, lastCommentId);

        return comments.stream()
            .map(CommentResponse::from)
            .toList();
    }
}
