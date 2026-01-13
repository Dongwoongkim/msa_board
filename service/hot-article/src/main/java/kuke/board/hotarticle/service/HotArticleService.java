package kuke.board.hotarticle.service;

import java.util.List;
import java.util.Objects;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import kuke.board.hotarticle.client.ArticleClient;
import kuke.board.hotarticle.repository.HotArticleListRepository;
import kuke.board.hotarticle.service.eventhandler.EventHandler;
import kuke.board.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {

    private final List<EventHandler> eventHandlers;
    private final ArticleClient articleClient; // 원본 Article 정보 가져오는 Client
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    private final HotArticleListRepository hotArticleListRepository;

    public void handleEvent(Event<EventPayload> event) {
        // 이벤트 타입에 맞는 핸들러 찾기
        EventHandler<EventPayload> eventHandler = findEventHandler(event);

        if (eventHandler == null) {
            return;
        }

        // 이벤트 타입에 따라 다르게 처리
        // ArticleCreated, ArticleDeleted 이벤트는 인기글 조회 시 해당 날짜에 생성된 게시글인지 검사하기 위한 이벤트
        if (isArticleCreatedOrDeleted(event)) {
            eventHandler.handle(event);
        } else {
            // 좋아요, 댓글, 조회 이벤트 : 점수 업데이트
            hotArticleScoreUpdater.update(event, eventHandler);
        }
    }

    // 해당하는 핸들러 찾기
    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
            .filter(eventHandler -> eventHandler.supports(event))
            .findAny()
            .orElse(null);
    }

    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType() || EventType.ARTICLE_DELETED == event.getType();
    }

    // client를 통해 간접적으로 article-read
    public List<HotArticleResponse> readAll(String dateStr) {
        return hotArticleListRepository.readAll(dateStr)
            .stream()
            .map(articleClient::read)
            .filter(Objects::nonNull)
            .map(HotArticleResponse::from)
            .toList();
    }
}
