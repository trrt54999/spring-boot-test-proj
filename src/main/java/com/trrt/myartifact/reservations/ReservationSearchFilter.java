package com.trrt.myartifact.reservations;

public record ReservationSearchFilter(Long roomId,
                                      Long userId,
                                      Integer pageSize,
                                      Integer pageNumber) {
}
