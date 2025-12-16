package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.request.UserSignUpRequest;
import com.back.matchduo.domain.user.service.UserSignUpService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSignUpController {
    private final UserSignUpService userSignUpService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody UserSignUpRequest request
    ) {
        userSignUpService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
