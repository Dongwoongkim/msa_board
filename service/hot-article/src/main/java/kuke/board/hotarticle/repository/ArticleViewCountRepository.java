package kuke.board.hotarticle.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleViewCountRepository {

    // hot-article::article::{articleId}::view-count
    private static final String KEY_FORMAT = "hot-article::article::%s::view-count";

    private final StringRedisTemplate redisTemplate;

    public void createOrUpdate(Long articleId, Long viewCount, Duration ttl) {
        redisTemplate.opsForValue().set(generateKey(articleId), String.valueOf(viewCount), ttl);
    }

    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? 0L : Long.parseLong(result);
    }

    private String generateKey(Long articleId) {
        return String.format(KEY_FORMAT, articleId);
    }
}
