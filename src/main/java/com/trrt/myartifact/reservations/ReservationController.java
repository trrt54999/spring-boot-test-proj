package com.trrt.myartifact.reservations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id) {
        log.info("Method called, id: {}", id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservationsByPagination(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        log.info("Get all reservations");

        var filter = new ReservationSearchFilter(roomId, userId, pageSize, pageNumber);
        return ResponseEntity.ok(reservationService.findAllByPagination(filter));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid Reservation reservationToCreate
    ) {
        log.info("Create reservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("test-header", "123")
                .body(reservationService.createReservation(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid Reservation reservationToUpdate
    ) {
        log.info("Update reservation: {} with id: {}", reservationToUpdate, id);
        var updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.ok(updated);
    }

    @Transactional
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id
    ) {
        log.info("Delete reservation with id: {}", id);

        reservationService.cancelReservation(id);
        return ResponseEntity.ok()
                .build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable Long id
    ) {
        log.info("Approve reservation with id: {}", id);
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}
