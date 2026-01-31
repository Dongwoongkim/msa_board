package kuke.board.articleread.cache;

import java.time.Duration;
import lombok.Getter;

@Getter
public class OptimizedCacheTTL {

    public static final long PHYSICAL_Ttl_DELAY_SECONDS = 5;
    private Duration logicalTTL;
    private Duration physicalTTL;

    public static OptimizedCacheTTL of(long ttlSeconds) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.logicalTTL = Duration.ofSeconds(ttlSeconds);
        optimizedCacheTTL.physicalTTL = optimizedCacheTTL.logicalTTL.plusSeconds(PHYSICAL_Ttl_DELAY_SECONDS);
        return optimizedCacheTTL;
    }
}
