package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.AbstractIntegrationTest;
import io.github.wojtekolo.hotelsystem.common.TestDataFactory;
import io.github.wojtekolo.hotelsystem.room.api.RoomListItem;
import io.github.wojtekolo.hotelsystem.room.model.LifecycleStatus;
import io.github.wojtekolo.hotelsystem.room.model.OperationalStatus;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.model.RoomType;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestDataFactory.class)
@Transactional
class RoomRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TestDataFactory data;

    @Test
    void should_return_room_list_item_when_room_exists(){
//        given
        RoomType deluxeType = RoomTestUtils.aValidType().name("Test type").pricePerNight(BigDecimal.valueOf(500)).capacity(5).build();
        entityManager.persist(deluxeType);

        Room room = RoomTestUtils.aValidRoom(deluxeType).operationalStatus(OperationalStatus.CLEAN).lifecycleStatus(LifecycleStatus.ACTIVE).name("101").floor(5).build();
        entityManager.persist(room);

//        when
        Slice<RoomListItem> result = roomRepository.findAllRooms(PageRequest.of(0, 10));

//        then
        assertThat(result).hasSize(1);

        RoomListItem dto = result.getContent().getFirst();
        assertThat(dto.name()).isEqualTo("101");
        assertThat(dto.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(dto.floor()).isEqualTo(5);
        assertThat(dto.operationalStatus()).isEqualTo(OperationalStatus.CLEAN);
        assertThat(dto.lifecycleStatus()).isEqualTo(LifecycleStatus.ACTIVE);
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

    @Test
    void should_return_list_of_chosen_and_existing_rooms(){
//        give
        Room requested1 = data.prepareRoom();
        Room requested2 = data.prepareRoom();
        Room ignored = data.prepareRoom();
        Long nonExistentId = 1234L;

//        when
        List<Room> rooms = roomRepository.findAllByIdWithLock(List.of(requested1.getId(), requested2.getId(), nonExistentId));

//        then
        assertThat(rooms).extracting(Room::getId).containsExactlyInAnyOrder(requested1.getId(), requested2.getId());
    }

    @Test
    void should_return_empty_list_when_no_ids_used(){
//        given
        Room ignored = data.prepareRoom();

//        when
        List<Room> rooms = roomRepository.findAllByIdWithLock(List.of());

//        then
        assertThat(rooms).isNotNull();
        assertThat(rooms).isEmpty();
    }
}
