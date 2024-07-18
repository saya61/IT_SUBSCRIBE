package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.ProfileImage;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ProfileImageRepostiory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class ProfileImageService {
    private final ProfileImageRepostiory profileImageRepository;
    private final MemberRepository memberRepository;

    // 업로드 경로를 application.properties에서 가져옴
    @Value("${upload.path}")
    private String uploadDir;

    public ProfileImage updateProfileImage(Member currentMember, MultipartFile file) throws IOException {
        // 업로드 경로를 절대 경로로 변환 및 정규화
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        // 업로드 경로를 File 객체로 변환
        File uploadDirFile = new File(uploadPath.toString());
        // 업로드 디렉토리가 존재하지 않으면 디렉토리 생성
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // 업로드된 파일의 원래 이름을 가져옴
        String fileName = file.getOriginalFilename();
        // 서버에 파일 저장
        File destFile = new File(uploadPath + File.separator + fileName); // 저장할 파일 경로 생성
        file.transferTo(destFile); // 업로드된 파일을 해당 경로에 저장

        // DB에 파일 저장
        ProfileImage profileImage = new ProfileImage(); // 새로운 ProfileImage 객체 생성
        profileImage.setFileName(fileName); // 파일 이름 설정
        profileImage.setFileUrl(destFile.getAbsolutePath()); // 파일 절대 경로 설정
        profileImageRepository.save(profileImage); // ProfileImage 객체를 DB에 저장

        // Member 테이블의 profileImage 값 변경
        if (!file.isEmpty()) { // 파일이 비어있지 않으면
            currentMember.setProfileImage(profileImage); // 현재 회원의 프로필 이미지 설정
        }
        memberRepository.save(currentMember); // 현재 회원 정보를 DB에 저장

        return profileImage; // 저장된 ProfileImage 객체를 반환
    }
}
