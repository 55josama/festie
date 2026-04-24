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
        int openedCount = chatRoomService.openScheduledChatRooms(now);
        int closedCount = chatRoomService.closeScheduledChatRooms(now);

        log.info("chat room scheduler executed. openedCount={}, closedCount={}, now={}",
                openedCount, closedCount, now);
    }
}
