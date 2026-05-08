package com.ojosama.notice.application.service;

import com.ojosama.notice.application.dto.command.CreateNoticeCommand;
import com.ojosama.notice.application.dto.command.UpdateNoticeCommand;
import com.ojosama.notice.application.dto.result.NoticeResult;
import com.ojosama.notice.domain.exception.NoticeErrorCode;
import com.ojosama.notice.domain.exception.NoticeException;
import com.ojosama.notice.domain.model.entity.Notice;
import com.ojosama.notice.domain.repository.NoticeRepository;
import com.ojosama.report.application.dto.command.CreateReportCommand;
import com.ojosama.report.application.dto.command.UpdateReportCommand;
import com.ojosama.report.application.dto.query.ListReportQuery;
import com.ojosama.report.application.dto.result.ReportInfoResult;
import com.ojosama.report.application.dto.result.ReportResult;
import com.ojosama.report.domain.model.entity.Report;
import com.ojosama.report.domain.model.enums.ReporterType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    // 공지사항 생성
    @Transactional
    public NoticeResult createNotice(CreateNoticeCommand command) {
        Notice notice = command.toEntity();
        Notice savedNotice = noticeRepository.save(notice);

        return NoticeResult.from(savedNotice);
    }

    // 공지사항 목록 조회
    @Transactional(readOnly = true)
    public Page<NoticeResult> getNoticeList(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findAllByDeletedAtIsNull(pageable);

        return notices.map(NoticeResult::from);
    }

    // 공지사항 상세 조회
    @Transactional(readOnly = true)
    public NoticeResult getNotice(UUID noticeId) {
        Notice notice = findNoticeById(noticeId);

        return NoticeResult.from(notice);
    }

    // 공지사항 수정
    @Transactional
    public NoticeResult updateNotice(UUID noticeId, UpdateNoticeCommand command) {
        Notice notice = findNoticeById(noticeId);
        notice.update(command.title(), command.content());

        return NoticeResult.from(notice);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(UUID noticeId) {
        Notice notice = findNoticeById(noticeId);
        notice.deleted();
    }

    private Notice findNoticeById(UUID noticeId) {
        return noticeRepository.findByIdAndDeletedAtIsNull(noticeId)
                .orElseThrow(() -> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }
}
