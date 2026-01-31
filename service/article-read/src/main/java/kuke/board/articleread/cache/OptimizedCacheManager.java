package kuke.board.articleread.cache;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import kuke.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {

    private static final String DELIMITER = "::";
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    /**
     * 최적화된 캐시 처리 메인 로직 1. 캐시 히트 시 데이터 반환 2. 논리적 만료 시 락을 획득한 한 명만 갱신 진행 3. 나머지 요청은 갱신 중에도 기존 데이터를 반환하여 성능 유지 (Cache Stampede 방지)
     */
    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType, OptimizedCacheOriginDataSupplier<?> originDataSupplier)
        throws Throwable {

        // 1. 전달받은 인자들을 조합하여 Redis 키 생성 (ex. article::123)
        String key = generateKey(type, args);

        // 2. Redis에서 데이터 조회
        String cachedData = redisTemplate.opsForValue().get(key);

        // 3. [Cache Miss] 캐시에 데이터가 전혀 없는 경우: 즉시 DB 조회 후 갱신
        if (cachedData == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 4. 역직렬화 진행 (JSON -> 객체)
        OptimizedCache optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);

        // 5. 역직렬화 실패 시 대응 (방어 코드)
        if (optimizedCache == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 6. [Cache Hit - Fresh] 논리적 만료 시간이 지나지 않았다면 바로 반환
        if (!optimizedCache.isExpired()) {
            return optimizedCache.parseData(returnType);
        }

        // 7. [Cache Hit - Stale] 논리적 만료는 되었으나 데이터가 있는 경우
        // 갱신을 위한 락 획득 시도 (동일 키에 대해 한 명만 성공)
        if (!optimizedCacheLockProvider.lock(key)) {
            // 락 획득 실패 시: 이미 누군가 갱신 중이므로, 현재의 '오래된 데이터'를 반환하여 DB 부하 차단
            return optimizedCache.parseData(returnType);
        }

        // 8. 락 획득 성공 시: DB에서 최신 데이터를 가져와 캐시 업데이트
        try {
            return refresh(originDataSupplier, key, ttlSeconds);
        } finally {
            // 9. 갱신 완료 후 락 해제
            optimizedCacheLockProvider.unlock(key);
        }
    }

    /**
     * DB(Origin)에서 데이터를 가져와 Redis에 물리적/논리적 TTL과 함께 저장
     */
    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier, String key, long ttlSeconds) throws Throwable {
        // 실제 DB 조회 수행
        Object originData = originDataSupplier.get();

        // 물리적 TTL과 논리적 TTL 설정 정보 생성
        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);

        // 논리적 만료 시간이 포함된 캐시 객체 생성
        OptimizedCache optimizedCache = OptimizedCache.of(originData, optimizedCacheTTL.getLogicalTTL());

        // Redis 저장 (물리적 TTL 적용)
        redisTemplate.opsForValue().set(
            key,
            DataSerializer.serialize(optimizedCache),
            optimizedCacheTTL.getPhysicalTTL()
        );

        return originData;
    }

    /**
     * 서비스 타입과 파라미터들을 구분자(::)로 연결하여 유니크한 키 생성
     */
    private String generateKey(String prefix, Object[] args) {
        // prefix = a, args = [1,2]
        // key a::1::2
        return prefix + DELIMITER +
            Arrays.stream(args)
                .map(String::valueOf)
                .collect(joining(DELIMITER));

    }
}