package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.user.entity.Verification;
import com.back.matchduo.domain.user.repository.VerificationRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    private final VerificationRepository verificationRepository;
    private final JavaMailSender mailSender;

    // 인증번호 생성 + 저장 + 이메일 발송
    @Transactional
    public void createAndSendVerificationCode(String email) {

        verificationRepository.findByEmail(email)
                .ifPresent(verificationRepository::delete);

        String code = generateVerificationCode();

        Verification verification = Verification.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        verificationRepository.save(verification);

        sendVerificationMailAsync(email, code);
    }

    // 이메일 비동기 전송
    @Async("mailExecutor")
    void sendVerificationMailAsync(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[MatchDuo] 이메일 인증 코드");
            message.setText("""
                    안녕하세요. MatchDuo 입니다.
                    
                    회원가입을 위해 아래 인증 코드를 입력해주세요.
                    
                    인증코드: %s
                    
                    해당 코드는 5분간 유효합니다.
                    """.formatted(code));

            mailSender.send(message);
            log.info("[이메일 인증] 인증코드 전송 완료: {}", email);

        } catch (Exception e) {
            log.error("이메일 인증코드 전송 실패", e);
            throw new CustomException(CustomErrorCode.EMAIL_SEND_FAILED);
        }
    }

    // 인증 코드 검증
    @Transactional
    public void verifyCode(String email, String code) {

        Verification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_VERIFICATION_CODE));

        if (verification.isExpired()) {
            throw new CustomException(CustomErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        if (!verification.getCode().equals(code)) {
            throw new CustomException(CustomErrorCode.INVALID_VERIFICATION_CODE);
        }

        verification.markAsVerified();
    }

    // 인증 완료 여부 확인
    public boolean isVerified(String email) {
        return verificationRepository.findByEmail(email)
                .map(Verification::isVerified)
                .orElse(false);
    }

    // 인증 코드 생성
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHAR_SET.charAt(random.nextInt(CHAR_SET.length())));
        }
        return sb.toString();
    }
}