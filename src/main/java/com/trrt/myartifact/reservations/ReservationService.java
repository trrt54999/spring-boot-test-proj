package com.trrt.myartifact.reservations;

import com.trrt.myartifact.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationMapper mapper,
            ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationRepository = reservationRepository;
        this.mapper = mapper;
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservation = reservationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Not found reservation by id: " + id));

        return mapper.toDomain(reservation);
    }

    public List<Reservation> findAllByPagination(ReservationSearchFilter filter) {
        int basicPageSize = 10;
        int basicPage = 0;

        int pageSize = filter.pageSize() != null
                ? filter.pageSize() : basicPageSize;
        int pageNumber = filter.pageNumber() != null
                ? filter.pageNumber() : basicPage;

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> reservations = reservationRepository.findAllByPagination(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return reservations.stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date cannot be more then end date and must be earlier 1 day!");
        }

        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = reservationRepository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found by id"));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Can only update reservation who pending!");
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date cannot be more then end date and must be earlier 1 day!");
        }

        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setStatus(ReservationStatus.PENDING);
        reservationToSave.setId(reservationEntity.getId());

        var updatedReservation = reservationRepository.save(reservationToSave);

        return mapper.toDomain(updatedReservation);
    }

    public void cancelReservation(Long id) {
        var reservationToCancel = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found by id"));

        if (reservationToCancel.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation, contact with manager");
        }
        if (reservationToCancel.getStatus().equals(ReservationStatus.CANCELED)) {
            throw new IllegalStateException("Cannot cancel canceled reservation");
        }
        reservationRepository.setStatus(id, ReservationStatus.CANCELED);
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found by id"));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve not pending reservation");
        }

        var isAvailableToApprove = reservationAvailabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

        if (!isAvailableToApprove) {
            throw new IllegalArgumentException("Cannot approve reservation with conflict");
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }
}
