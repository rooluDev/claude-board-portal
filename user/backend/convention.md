# 코딩 컨벤션

## Java 코드 컨벤션 (Google Java Style Guide 기반)

### 1. 파일 구조

#### 1.1 파일 구성 순서
```java
1. 라이센스 또는 저작권 정보 (선택)
2. package 문
3. import 문
4. 정확히 하나의 최상위 클래스
```

#### 1.2 Import 문
- import 문에 와일드카드(*) 사용 금지
- 정적 import와 일반 import 구분
- 그룹별 정렬:
    1. 정적 import
    2. java.*
    3. javax.*
    4. 서드파티 라이브러리
    5. 프로젝트 내부 패키지

```java
// Good
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.board.domain.Board;

// Bad
import java.util.*;
```

### 2. 네이밍 컨벤션

#### 2.1 패키지명
- 모두 소문자
- 언더스코어(_) 사용 금지
- 의미 있는 이름 사용

```java
// Good
package com.board.service;
package com.board.repository;

// Bad
package com.board.Service;
package com.board.my_repository;
```

#### 2.2 클래스명
- UpperCamelCase (파스칼 케이스)
- 명사 또는 명사구 사용
- 테스트 클래스: 테스트 대상 클래스명 + Test

```java
// Good
public class BoardService { }
public class BoardCreateDto { }
public class BoardServiceTest { }

// Bad
public class boardService { }
public class Board_Service { }
```

#### 2.3 메서드명
- lowerCamelCase
- 동사 또는 동사구 사용
- 의미를 명확하게 전달

```java
// Good
public Board findById(Long id) { }
public void deleteBoard(Long id) { }
public boolean isValidPassword(String password) { }

// Bad
public Board get(Long id) { }
public void delete(Long id) { }
public boolean checkPw(String password) { }
```

#### 2.4 변수명
- lowerCamelCase
- 의미 있는 이름 사용
- 약어 사용 최소화

```java
// Good
private Long boardId;
private String userName;
private List<Board> boardList;

// Bad
private Long id;
private String usrNm;
private List<Board> list;
```

#### 2.5 상수명
- UPPER_SNAKE_CASE
- static final 필드에 사용

```java
// Good
private static final int MAX_FILE_COUNT = 3;
private static final String DEFAULT_ENCODING = "UTF-8";

// Bad
private static final int maxFileCount = 3;
private static final String DefaultEncoding = "UTF-8";
```

### 3. 포매팅

#### 3.1 들여쓰기
- 4칸 스페이스 사용 (탭 사용 금지)

#### 3.2 중괄호
- K&R 스타일 (Egyptian brackets)
- 빈 블록도 중괄호 사용

```java
// Good
if (condition) {
    doSomething();
}

// Bad
if (condition) 
{
    doSomething();
}

// Bad
if (condition) doSomething();
```

#### 3.3 줄 길이
- 최대 100자 권장
- 120자 초과 금지

#### 3.4 공백
- 연산자 앞뒤에 공백
- 쉼표 뒤에 공백
- 메서드 이름과 여는 괄호 사이 공백 없음

```java
// Good
int sum = a + b;
List<String> names = Arrays.asList("Alice", "Bob");
public void doSomething(int param) {

// Bad
int sum=a+b;
List<String> names = Arrays.asList("Alice","Bob");
public void doSomething (int param) {
```

### 4. 주석

#### 4.1 JavaDoc
- public 메서드와 클래스에 필수
- @param, @return, @throws 사용

```java
/**
 * 게시글을 ID로 조회합니다.
 *
 * @param boardId 조회할 게시글 ID
 * @return 게시글 정보
 * @throws BoardNotFoundException 게시글이 존재하지 않을 경우
 */
public Board findById(Long boardId) {
    // 구현
}
```

#### 4.2 일반 주석
- 복잡한 로직 설명에 사용
- 코드 자체로 의미가 명확하면 주석 불필요

```java
// Good
// 비밀번호를 SHA-256으로 해싱
String hashedPassword = hashPassword(password);

// Bad - 불필요한 주석
// boardId를 설정
board.setBoardId(boardId);
```

### 5. 어노테이션

#### 5.1 위치
- 각 어노테이션은 별도 줄에 작성
- 매개변수가 없는 단일 어노테이션은 같은 줄 가능

```java
// Good
@Override
@Transactional
public void createBoard(BoardCreateDto dto) { }

// Also Good
@Override public void toString() { }

// Bad
@Override @Transactional public void createBoard() { }
```

### 6. 예외 처리

#### 6.1 구체적인 예외 사용
```java
// Good
catch (FileNotFoundException e) {
    log.error("File not found", e);
}

// Bad
catch (Exception e) {
    // 너무 광범위
}
```

#### 6.2 예외 무시 금지
```java
// Bad
try {
    doSomething();
} catch (Exception e) {
    // 무시
}

// Good
try {
    doSomething();
} catch (IOException e) {
    log.error("Failed to process file", e);
    throw new FileProcessException("File processing failed", e);
}
```

