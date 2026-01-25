package kuke.board.articleread.service.event.handler;

import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleUpdatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUpdatedEventHandler implements EventHandler<ArticleUpdatedEventPayload> {

    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleUpdatedEventPayload> event) {
        ArticleUpdatedEventPayload payload = event.getPayload();
        articleQueryModelRepository.read(payload.getArticleId())
            .ifPresent(articleQueryModel -> {
                articleQueryModel.updateBy(payload);
                articleQueryModelRepository.update(articleQueryModel);
            });
    }

    @Override
    public boolean supports(Event<ArticleUpdatedEventPayload> eventType) {
        return EventType.ARTICLE_UPDATED.equals(eventType.getType());
    }
}
