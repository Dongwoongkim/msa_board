**Note**: 대용량 트래픽 처리와 마이크로서비스 아키텍처 학습을 위한 실습

### 학습 목표

- 마이크로서비스 아키텍처 설계 및 구현
- 이벤트 기반 아키텍처(Event-Driven Architecture) 이해
- CQRS 패턴을 통한 읽기/쓰기 분리
- 분산 시스템에서의 데이터 일관성 보장
- 고성능 캐싱 전략 구현
- 대용량 트래픽 처리 최적화

## ✨ 주요 기능

### 게시글 관리 (Article Service)

- 게시글 CRUD
- 게시판별 게시글 카운팅
- 페이지네이션

### 댓글 관리 (Comment Service)

- 댓글 CRUD
- 계층형 댓글 구조 (Materialized Path 패턴)
- 게시글별 댓글 수 집계

### 좋아요 기능 (Like Service)

- 게시글 좋아요/좋아요 취소
- 좋아요 수 집계
- 이벤트 발행을 통한 실시간 업데이트

### 조회수 관리 (View Service)

- 게시글 조회수 증가
- Redis 기반 조회수 집계
- 분산 락을 통한 동시성 제어
- 배치 처리를 통한 DB 부하 최소화

### 인기 게시글 (Hot Article Service)

- 실시간 인기 게시글 랭킹
- 가중치 기반 점수 계산 (조회수, 좋아요, 댓글, 시간)
- Redis Sorted Set을 활용한 효율적인 랭킹 관리

### 게시글 조회 최적화 (Article Read Service)

- CQRS 패턴 적용
- 읽기 전용 모델 최적화
- 멀티레벨 캐싱 전략
- 이벤트 기반 데이터 동기화

## 🏗️ 아키텍처

### 전체 시스템 아키텍처

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  Article        │      │  Comment        │      │  Like           │
│  Service        │      │  Service        │      │  Service        │
│  (Write)        │      │  (Write)        │      │  (Write)        │
└────────┬────────┘      └────────┬────────┘      └────────┬────────┘
         │                        │                        │
         └────────────────────────┴────────────────────────┘
                                  │
                            ┌─────▼─────┐
                            │   Kafka   │
                            │  Message  │
                            │   Broker  │
                            └─────┬─────┘
                                  │
         ┌────────────────────────┼────────────────────────┐
         │                        │                        │
┌────────▼────────┐      ┌────────▼────────┐      ┌────────▼────────┐
│  Article Read   │      │  Hot Article    │      │  View           │
│  Service        │      │  Service        │      │  Service        │
│  (Read)         │      │  (Ranking)      │      │  (Analytics)    │
└─────────────────┘      └─────────────────┘      └─────────────────┘
         │                        │                        │
         └────────────────────────┴────────────────────────┘
                                  │
                            ┌─────▼─────┐
                            │   Redis   │
                            │  (Cache)  │
                            └───────────┘
```

### 이벤트 흐름

```
1. 사용자 액션 (좋아요 클릭)
   ↓
2. Like Service API 호출
   ↓
3. Like Service가 이벤트 발행
   Event {
     eventId: 123,
     type: ARTICLE_LIKED,
     payload: {
       articleId: 456,
       articleLikeCount: 42
     }
   }
   ↓
4. Event를 JSON으로 직렬화
   ↓
5. Kafka의 "kuke-board-like" 토픽에 발행
   ↓
6. Hot-Article Service가 메시지 수신
   ↓
7. EventHandler 선택 (ArticleLikedEventHandler)
   ↓
8. Redis에 좋아요 수 저장 & 인기도 점수 재계산
   ↓
