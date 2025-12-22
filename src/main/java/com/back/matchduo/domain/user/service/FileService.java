package com.back.matchduo.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    // 로컬 테스트용 경로 (실제 존재하는 폴더여야 함. 없으면 에러 납니다)
    // 배포 시에는 application-prod.yml 등에서 외부 경로로 설정하는 것이 좋습니다.
    private final String uploadDir = "C:/matchduo_uploads/";

    public String upload(MultipartFile file) {
        if (file.isEmpty()) return null;

        try {
            // 1. 파일명 중복 방지 (UUID 사용)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);

            // 2. 폴더가 없으면 생성
            Files.createDirectories(path.getParent());

            // 3. 물리적 파일 저장
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // 4. DB에 저장할 "웹 접근 경로" 반환
            // 나중에 브라우저에서 /images/파일명 으로 접근하게 됩니다.
            return "/images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }
}