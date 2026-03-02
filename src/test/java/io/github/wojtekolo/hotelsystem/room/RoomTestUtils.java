package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomDetails;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomTypeDto;

import java.math.BigDecimal;

public class RoomTestUtils {
    private static int counter = 100;
    public static Room.RoomBuilder aValidRoom(RoomType type) {
        return Room.builder()
                .name(String.valueOf(counter++))
                .floor(0)
                .description("Room description")
                .type(type)
                .lifecycleStatus(LifecycleStatus.ACTIVE)
                .operationalStatus(OperationalStatus.CLEAN);
    }
    public static Room.RoomBuilder aValidRoom() {
        return aValidRoom(aValidType().build());
    }
    public static RoomType.RoomTypeBuilder aValidType(){
        return RoomType.builder()
                .name(String.valueOf(counter++))
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

        return new RoomDetails(
                10L,
                "room name",
                2,
                "description",
                roomTypeDto,
                LifecycleStatus.ACTIVE,
                OperationalStatus.CLEAN
        );
    }
}
