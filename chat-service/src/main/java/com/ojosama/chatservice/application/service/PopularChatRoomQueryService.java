package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.application.dto.result.PopularChatRoomResult;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import com.ojosama.common.exception.CommonErrorCode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularChatRoomQueryService {

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

        // ChatRoomId 로 ChatRoom 맵으로 변환 <채팅방id,채팅방정보> 형태
        Map<UUID, ChatRoom> chatRoomsById = chatRoomRepository.findAllByIds(viewerCounts.keySet()).stream()
                .collect(Collectors.toMap(ChatRoom::getId, chatRoom -> chatRoom));

        return viewerCounts.entrySet().stream()
                .map(entry -> buildPopularRoom(chatRoomsById.get(entry.getKey()), entry.getValue()))
                .filter(result -> result != null && result.currentViewerCount() > 0)
                .limit(resolvedLimit)
                .toList();
    }

    private PopularChatRoomResult buildPopularRoom(ChatRoom chatRoom, Integer viewerCount) {
        if (chatRoom == null || !chatRoom.isOpen() || viewerCount == null || viewerCount <= 0) {
            return null;
        }
        return PopularChatRoomResult.from(chatRoom, viewerCount);
    }

    private int resolveLimit(Integer limit) {
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedLimit <= 0 || resolvedLimit > MAX_LIMIT) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return resolvedLimit;
    }
}
