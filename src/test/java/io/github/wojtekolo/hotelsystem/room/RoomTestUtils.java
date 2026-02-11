package io.github.wojtekolo.hotelsystem.room;

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
}
