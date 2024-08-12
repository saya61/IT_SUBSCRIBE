package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.ProfileImage;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ProfileImageRepository;
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
    private final ProfileImageRepository profileImageRepository;
    private final MemberRepository memberRepository;

    // 업로드 경로를 application.properties에서 가져옴
    @Value("${upload.path}")
    private String uploadDir;

    public ProfileImage updateProfileImage(Member currentMember, MultipartFile file) throws IOException {
        // 업로드 경로를 1. 절대 경로로 변환 2. 정규화
        // 1. 상대 경로는 현재 작업 디렉토리에 따라 달라질 수 있음
        // 절대 경로는 파일 시스템의 루트부터 시작하는 명확한 경로를 제공 => 파일을 저장하거나 읽을 때 정확한 위치를 보장함
        // 2. 경로를 정규화하면 경로의 불필요한 부분을 제거하여 깔끔한 경로를 제공함
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        // 업로드 경로를 File 객체로 변환 (mkdirs() 사용하기 위해)
        File uploadDirFile = new File(uploadPath.toString());
        // 업로드 디렉토리가 존재하지 않으면 디렉토리 생성
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // 업로드된 파일의 원래 이름을 가져옴
        String fileName = file.getOriginalFilename();
        // 파일 저장 경로 설정
        // File.separator를 사용하면 운영체제에 상관없이 올바른 디렉토리 구분자를 사용할 수 있음
        // 윈도우에서는 백슬래시(\)가 되고, 유닉스에서는 슬래시(/)가 됨
        File destFile = new File(uploadPath + File.separator + fileName);
        // 업로드된 파일을 해당 경로에 저장
        file.transferTo(destFile);

        // destFile 경로를 Path 객체로 변환 (파일 시스템 경로를 좀 더 유연하고 강력하게 다룰 수 있음)
        Path destPath = Paths.get(destFile.getPath());
        // DB에 파일 저장
        ProfileImage profileImage = new ProfileImage(); // 새로운 ProfileImage 객체 생성
        profileImage.setFileName(fileName); // 파일 이름 설정
        // 부모 디렉토리 경로의 마지막 부분 + 파일 이름을 결합하여 파일 상대 경로 설정
        // => 클라이언트가 쉽게 파일을 다운로드하거나 표시할 수 있음
        profileImage.setFileUrl("/"+destPath.getParent().getFileName()+"/"+fileName);
        profileImageRepository.save(profileImage); // ProfileImage 객체를 DB에 저장

        // Member 테이블의 profileImage 값 변경
        if (!file.isEmpty()) { // 파일이 비어있지 않으면
            currentMember.setProfileImage(profileImage); // 현재 회원의 프로필 이미지 설정
        }
        memberRepository.save(currentMember); // 현재 회원 정보를 DB에 저장

        return profileImage; // 저장된 ProfileImage 객체를 반환
    }
}
