package com.sw.journal.journalcrawlerpublisher.dto;

import com.sw.journal.journalcrawlerpublisher.domain.ProfileImage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;

@Data
@Getter
@Setter
public class CommentDTO {
    private Long id;
    private String content;
    private Long articleId;
    private Long memberId;
    private String memberNickname;
    private String profileImageURL;
}
