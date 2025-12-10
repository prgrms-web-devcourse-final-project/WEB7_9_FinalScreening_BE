### 📌네이밍 규칙 총정리

| 구분 | 네이밍 예시 | 내용 |
| --- | --- | --- |
| 이슈 이름 | [FE/feat] 로그인 기능 추가 | 영역 + 목적 + 설명 |
| 브랜치 이름 | feat/#12/login-api | type + issue-number + 설명 |
| 커밋 메시지 | feat(auth): JWT 기반 인증 구현 | type(scope): subject |
| PR 이름 | [FE/feat] 로그인 기능 추가 | 이슈명과 동일하게 작성 권장 |

## 🏷️ 이슈 이름 규칙 (Issue Naming)

```css
[작업영역/목적] 설명
```

✅ **예시**

```bash
[FE/feat] 로그인 기능 추가
[BE/fix] 상품 목록 조회 오류 수정
```

| 구분 | 설명 |
| --- | --- |
| FE | Frontend 작업 |
| BE | Backend 작업 |
| feat | 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 |
| refactor | 리펙토링 |
| test | 테스트코드 작성, 수정 |

### 브랜치 종류

- **main** : 실제 배포 가능한 코드가 존재하는 브랜치
- **feature** : 새로운 기능을 개발할 때 사용하는 브랜치
- **refactor** : 작성된 코드를 리팩토링 할때 사용하는 브랜치
- **fix** : 버그수정이나, 간단한 수정사항 적용할 때 사용하는 브랜치

### 브랜치 네이밍 규칙

```
type/issue-number/description
```

- `type`: 브랜치의 목적 (ex. feat, fix, refactor,test 등)
- `issue-number` (선택 사항): GitHub Issue 번호가 있다면 기입하여 작업을 추적
- `description`: 브랜치에서 수행하는 작업을 간결하게 설명 (영문 소문자, 단어는 하이픈으로 연결)

✅ **예시**

```smalltalk
feat/#12/login-api
fix/#34/order-bug
```

### 커밋 메시지 네이밍 규칙

✔️ 팀원 누구나 커밋 히스토리를 보고 변경 사항을 쉽게 이해할 수 있도록 일관된 커밋 메시지 규칙을 따름

```
type(scope): subject
```

- `type`: 커밋의 종류. (아래 표 참고)
- `scope` (선택 사항): 변경된 코드의 범위를 명시. (예: `auth`, `user-api`, `db`)
- `subject`: 커밋에 대한 간결한 요약

✅**예시**

```makefile
feat(auth): JWT 기반 인증/인가 구현
fix(order): 결제 버그 수정
docs: README 배포 방법 추가
```

- ✔️**참고**
    
    
    | 커밋 타입 | 설명 |
    | --- | --- |
    | `feat` | 새로운 기능 추가 |
    | `fix` | 버그 수정 |
    | `docs` | 문서 수정 ([README.md](http://readme.md/), API 문서 등) |
    | `style` | 코드 포맷팅, 세미콜론 누락 등 (코드 로직 변경 없음) |
    | `refactor` | 코드 리팩토링 (기능 변경 없음) |
    | `test` | 테스트 코드 추가 또는 수정 |
    | `chore` | 빌드 스크립트, 패키지 매니저 설정 등 기타 변경 사항 |
    | `rename` | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 |
    | `remove` | 파일을 삭제하는 작업만 수행한 경우 |
    | `init` | 초기 생성, 꼭 필요한 라이브러리 설치하는 경우 |

### Pull Request (PR) 네이밍 규칙

✅**예시**

```java
[FE/feat] 로그인 기능 추가
```

### **📄 PR 템플릿**

- 개요
- 주요 변경 내용
    - 어떤 부분이 추가되었고 변경되었는지 구체적으로 작성
- 스크린샷 (선택 사항)
    - API 테스트 결과나 변경된 화면 등 시각적인 자료
- [이슈링크](https://github.com/prgrms-be-devcourse/NBE7-9-3-Team10/issues/61)
- [pr링크](https://github.com/prgrms-be-devcourse/NBE7-9-3-Team10/pull/83)

### 👥 PR 리뷰 안내

- 팀원 간 PR 등록 시, 다른 팀원분들께서(가능하면 이전에 PR리뷰 해주신 횟수가 적으셨던 분 환영!) 조금만 시간을 내어 코드를 살펴봐주시고, 간단하게나마 리뷰를 작성해주시면 감사하겠습니다!
- main 브랜치 보호 규칙
- pr이 올라가는 즉시, pr리뷰를 담당해주시는 분 께서 잠깐 시간을 내주셔서 리뷰

### ✅ Merge 기준

- PR리뷰로 달아주신 사이드 이펙트나 리팩토링 거리 해결
- ‘LGTM’, 또는 ‘좋은 거 같습니다.’ << 같은 이상없음 메시지를 다른 팀원분께 2건 이상 Approve 받으면 Merge 준비 완료

### 📌참고

- 네이밍 종류 목록
    - **PascalCase** (파스칼 케이스)
        - 첫글자와 이어지는 단어의 첫글자를 대문자로 표기하는 방법
        - 예) `GoodPerson`, `MyKakaoCake`, `IAmDeveloper`
    - **camelCase** (카멜 케이스)
        - 첫단어는 소문자로 표기하지만, 이어지는 단어의 첫글자는 대문자로 표기하는 방법
        - 예) `goodPerson`, `myKakaoCake`, `iAmDeveloper`
    - **snake_case** (스네이크 케이스)
        - 모든 단어를 소문자로 표기하고, 단어를 언더바(_) 로 연결하는 방법
        - 예) `good_person`, `my_kakao_cake`, `i_am_developer`
    - **kebab-case** (케밥 케이스)
        - 모든 단어를 소문자로 표기하고, 단어를 대시(-) 로 연결하는 방법
        - 예) `good-person`, `my-kakao-cake`, `i-am-developer`
        - 보통 파일명이나 폴더명을 만들때 사용하는 편.
    - **UPPER_CASE** (어퍼 케이스)
        - 모든 단어를 대문자로 표기하고, 단어를 언더바(_) 로 연결하는 방법
        - 예) `GOOD_PERSON`, `MY_KAKAO_CAKE`, `I_AM_DEVELOPER`
        - 대부분의 프로그래밍에서 상수변수(constant variable)의 이름을 이렇게 사용.
        

| 폴더 | camelCase |
| --- | --- |
| 변수 | camelCase |
| 상수 | UPPER_CASE |
| Boolean | is 접두사 사용하기
ex) `isVisible` |
| 함수 | camelCase
동사+명사 |
| 클래스 | PascalCase |
| 컴포넌트 | PascalCase |
| 엔티티 | PascalCase |

### 🧑‍💻 코드 리뷰 시간

- 오전 스크럼 이후 진행, 오후 4시
- 이슈 및 pr에 내용 자세하게 작성
    - 팀원들이 이해하기 쉽게
    - 이후 궁금한 사항 마이크 키고 질문!
