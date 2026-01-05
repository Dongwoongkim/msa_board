package kuke.board.view.service;

import java.time.Duration;
import kuke.board.view.repository.ArticleViewCountRepository;
import kuke.board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleViewService {

    private static final int BACK_UP_BATCH_SIZE = 100;
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository;
    private final ArticleViewCountRepository articleViewCountRepository;
    private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;

    public Long increase(Long articleId, Long userId) {
        boolean lock = articleViewDistributedLockRepository.lock(articleId, userId, TTL);

        // 분산 락이 없으면 조회수 리턴
        if (!lock) {
            return articleViewCountRepository.read(articleId);
        }

        Long count = articleViewCountRepository.increase(articleId);

        // 100개 단위로 백업
        if (count % BACK_UP_BATCH_SIZE == 0) {
            articleViewCountBackUpProcessor.backUp(articleId, count);
        }

        return count;
    }

    public Long count(Long articleId) {
        return articleViewCountRepository.read(articleId);
    }
}
