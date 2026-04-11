package io.github.wojtekolo.hotelsystem.booking.service.validation;

import io.github.wojtekolo.hotelsystem.booking.exception.details.ConflictingStay;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayExternalConflict;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.booking.service.BookingMapper;
import io.github.wojtekolo.hotelsystem.booking.service.BookingMapperImpl;
import io.github.wojtekolo.hotelsystem.customer.service.CustomerMapper;
import io.github.wojtekolo.hotelsystem.employee.service.EmployeeMapper;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;
import static io.github.wojtekolo.hotelsystem.room.RoomTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingExternalValidatorTest {
    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private EmployeeMapper employeeMapper;
    @Mock
    private RoomStayRepository roomStayRepository;

    private BookingExternalValidator externalValidator;


    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        BookingMapper bookingMapper = new BookingMapperImpl(customerMapper, employeeMapper);
        externalValidator = new BookingExternalValidator(
                roomStayRepository,
                bookingMapper
        );
    }

    @Test
    public void should_return_conflict_when_new_stay_collides_with_existing() {
//        given
        Room room = aValidRoom().id(15L).name("roomname").build();

        when(roomStayRepository.getConflicts(15L,
                List.of(RoomStayStatus.ACTIVE, RoomStayStatus.PLANNED),
                today.plusDays(7), today.plusDays(12), null))
                .thenReturn(List.of(
                        buildRoomStay(1L, room, today.plusDays(7), today.plusDays(12))
                ));

//        when
        List<RoomStayExternalConflict> result = externalValidator.validate(List.of(
                buildRoomStay(null, room, today.plusDays(7), today.plusDays(12))
        ), null);

//        then
        assertThat(result)
                .extracting(RoomStayExternalConflict::roomId, RoomStayExternalConflict::roomName)
                .containsExactly(tuple(15L, "roomname"));

        assertThat(result.getFirst().conflictingStays())
                .extracting(ConflictingStay::from, ConflictingStay::to)
                .containsExactly(tuple(today.plusDays(7), today.plusDays(12)));
    }

    @Test
    public void booking_conflict_response_should_contain_only_rooms_unavailable_in_chosen_period() {
//        given
        Room room1 = aValidRoom().id(1L).name("roomname1").build();
        Room room2 = aValidRoom().id(2L).name("roomname2").build();
        Room room3 = aValidRoom().id(3L).name("roomname3").build();

        List<RoomStay> newStays = List.of(
                buildRoomStay(null, room1, today.plusDays(5), today.plusDays(10)),
                buildRoomStay(null, room2, today.plusDays(5), today.plusDays(10)),
                buildRoomStay(null, room3, today.plusDays(5), today.plusDays(10))
        );

        when(roomStayRepository.getConflicts(eq(1L), any(), any(), any(), any()))
                .thenReturn(List.of(
                        buildRoomStay(10L, room1, today.plusDays(5), today.plusDays(10))
                ));

        when(roomStayRepository.getConflicts(eq(2L), any(), any(), any(), any()))
                .thenReturn(List.of(
                        buildRoomStay(11L, room2, today.plusDays(4), today.plusDays(7)),
                        buildRoomStay(12L, room2, today.plusDays(8), today.plusDays(12))
                ));

        when(roomStayRepository.getConflicts(eq(3L), any(), any(), any(), any()))
                .thenReturn(List.of(

                ));

//        when
        List<RoomStayExternalConflict> result = externalValidator.validate(newStays, null);

//        then
        assertThat(result)
                .extracting(RoomStayExternalConflict::roomId, RoomStayExternalConflict::roomName)
                .containsExactly(tuple(1L, "roomname1"), tuple(2L, "roomname2"));

        assertThat(result)
                .flatExtracting(RoomStayExternalConflict::conflictingStays)
                .extracting(ConflictingStay::roomStayId)
                .containsExactlyInAnyOrder(10L, 11L, 12L);
    }
}