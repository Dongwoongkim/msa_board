package kuke.board.hotarticle.consumer;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType.Topic;
import kuke.board.hotarticle.service.HotArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {

    private final HotArticleService hotArticleService;

    /**
     * @param message
     * @param ack
     * @KafkaListener : 설정된 토픽들로부터 메시지가 발행(Publish)되면 이를 감지하여 메소드를 자동으로 실행
     * @args는 카프카 브로커에서 가져온 원시 데이터로 Message Converter가 자동으로 채움
     */
    @KafkaListener(topics = {
        Topic.KUKE_BOARD_ARTICLE,
        Topic.KUKE_BOARD_COMMENT,
        Topic.KUKE_BOARD_LIKE,
        Topic.KUKE_BOARD_VIEW,
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[HotArticleEventConsumer] received message: {}", message);

        // json -> event 객체 convert
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            // event 처
            hotArticleService.handleEvent(event);
        }

        // 카프카 브로커에 명시적으로 이벤트 처리 성공을 알림
        // 처리 후 offset 커밋
        ack.acknowledge();
    }
}
