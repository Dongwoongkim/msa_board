package kuke.board.like.service;

import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleLikedEventPayload;
import kuke.board.common.event.payload.ArticleUnlikedEventPayload;
import kuke.board.common.outboxmessagerelay.OutboxEventPublisher;
import kuke.board.common.snowflake.Snowflake;
import kuke.board.like.entity.ArticleLike;
import kuke.board.like.entity.ArticleLikeCount;
import kuke.board.like.repository.ArticleLikeCountRepository;
import kuke.board.like.repository.ArticleLikeRepository;
import kuke.board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private final Snowflake snowflake = new Snowflake();
    private final OutboxEventPublisher publisher;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleLikeCountRepository articleLikeCountRepository;

    public ArticleLikeResponse read(Long articleId, Long userId) {
        return ArticleLikeResponse.from(articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
            .orElseThrow());
    }

    /**
     * update 구문
     */
    @Transactional
    public void likePessimisticLock1(Long articleId, Long userId) {
        ArticleLike articleLike = ArticleLike.create(snowflake.nextId(), articleId, userId);
        articleLikeRepository.save(articleLike);

        int result = articleLikeCountRepository.increase(articleId);

        if (result == 0) {
            // 최초 요청 시에는 1로 초기화 하여 삽입
            // 트래픽 몰리는 경우 유실될 수 있으므로, 게시글 생성 시점에 0으로 초기화해서 삽입해두는 방법도 있음.
            articleLikeCountRepository.save(
                ArticleLikeCount.init(articleId, 1L)
            );
        }

        publisher.publish(
            EventType.ARTICLE_LIKED,
            ArticleLikedEventPayload.builder()
                .articleLikeId(articleLike.getArticleLikeId())
                .articleId(articleLike.getArticleId())
                .userId(articleLike.getUserId())
                .createdAt(articleLike.getCreatedAt())
                .articleLikeCount(count(articleLike.getArticleId()))
                .build(),
            articleLike.getArticleId()
        );
    }

    /**
     * update 구문
     */
    @Transactional
    public void unlikePessimisticLock1(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
            .ifPresent(articleLike -> {
                articleLikeRepository.delete(articleLike);
                articleLikeCountRepository.decrease(articleId); // dirty check로 save하지 않아도 ok

                publisher.publish(
                    EventType.ARTICLE_UNLIKED,
                    ArticleUnlikedEventPayload.builder()
                        .articleLikeId(articleLike.getArticleLikeId())
                        .articleId(articleLike.getArticleId())
                        .userId(articleLike.getUserId())
                        .createdAt(articleLike.getCreatedAt())
                        .articleLikeCount(count(articleLike.getArticleId()))
                        .build(),
                    articleLike.getArticleId()
                );
            });
    }

    /**
     * select ... for update
     */
    @Transactional
    public void likePessimisticLock2(Long articleId, Long userId) {
        ArticleLike articleLike = ArticleLike.create(snowflake.nextId(), articleId, userId);

        articleLikeRepository.save(articleLike);

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
            .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));

        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount); // dirty check로 save하지 않아도 ok
    }

    /**
     * select ... for update
     */
    @Transactional
    public void unlikePessimisticLock2(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
            .ifPresent(articleLike -> {
                articleLikeRepository.delete(articleLike);
                ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
                    .orElseThrow();
                articleLikeCount.decrease();
                articleLikeCountRepository.save(articleLikeCount); // dirty check로 save하지 않아도 ok
            });
    }

    /**
     * optimistic Lock
     */
    @Transactional
    public void likeOptimisticLock(Long articleId, Long userId) {
        ArticleLike articleLike = ArticleLike.create(snowflake.nextId(), articleId, userId);

        articleLikeRepository.save(articleLike);

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId)
            .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));

        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount); // dirty check로 save하지 않아도 ok
    }

    /**
     * optimistic Lock
     */
    @Transactional
    public void unlikeOptimisticLock(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
            .ifPresent(articleLike -> {
                articleLikeRepository.delete(articleLike);
                ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseThrow();
                articleLikeCount.decrease();
                articleLikeCountRepository.save(articleLikeCount); // dirty check로 save하지 않아도 ok
            });
    }

    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
            .map(ArticleLikeCount::getLikeCount)
            .orElse(0L);
    }
}
