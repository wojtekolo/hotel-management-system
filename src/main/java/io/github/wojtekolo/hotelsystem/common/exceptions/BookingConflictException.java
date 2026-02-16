package io.github.wojtekolo.hotelsystem.common.exceptions;

import io.github.wojtekolo.hotelsystem.booking.RoomStay;

import java.util.List;

public class BookingConflictException extends RuntimeException{
    public BookingConflictException(List<RoomStay> conflicts) {
        super("Room "+conflicts.getFirst().getRoom().getName()+" is booked for selected period. Conflicting bookings: "+ generateMessage(conflicts));
    }

    private static String generateMessage(List<RoomStay> conflicts){
        StringBuilder builder = new StringBuilder();
        conflicts.forEach(conflict ->
                builder.append("ID: "+conflict.getId()+", from: "+conflict.getActiveFrom()+", to: "+conflict.getActiveTo()+". "));
        return builder.toString();
    }
}
