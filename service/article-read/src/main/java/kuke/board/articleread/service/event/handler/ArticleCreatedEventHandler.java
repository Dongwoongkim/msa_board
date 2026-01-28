package kuke.board.articleread.service.event.handler;

import java.time.Duration;
import kuke.board.articleread.repository.ArticleIdListRepository;
import kuke.board.articleread.repository.ArticleQueryModel;
import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.articleread.repository.BoardArticleCountRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {

    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        /*
        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);
        // 이 시점에 목록에는 뜨지만, 아티클 쿼리 모델 조회는 불가

        articleQueryModelRepository.create(
            ArticleQueryModel.create(payload),
            Duration.ofDays(1)
        );
         */
        articleQueryModelRepository.create(
            ArticleQueryModel.create(payload),
            Duration.ofDays(1)
        );
        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> eventType) {
        return EventType.ARTICLE_CREATED.equals(eventType.getType());
    }
}
