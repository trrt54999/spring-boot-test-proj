package com.trrt.myartifact.reservations.availability;

import com.trrt.myartifact.reservations.ReservationEntity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {
    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationAvailabilityController(
            ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @Valid CheckAvailabilityRequest request
    ) {
        boolean isAvailable = reservationAvailabilityService.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate());

        var message = isAvailable
                ? "Room available to reservation"
                : "Room is not available to reservation";
        var status = isAvailable
                ? AvailabilityStatus.AVAILABLE
                : AvailabilityStatus.RESERVED;

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CheckAvailabilityResponse(message, status));
    }
}
