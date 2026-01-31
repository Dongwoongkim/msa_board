package kuke.board.articleread.cache;

import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OptimizedCacheTTLTest {

    @Test
    void ofTest() {
        // given
        long ttlSeconds = 10;

        // when
        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);

        // then
        Assertions.assertThat(optimizedCacheTTL.getLogicalTTL()).isEqualTo(Duration.ofSeconds(ttlSeconds));
        Assertions.assertThat(optimizedCacheTTL.getPhysicalTTL()).isEqualTo(
            Duration.ofSeconds(ttlSeconds).plusSeconds(OptimizedCacheTTL.PHYSICAL_Ttl_DELAY_SECONDS)
        );
    }
}