9. 인기 게시물 랭킹 업데이트
```

## 🛠️ 기술 스택

### Backend

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.2
- **Build Tool**: Gradle

### Database & Storage

- **RDBMS**: MySQL (게시글, 댓글, 좋아요 데이터)
- **Cache**: Redis (조회수, 인기 게시글, 읽기 최적화)
- **Message Queue**: Apache Kafka (이벤트 기반 통신)

### Infrastructure

- **JPA**: Spring Data JPA
- **ID Generator**: Snowflake Algorithm (분산 환경 고유 ID 생성)

## 📦 모듈 구조

### Service Modules

#### 1. Article Service (게시글 서비스)

```
service/article/
├── api/              # REST API Controllers
├── entity/           # JPA Entities
├── repository/       # Data Access Layer
└── service/          # Business Logic
```

**주요 클래스**

- `ArticleController`: 게시글 CRUD API
- `Article`: 게시글 엔티티
- `BoardArticleCount`: 게시판별 게시글 수 관리
- `PageLimitCalculator`: 페이지네이션 계산 유틸

#### 2. Comment Service (댓글 서비스)

```
service/comment/
├── api/              
│   ├── CommentController      # 일반 댓글 API
│   └── CommentControllerV2    # 계층형 댓글 API
├── entity/           
│   ├── Comment                # 일반 댓글 엔티티
│   ├── CommentV2              # 계층형 댓글 엔티티
│   └── CommentPath            # Materialized Path 구현
└── repository/       
```

**주요 특징**

- V1: 단순 댓글 구조
- V2: Materialized Path 패턴을 활용한 계층형 댓글

#### 3. Like Service (좋아요 서비스)

```
service/like/
├── api/              
├── entity/           
│   ├── ArticleLike           # 좋아요 엔티티
│   └── ArticleLikeCount      # 좋아요 수 집계
├── repository/       
└── service/          
```

**주요 특징**

- 좋아요/좋아요 취소 기능
- 이벤트 발행을 통한 실시간 반영

#### 4. View Service (조회수 서비스)

```
service/view/
├── api/              
├── entity/           
├── repository/       
│   ├── ArticleViewCountRepository              # Redis 조회수 관리
│   ├── ArticleViewCountBackUpRepository        # DB 백업
│   └── ArticleViewDistributedLockRepository    # 분산 락
└── service/          
    └── ArticleViewCountBackUpProcessor         # 배치 처리
```

**주요 특징**

- Redis 기반 실시간 조회수 처리
- 분산 락을 통한 동시성 제어
- 주기적인 DB 백업

#### 5. Hot Article Service (인기 게시글 서비스)

```
service/hot-article/
├── api/              
├── consumer/         # Kafka Event Consumer
├── repository/       # Redis 기반 데이터 저장소
│   ├── ArticleLikeCountRepository
│   ├── ArticleViewCountRepository
│   ├── ArticleCommentCountRepository
│   ├── ArticleCreatedTimeRepository
│   └── HotArticleListRepository
├── service/          
│   ├── eventhandler/          # 이벤트 핸들러
│   │   ├── ArticleCreatedEventHandler
│   │   ├── ArticleDeletedEventHandler
│   │   ├── ArticleLikedEventHandler
│   │   ├── ArticleUnlikedEventHandler
│   │   ├── ArticleViewEventHandler
│   │   ├── CommentCreatedEventHandler
│   │   └── CommentDeletedEventHandler
│   ├── HotArticleScoreCalculator  # 점수 계산 로직
│   └── HotArticleScoreUpdater     # 랭킹 업데이트
└── utils/            
```

**인기 게시글 점수 계산 공식**

```
score = (views * viewWeight + likes * likeWeight + comments * commentWeight) / timeDecay

timeDecay = (currentTime - createdTime) / 3600000 + 2
```

#### 6. Article Read Service (게시글 조회 서비스)

```
service/article-read/
├── api/              
├── cache/            # 최적화된 캐시 시스템
│   ├── OptimizedCache
│   ├── OptimizedCacheManager
│   ├── OptimizedCacheAspect
│   └── OptimizedCacheTTL
├── client/           # 타 서비스 호출
│   ├── ArticleClient
│   ├── CommentClient
│   ├── LikeClient
│   └── ViewClient
├── consumer/         # 이벤트 컨슈머
├── repository/       # 읽기 전용 모델
│   ├── ArticleQueryModel
│   ├── ArticleQueryModelRepository
│   ├── ArticleIdListRepository
│   └── BoardArticleCountRepository
└── service/          
    └── event/handler/    # 이벤트 핸들러
