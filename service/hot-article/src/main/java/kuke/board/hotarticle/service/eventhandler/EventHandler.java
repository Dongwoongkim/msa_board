package kuke.board.hotarticle.service.eventhandler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {

    // 이벤트 처리 로직
    void handle(Event<T> event);

    // 이 핸들러가 해당 이벤트를 처리할 수 있는지 확인
    boolean supports(Event<T> eventType);

    // 이벤트에서 게시물 ID 추출
    Long findArticleId(Event<T> event);
}
