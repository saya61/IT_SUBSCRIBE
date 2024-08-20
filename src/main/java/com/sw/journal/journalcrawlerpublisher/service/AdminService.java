package com.sw.journal.journalcrawlerpublisher.service;

import com.sw.journal.journalcrawlerpublisher.constant.CommentReportStatus;
import com.sw.journal.journalcrawlerpublisher.domain.*;
import com.sw.journal.journalcrawlerpublisher.dto.ArticleModificationDTO;
import com.sw.journal.journalcrawlerpublisher.dto.ReportDTO;
import com.sw.journal.journalcrawlerpublisher.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    // 댓글 로직 구현을 위한 Repository
    private final ReportRepository reportRepository;
    // 기사 로직 구현을 위한 Repository
    private final ArticleRepository articleRepository;
    private final ArticleRankRepository articleRankRepository;
    private final TagArticleRepository tagArticleRepository;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;

    public AdminService(ReportRepository reportRepository,
                        ArticleRepository articleRepository,
                        ArticleRankRepository articleRankRepository,
                        TagArticleRepository tagArticleRepository, ImageRepository imageRepository, TagRepository tagRepository) {
        this.reportRepository = reportRepository;
        this.articleRepository = articleRepository;
        this.articleRankRepository = articleRankRepository;
        this.tagArticleRepository = tagArticleRepository;
        this.imageRepository = imageRepository;
        this.tagRepository = tagRepository;
    }

    // 신고된 댓글 목록 조회
    public List<ReportDTO.Response> getReportedComments() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // 신고된 댓글 목록 조회 (페이징)
    public Page<ReportDTO.Response> getReportedCommentsWithPaging(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("reportDate")));

        Page<Report> reportPage;
        if (status == null || status.isEmpty()) {
            reportPage = reportRepository.findAll(pageable);
        } else {
            // 상태에 따라 필터링
            CommentReportStatus reportStatus = CommentReportStatus.valueOf(status.toUpperCase());
            reportPage = reportRepository.findByStatus(reportStatus, pageable);
        }

        return reportPage.map(this::convertToResponseDTO);
    }

    // 특정 신고된 댓글 조회
    public ReportDTO.Response getReportedComment(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report ID"));
        return convertToResponseDTO(report);
    }

    // 신고 상태 변경
    public ReportDTO.Response updateReportStatus(Long reportId, ReportDTO.Update updateStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report ID"));
        report.setStatus(updateStatus.getStatus());
        reportRepository.save(report);
        return convertToResponseDTO(report);
    }

    // Report -> ReportDTO.Response 변환
    private ReportDTO.Response convertToResponseDTO(Report report) {
        // 댓글 정보 가져오기
        Comment comment = report.getComment();
        Member commentator = comment.getMember(); // 댓글 작성자 정보
        Member reporter = report.getReporter(); // 신고자 정보

        // 댓글과 신고 정보로 Response 객체 생성
        return new ReportDTO.Response(
                comment.getId(),                                                        // 댓글 ID
                comment.getContent(),                                                   // 댓글 내용
                comment.getArticle().getId(),                                           // 기사 ID
                new ReportDTO.UserInfo(commentator.getId(), commentator.getUsername()), // 작성자 정보
                new ReportDTO.UserInfo(reporter.getId(), reporter.getUsername()),       // 신고자 정보
                report.getId(),                                                         // 신고 ID
                report.getReportDate(),                                                 // 신고 날짜
                report.getReason().getDescription(),                                    // 신고 사유
                report.getStatus().getDescription()                                     // 신고 상태
        );
    }

    //
    //
    //

    // 기사 수정 (제목&내용&태그)
    public void updateArticle(Long articleId, ArticleModificationDTO.Request articleRequest) {

        // Article
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다."));
        article.setTitle(articleRequest.getTitle());
        article.setContent(articleRequest.getContent());
        articleRepository.save(article);

        // TagArticle
        tagArticleRepository.deleteByArticleId(articleId);


        for (String tagName : articleRequest.getTags()) {
            // 태그가 이미 존재하는지 확인
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        // 태그가 존재하지 않으면 새로 생성
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });

            // 태그와 기사 사이의 매핑 저장
            TagArticle tagArticle = new TagArticle();
            tagArticle.setArticle(article);
            tagArticle.setTag(tag);
            tagArticleRepository.save(tagArticle);
        }
    }

    // 기사 삭제
    public void deleteArticle(Long articleId) {
        imageRepository.deleteByArticleId(articleId);
        tagArticleRepository.deleteByArticleId(articleId);
        articleRankRepository.deleteByArticleId(articleId);
        articleRepository.deleteById(articleId);
    }

}
