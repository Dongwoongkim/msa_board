package kuke.board.articleread.service.event.handler;

import java.time.Duration;
import kuke.board.articleread.repository.ArticleQueryModel;
import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {

    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        articleQueryModelRepository.create(
            ArticleQueryModel.create(payload),
            Duration.ofDays(1)
        );
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> eventType) {
        return EventType.ARTICLE_CREATED.equals(eventType.getType());
    }
}
