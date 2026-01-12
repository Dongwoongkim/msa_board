package kuke.board.hotarticle.repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleCreatedTimeRepository {

    // hot-article::article::{articleId}::created-time
    private static final String KEY_FORMAT = "hot-article::article::%s::created-time";

    private final StringRedisTemplate redisTemplate;

    public void createOrUpdate(Long articleId, LocalDateTime createdAt, Duration ttl) {
        redisTemplate.opsForValue()
            .set(generateKey(articleId),
                String.valueOf(createdAt.toInstant(ZoneOffset.UTC).toEpochMilli()),
                ttl
            );

        // 좋아요 이벤트가 왔는데, 이 이벤트에 대한 게시글이 오늘 게시글인지 확인하기 위해, 게시글 서비스에서 조회 필요
        // 하지만 게시글 생성 시간을 이벤트로 저장하고 있으면, 오늘 게시글인지 게시글 서비스를 통하지 않고 바로 알 수 있음
    }

    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    public LocalDateTime read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? null : LocalDateTime.ofInstant(
            Instant.ofEpochMilli(Long.valueOf(result)), ZoneOffset.UTC
        );
    }

    private String generateKey(Long articleId) {
        return String.format(KEY_FORMAT, articleId);
    }
}
