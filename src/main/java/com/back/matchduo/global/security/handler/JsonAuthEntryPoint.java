package com.back.matchduo.global.security.handler;

import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JsonAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        CustomErrorResponse body = CustomErrorResponse.builder()
                .status(CustomErrorCode.UNAUTHORIZED_USER.getStatus().value())
                .code(CustomErrorCode.UNAUTHORIZED_USER.name())
                .message(CustomErrorCode.UNAUTHORIZED_USER.getMessage())
                .build();

        response.setStatus(CustomErrorCode.UNAUTHORIZED_USER.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
