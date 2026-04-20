package io.github.wojtekolo.hotelsystem.booking.model.entity;

import java.util.EnumSet;
import java.util.Set;

public enum RoomStayStatus {
    ACTIVE, PLANNED, CANCELLED, COMPLETED, NOSHOW;

    public static final Set<RoomStayStatus> COLLIDING_STATUSES =
            EnumSet.of(ACTIVE, PLANNED, COMPLETED);
}
