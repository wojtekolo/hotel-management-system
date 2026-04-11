package io.github.wojtekolo.hotelsystem.booking.service.processing;

import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayCreateCommand;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayUpdateCommand;
import org.springframework.stereotype.Component;

@Component
public class BookingCommandMaker {

    public RoomStayCreateCommand prepareCreateCommand(RoomStayUpdateRequest req) {
        return new RoomStayCreateCommand(req.roomId(), req.from(), req.to(), req.customPricePerNight());
    }

    public RoomStayCreateCommand fromCreateRequest(RoomStayCreateRequest req) {
        return new RoomStayCreateCommand(req.roomId(), req.from(), req.to(), req.customPricePerNight());
    }

    public RoomStayUpdateCommand prepareUpdateCommand(RoomStayUpdateRequest req) {
        return new RoomStayUpdateCommand(req.id(), req.roomId(), req.from(), req.to(), req.customPricePerNight());
    }
}
