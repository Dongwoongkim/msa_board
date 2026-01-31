package kuke.board.articleread.cache;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimizedCacheLockProvider {

    private static final String KEY_PREFIX = "optimized-cache-lock::";
    private static final Duration LOCK_TTL = Duration.ofSeconds(3);
    private final StringRedisTemplate redisTemplate;

    public boolean lock(String key) {
        return redisTemplate.opsForValue().setIfAbsent(
            generateLockKey(key),
            "",
            LOCK_TTL
        );
    }

    public void unlock(String key) {
        redisTemplate.delete(generateLockKey(key));
    }

    private String generateLockKey(String key) {
        return KEY_PREFIX + key;
    }

}
