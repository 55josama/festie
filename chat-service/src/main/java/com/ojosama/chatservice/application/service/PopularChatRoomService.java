package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.application.dto.result.PopularChatRoomResult;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import com.ojosama.common.exception.CommonErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularChatRoomService {

    private static final int DEFAULT_LIMIT = 3;
    private static final int MAX_LIMIT = 10;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomPopularityTracker popularityTracker;

    public List<PopularChatRoomResult> getPopularChatRooms(Integer limit) {
        int resolvedLimit = resolveLimit(limit);
        Map<UUID, Integer> viewerCounts = popularityTracker.snapshotViewerCounts();
        if (viewerCounts.isEmpty()) {
            return List.of();
        }

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByIds(viewerCounts.keySet());

        return chatRooms.stream()
                .filter(ChatRoom::isOpen) // 열린 채팅방만
                .map(chatRoom -> PopularChatRoomResult.from(
                        chatRoom,
                        viewerCounts.getOrDefault(chatRoom.getId(), 0)
                ))
                .filter(result -> result.currentViewerCount() > 0)
                .sorted(Comparator
                        .comparingInt(PopularChatRoomResult::currentViewerCount).reversed()
                        .thenComparing(result -> result.chatRoom().openedAt(),
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(result -> result.chatRoom().chatRoomId()))
                .limit(resolvedLimit)
                .toList();
    }

    private int resolveLimit(Integer limit) {
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedLimit <= 0 || resolvedLimit > MAX_LIMIT) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return resolvedLimit;
    }
}
