package io.github.wojtekolo.hotelsystem.common.exceptions;

import io.github.wojtekolo.hotelsystem.booking.RoomStayConflict;
import io.github.wojtekolo.hotelsystem.booking.RoomStayConflictDetails;
import lombok.Getter;

import java.util.List;

@Getter
public class BookingConflictException extends RuntimeException{
    List<RoomStayConflict> conflicts;

    public BookingConflictException(List<RoomStayConflict> conflicts) {
        super(generateMessage(conflicts));
        this.conflicts = conflicts;
    }

    private static String generateMessage(List<RoomStayConflict> conflicts){
        StringBuilder builder = new StringBuilder();
        for (RoomStayConflict conflict: conflicts){
            builder.append("\nRoom ")
                    .append(conflict.roomName())
                    .append(" is booked for selected period. Conflicting bookings: ")
                    .append(generateRoomMessage(conflict.conflicts()));
        }
        return builder.toString();
    }
    private static String generateRoomMessage(List<RoomStayConflictDetails> conflicts){
        StringBuilder builder = new StringBuilder();
        conflicts.forEach(conflict ->
                builder.append("\nID: ").append(conflict.roomStayId())
                        .append(", from: ")
                        .append(conflict.from())
                        .append(", to: ")
                        .append(conflict.to())
                        .append(", "));
        return builder.toString();
    }
}
