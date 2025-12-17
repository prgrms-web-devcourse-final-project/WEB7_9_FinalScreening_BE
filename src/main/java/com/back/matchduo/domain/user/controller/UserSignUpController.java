package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.request.UserSignUpRequest;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.domain.user.service.EmailService;
import com.back.matchduo.domain.user.service.UserSignUpService;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSignUpController {
    private final UserSignUpService userSignUpService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(
            @Valid
            @RequestBody
            UserSignUpRequest request
    ) {
        userSignUpService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //인증번호 보내기 버튼
    @Operation(summary = "이메일 인증 요청 (인증번호 발송)")
    @PostMapping("/email/verify-request")
    public ResponseEntity<Void> requestEmailVerification(
            @RequestParam
            @Email
            String email
    ) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(CustomErrorCode.DUPLICATE_EMAIL);
        }

        emailService.createAndSendVerificationCode(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //인증 버튼
    @Operation(summary = "이메일 인증번호 확인")
    @PostMapping("/email/verify-confirm")
    public ResponseEntity<Void> confirmEmailVerification(
            @RequestParam
            @Email
            String email,

            @RequestParam
            @NotBlank
            String code
    ) {
        emailService.verifyCode(email, code);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
