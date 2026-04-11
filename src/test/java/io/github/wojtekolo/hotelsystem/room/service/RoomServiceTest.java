package io.github.wojtekolo.hotelsystem.room.service;

import io.github.wojtekolo.hotelsystem.booking.service.loading.RoomLoadResult;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    RoomRepository roomRepository;

    @Mock
    RoomTypeRepository roomTypeRepository;

    @Mock
    RoomMapper roomMapper;

    private RoomService roomService;

    @BeforeEach
    void setUp(){
        roomService = new RoomService(roomRepository, roomTypeRepository, roomMapper);
    }

    @Test
    public void should_return_valid_result(){
//        given
        Set<Long> idsToGet = Set.of(1L, 2L, 3L, 4L);
        when(roomRepository.findAllByIdWithLock(List.of(1L, 2L, 3L, 4L))).thenReturn(
                List.of(
                        RoomTestUtils.aValidRoom().id(1L).build(),
                        RoomTestUtils.aValidRoom().id(3L).build()
                )
        );
//        when
        RoomLoadResult result = roomService.findAndLockRooms(idsToGet);

//        then
        assertThat(result.missingIds()).containsExactlyInAnyOrder(2L, 4L);
        assertThat(result.rooms())
                .hasSize(2)
                .containsOnlyKeys(1L, 3L);
    }
}