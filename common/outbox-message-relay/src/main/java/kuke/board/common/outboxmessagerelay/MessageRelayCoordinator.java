package kuke.board.common.outboxmessagerelay;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {

    private static final String MSG_RELAY_KEY = "message-relay-coordinator::app-list::%s";
    private final StringRedisTemplate redisTemplate;
    private final String APP_ID = UUID.randomUUID().toString();
    private final int PING_INTERVAL_SECONDS = 3;
    private final int PING_FAILURE_THRESHOLD = 3;

    @Value("${spring.application.name}")
    private String appName;

    public AssignedShard assignShards() {
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHAR_COUNT);
    }

    private List<String> findAppIds() {
        return redisTemplate.opsForZSet()
            .reverseRange(generateKey(), 0, -1)
            .stream()
            .sorted()
            .toList();
    }

    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();
            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID);
            conn.zRemRangeByScore(
                key,
                Double.NEGATIVE_INFINITY,
                Instant.now().minusSeconds(PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            ); // 3초마다 핑을 쏴서 죽어있으면 삭제
            return null;
        });
    }

    @PreDestroy
    public void leave() {
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }

    private String generateKey() {

        return MSG_RELAY_KEY.formatted(appName);
    }
}
