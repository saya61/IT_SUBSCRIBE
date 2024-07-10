package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.constant.Role;
import com.sw.journal.journalcrawlerpublisher.domain.Member;
import com.sw.journal.journalcrawlerpublisher.domain.ProfileImage;
import com.sw.journal.journalcrawlerpublisher.repository.MemberRepository;
import com.sw.journal.journalcrawlerpublisher.repository.ProfileImageRepostiory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class ProfileImageService {
    private final ProfileImageRepostiory profileImageRepostiory;
    private final MemberRepository memberRepository;

    public ProfileImage updateProfileImage(Member currentMember, MultipartFile file) throws IOException {
        // 이미지 경로
        String uploadPath = "C:/Users/ekals/Documents/IT_SUBSCRIBE/profile/";  // application.properties에 upload.path = "경로"로 명시 가능
        String fileName = file.getOriginalFilename();

        // 서버에 파일 저장
        file.transferTo(new File(uploadPath+fileName));

        // DB에 파일 저장
        ProfileImage profileImage = new ProfileImage();
        profileImage.setFileName(fileName);
        profileImage.setFileUrl(uploadPath+fileName);
        profileImageRepostiory.save(profileImage);

        // Member 테이블의 profileImage 값 변경
        if (!file.isEmpty()) {
            currentMember.setProfileImage(profileImage);
        }
        memberRepository.save(currentMember);

        return profileImage;
    }
}
