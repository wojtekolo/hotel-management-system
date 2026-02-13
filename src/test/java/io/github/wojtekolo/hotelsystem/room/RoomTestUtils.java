package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomDetails;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomStatusDto;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomTypeDto;

import java.math.BigDecimal;

public class RoomTestUtils {
    public static Room.RoomBuilder aValidRoom(RoomType type, RoomStatus status) {
        return Room.builder()
                .name("0")
                .floor(0)
                .description("Room description")
                .type(type)
                .status(status);
    }
    public static RoomType.RoomTypeBuilder aValidType(){
        return RoomType.builder()
                .name("0")
                .pricePerNight(BigDecimal.valueOf(0))
                .description("Type description")
                .capacity(0);
    }
    public static RoomDetails aValidRoomDetails(){
        RoomTypeDto roomTypeDto = new RoomTypeDto(
                1L,
                "type name",
                BigDecimal.valueOf(100),
                "description",
                2
        );

        RoomStatusDto roomStatusDto = new RoomStatusDto(
                1L,
                "status name"
        );

        return new RoomDetails(
                10L,
                "room name",
                2,
                "description",
                roomTypeDto,
                roomStatusDto
        );
    }
}
