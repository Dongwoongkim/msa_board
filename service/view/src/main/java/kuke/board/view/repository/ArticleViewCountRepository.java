package kuke.board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleViewCountRepository {

    // view::article::{article_id}::view_count
    private static final String KEY_FORMAT = "view::article::%s::view_count";

    private final StringRedisTemplate redisTemplate;

    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(String.format(KEY_FORMAT, articleId));

        return result == null ? 0L : Long.parseLong(result);
    }

    public Long increase(Long articleId) {
        return redisTemplate.opsForValue().increment(String.format(KEY_FORMAT, articleId), 1);
    }

    private String generateKey(long articleId) {
        return String.format(KEY_FORMAT, articleId);
    }
}
