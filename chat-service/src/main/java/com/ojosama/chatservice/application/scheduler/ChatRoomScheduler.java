package com.ojosama.chatservice.application.scheduler;

import com.ojosama.chatservice.application.service.ChatRoomService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomScheduler {

    private final ChatRoomService chatRoomService;

    @Scheduled(fixedDelay = 60000) // 1 min
    public void syncChatRoomStatus() {
        LocalDateTime now = LocalDateTime.now();
        int openedCount = 0;
        int closedCount = 0;

        try {
            openedCount = chatRoomService.openScheduledChatRooms(now);
        } catch (Exception e) {
            log.error("스케쥴러가 채팅방 오픈에 실패했습니다. now={}", now, e);
        }

        try {
            closedCount = chatRoomService.closeScheduledChatRooms(now);
        } catch (Exception e) {
            log.error("스케쥴러가 채팅방 종료에 실패했습니다. now={}", now, e);
        }

        log.info("채팅방 스케쥴러가 실행되었습니다. openedCount={}, closedCount={}, now={}",
                openedCount, closedCount, now);
    }
}
