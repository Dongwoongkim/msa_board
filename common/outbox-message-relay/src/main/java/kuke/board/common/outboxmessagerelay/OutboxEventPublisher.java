package kuke.board.common.outboxmessagerelay;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final Snowflake outboxIdSnowflake = new Snowflake();
    private final Snowflake eventIdSnowflake = new Snowflake();
    private final ApplicationEventPublisher eventPublisher;

    /*
    service -> publisher.publish
     */
    public void publish(EventType type, EventPayload payload, Long shardKey) {
        // ex) article = 10, shardKey == articleId
        // 10 % 4 = 2(물리적 샤드)
        Outbox outbox = Outbox.create(
            outboxIdSnowflake.nextId(),
            type,
            Event.of(
                eventIdSnowflake.nextId(),
                type,
                payload
            ).toJson(),
            shardKey % MessageRelayConstants.SHAR_COUNT
        );

        eventPublisher.publishEvent(OutboxEvent.of(outbox));
    }

}
