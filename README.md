# SPRING ADVANCED

## 📌 프로젝트 목적

Spring Boot 기반 프로젝트에서 전반적인 코드 개선, 예외 처리 개선, N+1 문제 해결, 테스트 코드 리팩토링을 통해 유지보수성과 안정성을 향상시키는 작업을 수행

---

## ✅ Lv 1. 코드 개선

### 1. Early Return 적용 - `AuthService.signup()`

#### 🔧 변경 전:

```java
String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
if (userRepository.existsByEmail(signupRequest.getEmail())) {
    throw new InvalidRequestException("이미 존재하는 이메일입니다.");
}
```

#### ✅ 변경 후:

```java
// email중복시, 회원가입이 불가능하므로,
// 이를 제일 먼저 확인하여, 회원가입 가능시 진행하는 연산을 사전에 방지
        
if (userRepository.existsByEmail(signupRequest.getEmail())) {
    throw new InvalidRequestException("이미 존재하는 이메일입니다.");
}
String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
```

> ✅ 회원가입 시 이미 존재하는 이메일이면 `passwordEncoder.encode()`가 호출되지 않도록 early return 리팩토링을 적용

---

### 2. 불필요한 if-else 제거 - `WeatherClient.getTodayWeather()`

#### 🔧 변경 전:

```java
if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
    throw new ServerException("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + responseEntity.getStatusCode());
} else {
    if (weatherArray == null || weatherArray.length == 0) {
        throw new ServerException("날씨 데이터가 없습니다.");
    }
}
```

#### ✅ 변경 후:

```java
// throw문을 통하여 현재 메서드를 종료시키기 때문에 else문을 중복으로 사용할 필요가 없음.
if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
    throw new ServerException("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + responseEntity.getStatusCode());
}
if (weatherArray == null || weatherArray.length == 0) {
    throw new ServerException("날씨 데이터가 없습니다.");
}
```

> ✅ 중첩된 else 문을 제거하여 가독성과 명확성을 높임

---

### 3. Validation 로직 DTO로 이동 - `UserService.changePassword()`

#### 🔧 변경 전:

```java
if (userChangePasswordRequest.getNewPassword().length() < 8 ||
        !userChangePasswordRequest.getNewPassword().matches("[0-9]+") ||
        !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
    throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
}
```

#### ✅ 변경 후:

```java
public class UserChangePasswordRequest {
    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = ".*[0-9].*", message = "숫자를 포함해야 합니다.")
    @Pattern(regexp = ".*[A-Z].*", message = "대문자를 포함해야 합니다.")
    private String newPassword;
}
```

> ✅ Spring Validation(`@Pattern`, `@Size`)을 이용해 유효성 검증을 DTO 레벨에서 처리함으로써 서비스 로직을 간결하게 유지

---

## ✅ Lv 2. N+1 문제 해결

* 기존: JPQL의 `fetch join`을 이용한 N+1 문제 해결 코드
* 개선: `@EntityGraph`를 이용하여 동일한 효과를 가지면서도 더 선언적인 방식으로 개선

#### 변경 전 (`TodoRepository`)

```java
@Query("SELECT t FROM Todo t JOIN FETCH t.user")
List<Todo> findAllWithUser();
```

#### 변경 후:

```java
@EntityGraph(attributePaths = {"user"})
List<Todo> findAll();
```

> ✅ `@EntityGraph`를 통해 간결하게 N+1 문제를 해결하고, JPQL의 복잡도를 줄임임

---

## ✅ Lv 3. 테스트 코드 리팩토링

### 1. PasswordEncoder 테스트 수정

* `matches()` 호출 시 인자의 순서가 잘못되어 항상 실패하던 테스트 수정

#### 수정 전:

```java
assertTrue(passwordEncoder.matches(encoded, raw));
```

#### 수정 후:

```java
assertTrue(passwordEncoder.matches(raw, encoded));
```

---

### 2. `ManagerServiceTest` - 예외 타입 및 메서드명 수정

* 기존 테스트: `NullPointerException`이 발생하고 있었으나, 실제 의도는 `InvalidRequestException` 검증
* 서비스 로직 수정:

```java
if (todo.getUser() == null) {
    throw new InvalidRequestException("일정을 만든 유저 정보가 유효하지 않습니다.");
}
```

* 테스트 메서드명 수정:

```java
기존 메서드명: manager_목록_조회_시_Todo가_없다면_NPE_에러를_던진다()
수정 메서드: manager_목록_조회_시_Todo가_없다면_InvalidRequestException_을_던진다()
```

---

### 3. `CommentServiceTest` - 없는 Todo 등록 시 예외 검증 수정

* 기대 예외를 `ServerException` → `InvalidRequestException`으로 변경
* 서비스 로직과 테스트 메시지를 일치시킴

---

### 4. `ManagerServiceTest` - `Todo.getUser() == null`인 경우 서비스 로직 수정

* 서비스 코드에서 null 체크 추가하여 NPE 방지

---

## ✅ 종합 결론

이번 개선 작업을 통해 코드의 **가독성**, **안정성**, **예외 일관성**, **테스트 신뢰도**를 높임

* 불필요한 로직 실행 방지 (early return)
* 복잡한 조건문 단순화
* 서비스 로직의 책임 분리 (DTO 검증)
* 테스트와 실제 동작 일치 보장
