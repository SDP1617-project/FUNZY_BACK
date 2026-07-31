# SDP1617-project-BACK

## 로컬 개발 환경

### 요구 사항
- JDK 21

기본 프로필(`local`)은 H2(파일 기반, `.data/`)를 사용해서 별도 설치/실행 없이 바로 개발할 수 있고 재시작해도 데이터가 유지됩니다.

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. 테스트
```bash
./gradlew test
```

## 프로필

| 프로필 | 용도 | DB |
|---|---|---|
| `local` (기본) | 로컬 개발 | H2 파일 기반 (postgres 호환 모드, `.data/`) |
| `prod` | CI / 운영 | PostgreSQL (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경변수) |

CI는 실제 PostgreSQL(service container) 위에서 `prod` 프로필로 빌드/테스트를 실행해 postgres 전용 문법·기능 차이를 PR 단계에서 잡아냅니다.

## Redis

`spring-boot-starter-data-redis` 의존성과 `prod` 프로필의 접속 설정(`REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` 환경변수)만 준비된 상태입니다. 아직 redis를 사용하는 기능이 없어 로컬에 redis가 없어도 정상 실행/테스트됩니다. 실제로 사용하는 기능이 추가되면 그때 로컬 실행 방식(native 설치 vs docker)을 다시 정합니다.
