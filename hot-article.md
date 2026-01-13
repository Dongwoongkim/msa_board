1. 사용자가 Like Service API 호출
   POST /articles/{articleId}/like

2. Like Service가 이벤트 발행
   Event {
   eventId: 123,
   type: ARTICLE_LIKED,
   payload: {
   articleId: 456,
   articleLikeCount: 42
   }
   }

3. Event를 JSON으로 직렬화
   {"eventId":123,"type":"ARTICLE_LIKED","payload":{...}}

4. Kafka의 "kuke-board-like" 토픽에 발행

5. Hot-Article Service가 메시지 수신
   HotArticleEventConsumer.listen()

6. JSON → Event 객체로 역직렬화

7. EventHandler 선택
   → ArticleLikedEventHandler 선택 (supports() 반환 true)

8. handle() 실행
   → Redis에 좋아요 수 저장
   → articleLikeCountRepository.createOrUpdate(456, 42, ...)

9. HotArticleScoreUpdater가 인기도 점수 재계산

10. 인기 게시물 랭킹 업데이트