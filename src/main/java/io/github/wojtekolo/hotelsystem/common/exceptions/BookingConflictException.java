package io.github.wojtekolo.hotelsystem.common.exceptions;

import io.github.wojtekolo.hotelsystem.booking.RoomConflict;
import io.github.wojtekolo.hotelsystem.booking.RoomStay;

import java.util.List;

public class BookingConflictException extends RuntimeException{
    public BookingConflictException(List<RoomConflict> conflicts) {
        super(generateMessage(conflicts));
    }

    private static String generateMessage(List<RoomConflict> conflicts){
        StringBuilder builder = new StringBuilder();
        for (RoomConflict conflict: conflicts){
            builder.append("\nRoom ")
                    .append(conflict.room().getName())
                    .append(" is booked for selected period. Conflicting bookings: ")
                    .append(generateRoomMessage(conflict.conflicts()));
        }
        return builder.toString();
    }
    private static String generateRoomMessage(List<RoomStay> conflicts){
        StringBuilder builder = new StringBuilder();
        conflicts.forEach(conflict ->
                builder.append("\nID: ").append(conflict.getId())
                        .append(", from: ")
                        .append(conflict.getActiveFrom())
                        .append(", to: ")
                        .append(conflict.getActiveTo())
                        .append(", "));
        return builder.toString();
    }
}
