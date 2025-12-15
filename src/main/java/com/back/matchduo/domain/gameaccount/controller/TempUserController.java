package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 임시 User 등록용 컨트롤러
 * User 도메인 담당자가 회원가입 API를 구현하기 전까지 테스트용으로 사용
 * TODO: User 도메인 담당자가 회원가입 API 구현 후 삭제 예정
 */
@Slf4j
@RestController
@RequestMapping("/api/temp/users")
@RequiredArgsConstructor
public class TempUserController {

    private final UserRepository userRepository;

    /**
     * 임시 User 등록 (테스트용)
     * @param request email, password, nickname, verification_code
     * @return 생성된 User 정보
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTempUser(
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String nickname = request.get("nickname");
            String verificationCode = request.get("verification_code");

            // 필수 필드 검증
            if (email == null || password == null || nickname == null || verificationCode == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "필수 필드가 누락되었습니다. (email, password, nickname, verification_code)");
                return ResponseEntity.badRequest().body(error);
            }

            // 중복 체크는 저장 시 unique constraint 위반으로 처리
            // (User 엔티티의 email, nickname이 unique이므로)

            // 리플렉션을 사용하여 User 엔티티 생성
            User user = createUserWithReflection(email, password, nickname, verificationCode);

            // User 저장
            User savedUser = userRepository.save(user);

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("nickname", savedUser.getNickname());
            response.put("message", "임시 User가 생성되었습니다.");

            log.info("임시 User 생성 완료: id={}, email={}, nickname={}", 
                    savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            // Unique constraint 위반 (이메일 또는 닉네임 중복)
            log.warn("User 생성 실패: 중복된 이메일 또는 닉네임", e);
            Map<String, Object> error = new HashMap<>();
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("email")) {
                error.put("error", "이미 존재하는 이메일입니다.");
            } else if (errorMessage != null && errorMessage.contains("nickname")) {
                error.put("error", "이미 존재하는 닉네임입니다.");
            } else {
                error.put("error", "이미 존재하는 이메일 또는 닉네임입니다.");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            log.error("임시 User 생성 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "User 생성에 실패했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 리플렉션을 사용하여 User 엔티티 생성
     * User 엔티티를 수정하지 않고 protected 생성자를 호출하고 필드를 설정
     * BaseEntity를 상속받으므로 createdAt, updatedAt은 JPA Auditing이 자동으로 설정함
     */
    private User createUserWithReflection(String email, String password, String nickname, String verificationCode)
            throws Exception {
        // protected 생성자 가져오기
        Constructor<User> constructor = User.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        User user = constructor.newInstance();

        // 필드 설정
        // createdAt, updatedAt은 BaseEntity의 @EntityListeners(AuditingEntityListener.class)로 자동 설정됨
        setField(user, "email", email);
        setField(user, "password", password);
        setField(user, "nickname", nickname);
        setField(user, "verification_code", verificationCode);
        // BaseEntity의 isActive는 기본값이 true이므로 설정 불필요
        // BaseEntity의 deletedAt은 기본값이 null이므로 설정 불필요

        return user;
    }

    /**
     * 리플렉션을 사용하여 필드 값 설정
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    /**
     * 클래스 계층 구조를 따라가며 필드 찾기
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
}

