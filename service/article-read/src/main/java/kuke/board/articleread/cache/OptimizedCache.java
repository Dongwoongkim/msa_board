package kuke.board.articleread.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Duration;
import java.time.LocalDateTime;
import kuke.board.common.dataserializer.DataSerializer;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OptimizedCache {

    private String data;
    private LocalDateTime expiredAt;

    public static OptimizedCache of(Object data, Duration ttl) {
        OptimizedCache cache = new OptimizedCache();
        cache.data = DataSerializer.serialize(data);
        cache.expiredAt = LocalDateTime.now().plus(ttl);
        return cache;
    }

    @JsonIgnore
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public <T> T parseData(Class<T> clazz) {
        return DataSerializer.deserialize(data, clazz);
    }
}