```

**주요 특징**

- CQRS 패턴 적용 (읽기/쓰기 분리)
- 멀티레벨 캐싱 (L1: 로컬, L2: Redis)
- 이벤트 기반 데이터 동기화
- 읽기 최적화된 쿼리 모델

### Common Modules

#### 1. Snowflake

```
common/snowflake/
└── Snowflake.java    # 분산 환경 고유 ID 생성기
```

**특징**

- Twitter Snowflake 알고리즘 구현
- 분산 환경에서 충돌 없는 고유 ID 생성
- 시간 기반 정렬 가능

#### 2. Event

```
common/event/
├── Event.java
├── EventType.java
├── EventPayload.java
└── payload/
    ├── ArticleCreatedEventPayload
    ├── ArticleDeletedEventPayload
    ├── ArticleLikedEventPayload
    ├── ArticleUnlikedEventPayload
    ├── ArticleUpdatedEventPayload
    ├── ArticleViewedEventPayload
    ├── CommentCreatedEventPayload
    └── CommentDeletedEventPayload
```

**특징**

- 이벤트 기반 아키텍처의 핵심 모듈
- 타입 안전한 이벤트 페이로드
- Kafka 토픽별 이벤트 분류

#### 3. Outbox Message Relay

```
common/outbox-message-relay/
├── Outbox.java                    # Outbox 엔티티
├── OutboxRepository.java          # Outbox 저장소
├── OutboxEventPublisher.java      # 이벤트 발행
├── MessageRelay.java              # 메시지 릴레이
├── MessageRelayCoordinator.java   # 릴레이 코디네이터
└── AssignedShard.java             # 샤드 할당
```

**특징**

- Transactional Outbox 패턴 구현
- 메시지 발행 보장
- 분산 환경에서의 샤딩 지원
- 메시지 중복 발행 방지

#### 4. Data Serializer

```
common/data-serializer/
└── DataSerializer.java    # JSON 직렬화/역직렬화
```

**특징**

- Jackson 기반 직렬화
- 이벤트 페이로드 변환
- 타입 안전한 역직렬화

## 🔑 핵심 구현 사항

### 1. 이벤트 기반 아키텍처

**Outbox 패턴을 통한 안정적인 이벤트 발행**

```java

@Transactional
public void createArticle(ArticleCreateRequest request) {
    // 1. 게시글 저장
    Article article = articleRepository.save(new Article(...));

    // 2. Outbox 테이블에 이벤트 저장 (같은 트랜잭션)
    outboxEventPublisher.publish(
        EventType.ARTICLE_CREATED,
        new ArticleCreatedEventPayload(article.getId(), ...)
    );

    // 3. 백그라운드 프로세스가 Outbox를 읽어 Kafka로 발행
}
```

### 2. CQRS 패턴

**읽기 전용 모델 최적화**

```java
// Write Model (Article Service)
@Entity
public class Article {

    @Id
    private Long id;
    private Long boardId;
    private String title;
    private String content;
    // ... 기타 필드
}

// Read Model (Article Read Service)
@RedisHash
public class ArticleQueryModel {

    @Id
    private Long id;
    private Long boardId;
    private String title;
    private String content;
    private Long viewCount;      // 조회수 (미리 계산)
    private Long likeCount;      // 좋아요 수 (미리 계산)
    private Long commentCount;   // 댓글 수 (미리 계산)
    // ... 읽기에 최적화된 구조
}
```

### 3. 멀티레벨 캐싱

**계층화된 캐시 전략**

```java

