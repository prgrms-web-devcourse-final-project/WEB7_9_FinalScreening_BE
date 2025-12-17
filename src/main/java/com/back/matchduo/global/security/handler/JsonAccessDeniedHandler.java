package com.back.matchduo.global.security.handler;

import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        CustomErrorResponse body = CustomErrorResponse.builder()
                .status(CustomErrorCode.INVALID_USER_ROLE.getStatus().value())
                .code(CustomErrorCode.INVALID_USER_ROLE.name())
                .message(CustomErrorCode.INVALID_USER_ROLE.getMessage())
                .build();

        response.setStatus(CustomErrorCode.INVALID_USER_ROLE.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