### 7. 기타 Best Practices

#### 7.1 메서드 길이
- 한 메서드는 하나의 기능만 수행
- 50줄 이하 권장

#### 7.2 매개변수 개수
- 4개 이하 권장
- 그 이상이면 DTO 사용 고려

#### 7.3 불변성
- 가능한 final 사용
- DTO는 불변 객체로 설계 권장

```java
// Good
public class BoardCreateDto {
    private final Long categoryId;
    private final String title;
    
    // getter만 제공
}
```

## SQL 컨벤션 (MyBatis XML)

### 1. 대소문자
- SQL 키워드: 대문자
- 테이블명, 컬럼명: 소문자

```xml
<!-- Good -->
<select id="findById" resultType="Board">
    SELECT board_id, title, content
    FROM board
    WHERE board_id = #{boardId}
</select>

<!-- Bad -->
<select id="findById" resultType="Board">
    select BOARD_ID, TITLE, CONTENT
    from Board
    where board_id = #{boardId}
</select>
```

### 2. 들여쓰기
- 4칸 스페이스
- 쿼리 가독성을 위해 절별로 줄바꿈

```xml
<select id="findAll" resultType="Board">
    SELECT 
        b.board_id,
        b.title,
        b.content,
        b.created_at,
        c.category_name
    FROM board b
        INNER JOIN category c ON b.category_id = c.category_id
    WHERE b.created_at BETWEEN #{from} AND #{to}
        <if test="keyword != null">
            AND (b.title LIKE CONCAT('%', #{keyword}, '%')
                 OR b.content LIKE CONCAT('%', #{keyword}, '%'))
        </if>
    ORDER BY b.created_at DESC
    LIMIT #{limit} OFFSET #{offset}
</select>
```

### 3. 파라미터
- #{} 사용 (파라미터 바인딩)
- ${} 사용 금지 (SQL Injection 위험)

```xml
<!-- Good -->
WHERE board_id = #{boardId}

<!-- Bad -->
WHERE board_id = ${boardId}
```

## Thymeleaf 컨벤션

### 1. 속성 순서
```html
<input 
    type="text"
    id="title"
    name="title"
    class="form-control"
    th:field="*{title}"
    th:value="${board.title}"
    required />
```

### 2. 조건문/반복문 가독성
```html
<!-- Good -->
<div th:if="${board != null}">
    <h2 th:text="${board.title}">제목</h2>
</div>

<tr th:each="board : ${boards}">
    <td th:text="${board.title}">제목</td>
</tr>
```

### 3. URL 생성
```html
<!-- Good - @{} 사용 -->
<a th:href="@{/board/view/{id}(id=${board.boardId})}">상세보기</a>

<!-- Bad -->
<a href="/board/view?id=1">상세보기</a>
```

## JavaScript 컨벤션

### 1. 네이밍
- 변수/함수: camelCase
- 상수: UPPER_SNAKE_CASE

```javascript
// Good
const maxFileSize = 2 * 1024 * 1024;
function validateForm() { }

// Bad
const MAX_FILE_SIZE = 2 * 1024 * 1024; // 변수는 camelCase
function ValidateForm() { }
```

### 2. 세미콜론
- 항상 사용

```javascript
// Good
const name = "John";
const age = 30;

// Bad
const name = "John"
const age = 30
```

### 3. 문자열
- 일관되게 작은따옴표 또는 큰따옴표 사용

```javascript
// Good (일관성)
const message = "Hello World";
const error = "Invalid input";

// Bad (혼용)
const message = "Hello World";
const error = 'Invalid input';
```

### 4. 함수
- 화살표 함수 우선 사용

```javascript
// Good
const numbers = [1, 2, 3];
const doubled = numbers.map(n => n * 2);

// Acceptable
function calculateSum(a, b) {
    return a + b;
}
```

## 파일 구조 컨벤션

### 1. 패키지 구조
```
com.board
├── config          // 설정 클래스
├── controller      // 컨트롤러
├── service         // 서비스
├── repository      // 레포지토리(Mapper)
├── domain          // 엔티티
├── dto             // DTO
├── exception       // 예외 클래스
└── util            // 유틸리티
```

### 2. 리소스 구조
```
resources
├── application.yml
├── mybatis
│   ├── mybatis-config.xml
│   └── mapper
│       ├── BoardMapper.xml
│       └── FileMapper.xml
├── static
│   ├── css
│   ├── js
│   └── uploads
└── templates
    ├── board
    ├── fragments
    └── error
```

## Git 커밋 메시지 컨벤션

### 커밋 메시지 구조
```
<type>: <subject>

<body>

<footer>
```

### Type
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅
- `refactor`: 리팩토링
- `test`: 테스트 코드
- `chore`: 빌드, 설정 변경

### 예시
```
feat: 게시글 목록 조회 API 구현

- 페이징 처리 추가
- 검색 조건(카테고리, 날짜, 키워드) 지원

Resolves: #123
```