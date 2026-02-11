package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void should_return_room_list_item_when_room_exists(){
//        given
        RoomType deluxeType = RoomTestUtils.aValidType().name("Test type").pricePerNight(BigDecimal.valueOf(500)).capacity(5).build();
        entityManager.persist(deluxeType);
        RoomStatus availableStatus = RoomStatus.builder().name("Available").build();
        entityManager.persist(availableStatus);
        Room room = RoomTestUtils.aValidRoom(deluxeType, availableStatus).name("101").floor(5).build();
        entityManager.persist(room);

//        when
        Slice<RoomListItem> result = roomRepository.findAllRooms(PageRequest.of(0, 10));

//        then
        assertThat(result).hasSize(1);

        RoomListItem dto = result.getContent().getFirst();
        assertThat(dto.name()).isEqualTo("101");
        assertThat(dto.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(dto.floor()).isEqualTo(5);
        assertThat(dto.status()).isEqualTo("Available");
        assertThat(dto.type()).isEqualTo("Test type");
        assertThat(dto.capacity()).isEqualTo(5);
    }

    @Test
    void should_return_empty_slice_when_no_rooms_found(){
//        given
//        when
        Slice<RoomListItem> result = roomRepository.findAllRooms(PageRequest.of(0, 10));

//        then
        assertThat(result.getContent()).isEmpty();
    }
}
