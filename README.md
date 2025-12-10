## 🧑‍💻 개발 기간 & 팀원

### **개발 기간**
> 2025.12.10 (수) 09:00 ~ 2026.01.07 (수) 18:00

### **팀원**
| <a href="https://github.com/HongRae-Kim"><img src="https://github.com/HongRae-Kim.png" width="100"/></a> | <a href="https://github.com/kimwonmin"><img src="https://github.com/kimwonmin.png" width="100"/></a> | <a href="https://github.com/Boojw"><img src="https://github.com/Boojw.png" width="100"/></a> | <a href="https://github.com/seopgyu"><img src="https://github.com/seopgyu.png" width="100"/></a> | <a href="https://github.com/ascal34"><img src="https://github.com/ascal34.png" width="100"/></a> | <a href="https://github.com/KyeongwonBang"><img src="https://github.com/KyeongwonBang.png" width="100"/></a> |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **김홍래** | **김원민** | **부종우** | **김규섭** | **조영주** | **방경원** |
| 팀장 | 팀원 | 팀원 | 팀원 | 팀원 | 팀원 |

---

### **🔄 작업 순서**

1. **이슈 생성** → 작업 단위 정의
2. **브랜치 생성** → main 브랜치에서 이슈별 작업 브랜치 생성
3. **Commit & Push**
4. **PR 생성 & 코드 리뷰** → 최소 2명 승인 필요
5. **Merge & 브랜치 정리**
    - 리뷰 완료 후 main 브랜치로 Merge
    - Merge 후 이슈별 작업 브랜치 삭제

---

### 🧑‍💻 코드 리뷰 시간

- 오전 스크럼 이후 진행, 오후 4시
- 이슈 및 pr에 내용 자세하게 작성
    - 팀원들이 이해하기 쉽게
    - 이후 궁금한 사항 마이크 키고 질문!

---

## 📌네이밍 규칙 총정리

| 구분 | 네이밍 예시 | 내용 |
| --- | --- | --- |
| 이슈 이름 | [FE/feat] 로그인 기능 추가 | 영역 + 목적 + 설명 |
| 브랜치 이름 | feat/#12/login-api | type + issue-number + 설명 |
| 커밋 메시지 | feat(auth): JWT 기반 인증 구현 | type(scope): subject |
| PR 이름 | [FE/feat] 로그인 기능 추가 | 이슈명과 동일하게 작성 권장 |

1. **이슈 네이밍 규칙**
    - 제목 규칙 : `[작업영역/목적] 설명`
    - 예시 : `[BE/fix] 상품 목록 조회 오류 수정`
    - 본문은 템플릿에 맞춰서 작성

2. **브랜치 네이밍 규칙**
    - 생성 기준 : `main` 브랜치에서 생성
    - 명명 규칙  : `타입/#이슈번호/설명`
    - 예시: `feat/#12/login-api'
    
3. **PR 네이밍 규칙**
    - 제목 규칙 : `[작업영역/목적] 설명`
    - 예시 : `[BE/feat] 로그인 기능 추가`
    - 본문은 **📄 PR 템플릿**에 맞춰서 작성 + close #이슈넘버

4. **커밋 메시지 네이밍 규칙**
    - `타입(범위) : 작업내용`
    - 예시: `fix(order): 결제 버그 수정`

    | 타입 | 설명 |
    | --- | --- |
    | FE | Frontend 작업 |
    | BE | Backend 작업 |
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

### **📄 PR 템플릿**

- 개요
- 주요 변경 내용
    - 어떤 부분이 추가되었고 변경되었는지 구체적으로 작성
- 스크린샷 (선택 사항)
    - API 테스트 결과나 변경된 화면 등 시각적인 자료
- [이슈링크](https://github.com/prgrms-be-devcourse/NBE7-9-3-Team10/issues/61)
- [pr링크](https://github.com/prgrms-be-devcourse/NBE7-9-3-Team10/pull/83)


---

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

