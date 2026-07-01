package com.trrt.myartifact.reservations.availability;

import com.trrt.myartifact.reservations.ReservationRepository;
import com.trrt.myartifact.reservations.ReservationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {
    private ReservationRepository reservationRepository;

    public ReservationAvailabilityService(
            ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public boolean isReservationAvailable(Long roomId, LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Start date cannot be more then end date and must be earlier 1 day!");
        }
        
        List<Long> conflictingIds = reservationRepository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED);
        if (conflictingIds.isEmpty()) {
            return false;
        }
        return true;
    }
}