@OptimizedCacheable(
    cacheName = "article",
    ttl = @OptimizedCacheTTL(
        origin = 300,  // 5분
        empty = 60     // 1분 (빈 결과)
    )
)
public ArticleReadResponse getArticle(Long articleId) {
    // 1. L1 Cache (로컬 메모리) 확인
    // 2. L2 Cache (Redis) 확인
    // 3. DB 조회 후 캐시에 저장
}
```

### 4. 분산 락을 통한 동시성 제어

**Redis 기반 분산 락**

```java
public void incrementViewCount(Long articleId) {
    String lockKey = "lock:view:" + articleId;

    if (lockRepository.tryLock(lockKey, 3, TimeUnit.SECONDS)) {
        try {
            // 조회수 증가 로직
            viewCountRepository.increment(articleId);
        } finally {
            lockRepository.unlock(lockKey);
        }
    }
}
```

### 5. 계층형 댓글 (Materialized Path)

**효율적인 계층 구조 관리**

```java

@Entity
public class CommentV2 {

    @Id
    private Long id;

    @Embedded
    private CommentPath path;  // 예: "/1/5/12"

    // path를 이용한 조회 쿼리
    // SELECT * FROM comment WHERE path LIKE '/1/%'  -- 1번 댓글의 모든 하위 댓글
}
```

### 6. 인기 게시글 점수 계산

**가중치 기반 실시간 랭킹**

```java
public double calculateScore(Long articleId) {
    long views = viewCountRepository.get(articleId);
    long likes = likeCountRepository.get(articleId);
    long comments = commentCountRepository.get(articleId);
    long createdTime = createdTimeRepository.get(articleId);

    double timeDecay = calculateTimeDecay(createdTime);

    return (views * 1.0 + likes * 2.0 + comments * 3.0) / timeDecay;
}
```

## 🚀 실행 방법

### 사전 요구사항

- JDK 21
- Docker & Docker Compose (Redis, MySQL, Kafka)
- Gradle 8.x

### 1. 인프라 실행

```bash
# Docker Compose로 인프라 실행
docker-compose up -d

# 실행 확인
docker ps
```

### 2. 프로젝트 빌드

```bash
# 전체 프로젝트 빌드
./gradlew clean build

# 특정 서비스만 빌드
./gradlew :service:article:build
```

### 3. 서비스 실행

각 서비스를 개별적으로 실행합니다:

```bash
# Article Service
./gradlew :service:article:bootRun

# Comment Service
./gradlew :service:comment:bootRun

# Like Service
./gradlew :service:like:bootRun

# View Service
./gradlew :service:view:bootRun

# Hot Article Service
./gradlew :service:hot-article:bootRun

# Article Read Service
./gradlew :service:article-read:bootRun
```

### 4. API 테스트

**게시글 생성**

```bash
curl -X POST http://localhost:8080/articles \
  -H "Content-Type: application/json" \
  -d '{
    "boardId": 1,
    "title": "테스트 게시글",
    "content": "게시글 내용"
  }'
```

**게시글 조회 (최적화된 읽기)**

```bash
curl http://localhost:8081/articles/1
```

**인기 게시글 조회**

```bash
curl http://localhost:8085/hot-articles
```

## 📚 Point

### 1. 마이크로서비스 아키텍처

- 서비스 분리 기준 (도메인 주도 설계)
- 서비스 간 통신 전략 (동기 vs 비동기)
- 데이터 일관성 유지 방법

### 2. 이벤트 기반 아키텍처

- Transactional Outbox 패턴
- 이벤트 소싱
- 최종 일관성 (Eventual Consistency)

### 3. CQRS 패턴

- 읽기/쓰기 모델 분리
- 쿼리 최적화
- 데이터 동기화 전략

### 4. 고성능 최적화

- 멀티레벨 캐싱
- 분산 락
- 배치 처리
- 인덱싱 전략

### 5. 분산 시스템 설계

- Snowflake ID 생성기
- 샤딩 전략
- 메시지 큐 활용