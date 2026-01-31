package kuke.board.articleread.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;

class OptimizedCacheTest {

    @Test
    void parseDataTest() {
        parseDataTest("data", 10);
        parseDataTest(1L, 10);
        parseDataTest(new TestData("data"), 10);
    }

    void parseDataTest(Object data, long ttlSeconds) {
        // given
        OptimizedCache optimizedCache = OptimizedCache.of(data, Duration.ofSeconds(ttlSeconds));
        System.out.println("optimizedCache: " + optimizedCache);

        // when
        Object resolvedData = optimizedCache.parseData(data.getClass());

        // then
        System.out.println("resolvedData: " + resolvedData);
        assertThat(resolvedData).isEqualTo(data);

        System.out.println();
    }

    @Test
    void isExpiredTest() {
        assertThat(OptimizedCache.of("dd", Duration.ofDays(-30)).isExpired()).isTrue();
        assertThat(OptimizedCache.of("dd", Duration.ofDays(30)).isExpired()).isFalse();
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestData {

        String testData;
    }
}