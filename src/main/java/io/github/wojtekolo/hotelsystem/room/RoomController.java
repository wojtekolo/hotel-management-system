package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomCreateRequest;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomDetails;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;


    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/list")
    public ResponseEntity<Slice<RoomListItem>> getRooms(@PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable) {
        Slice<RoomListItem> roomListItems = roomService.getRooms(pageable);
        return ResponseEntity.ok(roomListItems);
    }
    @PostMapping("/add")
    public ResponseEntity<RoomDetails> addRoom(@Valid @RequestBody RoomCreateRequest createRequest) {
        RoomDetails createdRoom = roomService.addRoom(createRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRoom.id())
                .toUri();

        return ResponseEntity.created(location).body(createdRoom);
    }
}
