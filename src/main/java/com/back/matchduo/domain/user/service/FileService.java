package com.back.matchduo.domain.user.service;

import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile({"dev", "test"}) //prod 제외 → S3랑 충돌 방지
public class FileService {

    //하드코딩 경로 제거 / 명확한 업로드 루트
    private static final String UPLOAD_DIR = "uploads/profile";

    public String upload(MultipartFile file) {

        // INVALID_REQUEST 에러 코드
        if (file == null || file.isEmpty()) {
            throw new CustomException(CustomErrorCode.INVALID_FILE);
        }

        //디렉토리 경로
        Path dirPath = Paths.get(UPLOAD_DIR);

        //파일명 UUID 처리
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(fileName);

        try {
            //createDirectories IOException 처리
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            //transferTo IOException 처리
            file.transferTo(filePath.toFile());

        } catch (IOException e) {
            //RuntimeException
            throw new CustomException(CustomErrorCode.FILE_UPLOAD_FAILED);
        }

        //저장 경로 반환 (DB 저장용)
        return "/uploads/profile/" + fileName;
    }
}