package com.faleknatalia.cinemaBookingSystem.util;

import com.faleknatalia.cinemaBookingSystem.model.ScheduledMovie;
import com.faleknatalia.cinemaBookingSystem.model.Seat;
import com.faleknatalia.cinemaBookingSystem.model.SeatReservationByScheduledMovie;
import com.faleknatalia.cinemaBookingSystem.repository.CinemaHallRepository;
import com.faleknatalia.cinemaBookingSystem.repository.ScheduledMovieRepository;
import com.faleknatalia.cinemaBookingSystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SeatReservationByScheduledMovieGenerator {

    @Autowired
    ScheduledMovieRepository scheduledMovieRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    CinemaHallRepository cinemaHallRepository;

    public List<SeatReservationByScheduledMovie> generateSeatsReservationByScheduledMovies() {
        List<ScheduledMovie> scheduledMovies = scheduledMovieRepository.findAll();
        List<SeatReservationByScheduledMovie> seatReservationByScheduledMovies = new ArrayList<>();
        scheduledMovies.forEach(sm ->
                {
                    List<Seat> seats = cinemaHallRepository.findOne(sm.getCinemaHallId()).getSeats();
                    for (Seat seat : seats) {
                        seatReservationByScheduledMovies.add(new SeatReservationByScheduledMovie(sm.getScheduledMovieId(), sm.getCinemaHallId(), true, 1, seat));
                    }

                }
        );

        return seatReservationByScheduledMovies;
    }

}