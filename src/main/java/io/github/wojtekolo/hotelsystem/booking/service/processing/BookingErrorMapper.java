package io.github.wojtekolo.hotelsystem.booking.service.processing;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationCode;
import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolation;
import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolationReason;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingErrorMapper {

    public List<RoomStayViolationDetails> mapToErrorCodes(List<RoomStayViolation> violations) {
        return violations.stream().map(this::handleViolation).toList();
    }

    public RoomStayViolationDetails handleViolation(RoomStayViolation violation) {
        return new RoomStayViolationDetails(violation.stayId(), violation.currentStatus(), translate(violation.reason()), violation.context());
    }

    public RoomStayViolationCode translate(RoomStayViolationReason reason) {
        return switch (reason) {
            case RoomStayViolationReason.CANCEL_INVALID_STATUS -> RoomStayViolationCode.CANCEL_INVALID_STATUS;
            case RoomStayViolationReason.START_DATE_EDIT_INVALID_STATUS ->
                    RoomStayViolationCode.START_DATE_EDIT_INVALID_STATUS;
            case RoomStayViolationReason.END_DATE_EDIT_INVALID_STATUS ->
                    RoomStayViolationCode.END_DATE_EDIT_INVALID_STATUS;
            case RoomStayViolationReason.ROOM_EDIT_INVALID_STATUS -> RoomStayViolationCode.ROOM_EDIT_INVALID_STATUS;
            case RoomStayViolationReason.PRICE_EDIT_INVALID_STATUS ->
                    RoomStayViolationCode.PRICE_EDIT_INVALID_STATUS;
            case RoomStayViolationReason.END_DATE_IN_THE_PAST -> RoomStayViolationCode.END_DATE_IN_THE_PAST;
            case RoomStayViolationReason.START_DATE_IN_THE_PAST -> RoomStayViolationCode.START_DATE_IN_THE_PAST;
            case RoomStayViolationReason.END_DATE_NOT_AFTER_START_DATE ->
                    RoomStayViolationCode.END_DATE_NOT_AFTER_START_DATE;
            case RoomStayViolationReason.PRICE_NEGATIVE -> RoomStayViolationCode.PRICE_NEGATIVE;
        };
    }
}
