//package io.github.wojtekolo.hotelsystem.booking;
//
//import io.github.wojtekolo.hotelsystem.booking.api.validation.ExternalRoomStayConflict;
//import io.github.wojtekolo.hotelsystem.booking.api.validation.InternalRoomStayConflict;
//import io.github.wojtekolo.hotelsystem.booking.api.validation.RoomStayConflictDetails;
//import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
//import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
//import io.github.wojtekolo.hotelsystem.booking.service.validation.BookingExternalValidator;
//import io.github.wojtekolo.hotelsystem.booking.service.validation.BookingInternalValidator;
//import io.github.wojtekolo.hotelsystem.booking.service.validation.BookingValidator;
//import io.github.wojtekolo.hotelsystem.customer.service.CustomerMapper;
//import io.github.wojtekolo.hotelsystem.employee.service.EmployeeMapper;
//import io.github.wojtekolo.hotelsystem.room.model.Room;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;
//import static io.github.wojtekolo.hotelsystem.room.RoomTestUtils.*;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.tuple;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class BookingValidatorTest {
//
//    @Mock
//    private CustomerMapper customerMapper;
//
//    @Mock
//    private EmployeeMapper employeeMapper;
//
//    @Mock
//    private BookingInternalValidator internalValidator;
//
//    @Mock
//    private BookingExternalValidator externalValidator;
//
//    private final LocalDate today = LocalDate.now();
//
//    private BookingValidator bookingValidator;
//
//    @BeforeEach
//    void setUp() {
//        bookingValidator = new BookingValidator(
//                internalValidator,
//                externalValidator
//        );
//    }
//
//    @Test
//    public void should_return_conflict_when_new_stay_collides_with_existing() {
////        given
//        Room room = aValidRoom().id(15L).name("roomname").build();
//
//        when(roomStayRepository.getConflicts(15L,
//                List.of(RoomStayStatus.ACTIVE, RoomStayStatus.PLANNED),
//                today.plusDays(7), today.plusDays(12), null))
//                .thenReturn(List.of(
//                        buildRoomStay(1L, room, today.plusDays(7), today.plusDays(12))
//                ));
//
////        when
//        List<ExternalRoomStayConflict> result = bookingValidator.validateExternalConflicts(List.of(
//                buildRoomStay(null, room, today.plusDays(7), today.plusDays(12))
//        ));
//
////        then
//        assertThat(result)
//                .extracting(ExternalRoomStayConflict::roomId, ExternalRoomStayConflict::roomName)
//                .containsExactly(tuple(15L, "roomname"));
//
//        assertThat(result.getFirst().roomConflictsDetails())
//                .extracting(RoomStayConflictDetails::from, RoomStayConflictDetails::to)
//                .containsExactly(tuple(today.plusDays(7), today.plusDays(12)));
//    }
//
//    @Test
//    public void booking_conflict_response_should_contain_only_rooms_unavailable_in_chosen_period() {
////        given
//        Room room1 = aValidRoom().id(1L).name("roomname1").build();
//        Room room2 = aValidRoom().id(2L).name("roomname2").build();
//        Room room3 = aValidRoom().id(3L).name("roomname3").build();
//
//        List<RoomStay> newStays = List.of(
//                buildRoomStay(null, room1, today.plusDays(5), today.plusDays(10)),
//                buildRoomStay(null, room2, today.plusDays(5), today.plusDays(10)),
//                buildRoomStay(null, room3, today.plusDays(5), today.plusDays(10))
//        );
//
//        when(roomStayRepository.getConflicts(eq(1L), any(), any(), any(), any()))
//                .thenReturn(List.of(
//                        buildRoomStay(10L, room1, today.plusDays(5), today.plusDays(10))
//                ));
//
//        when(roomStayRepository.getConflicts(eq(2L), any(), any(), any(), any()))
//                .thenReturn(List.of(
//                        buildRoomStay(11L, room2, today.plusDays(4), today.plusDays(7)),
//                        buildRoomStay(12L, room2, today.plusDays(8), today.plusDays(12))
//                ));
//
//        when(roomStayRepository.getConflicts(eq(3L), any(), any(), any(), any()))
//                .thenReturn(List.of(
//
//                ));
//
////        when
//        List<ExternalRoomStayConflict> result = bookingValidator.validateExternalConflicts(newStays);
//
////        then
//        assertThat(result)
//                .extracting(ExternalRoomStayConflict::roomId, ExternalRoomStayConflict::roomName)
//                .containsExactly(tuple(1L, "roomname1"), tuple(2L, "roomname2"));
//
//        assertThat(result)
//                .flatExtracting(ExternalRoomStayConflict::roomConflictsDetails)
//                .extracting(RoomStayConflictDetails::roomStayId)
//                .containsExactlyInAnyOrder(10L, 11L, 12L);
//    }
//
//    @Test
//    public void should_return_conflicts_when_there_is_overlap_between_sequential_stays_inside_request() {
////        given
//        var room = aValidRoom().id(1L).name("roomname1").build();
////        when
//        List<InternalRoomStayConflict> result = bookingValidator.validateInternalConflicts(
//                List.of(
//                        buildRoomStay(1L, room, today.plusDays(10), today.plusDays(15)),
//                        buildRoomStay(2L, room, today.plusDays(13), today.plusDays(18)),
//                        buildRoomStay(3L, room, today.plusDays(17), today.plusDays(20))
//                )
//        );
//
////        then
//        assertThat(result)
//                .extracting(
//                        InternalRoomStayConflict::id1, InternalRoomStayConflict::from1, InternalRoomStayConflict::to1,
//                        InternalRoomStayConflict::id2, InternalRoomStayConflict::from2, InternalRoomStayConflict::to2
//                )
//                .containsExactlyInAnyOrder(
//                        tuple(
//                                1L, today.plusDays(10), today.plusDays(15),
//                                2L, today.plusDays(13), today.plusDays(18)
//                        ),
//                        tuple(
//                                2L, today.plusDays(13), today.plusDays(18),
//                                3L, today.plusDays(17), today.plusDays(20)
//                        )
//
//                );
//    }
//
//    @Test
//    public void should_return_conflicts_when_one_stay_overlaps_multiple_others() {
////        given
//        var room = aValidRoom().id(1L).name("roomname1").build();
//
////        when
//        List<InternalRoomStayConflict> result = bookingValidator.validateInternalConflicts(
//                List.of(
//                        buildRoomStay(1L, room, today.plusDays(10), today.plusDays(30)),
//                        buildRoomStay(2L, room, today.plusDays(13), today.plusDays(15)),
//                        buildRoomStay(3L, room, today.plusDays(16), today.plusDays(20)),
//                        buildRoomStay(4L, room, today.plusDays(22), today.plusDays(25))
//                )
//        );
//
////        then
//        assertThat(result)
//                .extracting(
//                        InternalRoomStayConflict::id1, InternalRoomStayConflict::from1, InternalRoomStayConflict::to1,
//                        InternalRoomStayConflict::id2, InternalRoomStayConflict::from2, InternalRoomStayConflict::to2
//                )
//                .containsExactlyInAnyOrder(
//                        tuple(
//                                1L, today.plusDays(10), today.plusDays(30),
//                                2L, today.plusDays(13), today.plusDays(15)
//                        ),
//                        tuple(
//                                1L, today.plusDays(10), today.plusDays(30),
//                                3L, today.plusDays(16), today.plusDays(20)
//                        ),
//                        tuple(
//                                1L, today.plusDays(10), today.plusDays(30),
//                                4L, today.plusDays(22), today.plusDays(25)
//                        )
//
//                );
//    }
//
//    @Test
//    public void should_return_empty_list_when_no_gap_between_sequential_stays_inside_request() {
////        given
//        var room = aValidRoom().id(1L).name("roomname1").build();
//
////        when
//        List<InternalRoomStayConflict> result = bookingValidator.validateInternalConflicts(
//                List.of(
//                        buildRoomStay(1L, room, today.plusDays(10), today.plusDays(15)),
//                        buildRoomStay(2L, room, today.plusDays(15), today.plusDays(20)),
//                        buildRoomStay(3L, room, today.plusDays(20), today.plusDays(25))
//                )
//        );
//
////        then
//        assertThat(result).hasSize(0);
//    }
//
//    @Test
//    public void should_return_empty_list_when_different_rooms_and_same_period() {
////        given
//        var room1 = aValidRoom().id(1L).build();
//        var room2 = aValidRoom().id(2L).build();
//
////        when
//        List<InternalRoomStayConflict> result = bookingValidator.validateInternalConflicts(
//                List.of(
//                        buildRoomStay(1L, room1, today.plusDays(10), today.plusDays(15)),
//                        buildRoomStay(2L, room2, today.plusDays(10), today.plusDays(15))
//                )
//        );
//
////        then
//        assertThat(result).hasSize(0);
//    }
//}