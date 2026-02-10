package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;


    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/roomlist")
    public ResponseEntity<Slice<RoomListItem>> getRooms(Pageable pageable) {
        Slice<RoomListItem> roomListItems = roomService.getRooms(pageable);
        return ResponseEntity.ok(roomListItems);
    }
}
