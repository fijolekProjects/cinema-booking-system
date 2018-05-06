package com.faleknatalia.cinemaBookingSystem.util;

import com.faleknatalia.cinemaBookingSystem.model.*;
import com.faleknatalia.cinemaBookingSystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TicketDataService {

    //TODO te wszystkie beany powinny byc private - wszedzie, wszedzie :)
    //Robimy publiczne beany dla wygody ale ogolnie to powinno byc private - to sie nazywa field injection natomiast na rozmowie kwalifikacyjnej raczej trzeba powiedziec ze uzywasz constructor injection :)
    //spring constructor injection vs field injection - google it
    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ScheduledMovieRepository scheduledMovieRepository;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    SeatReservationByScheduledMovieRepository seatReservationByScheduledMovieRepository;

    @Autowired
    TicketPriceRepository ticketPriceRepository;

    //TODO te formattery powinny byc private
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter formatterHour = DateTimeFormatter.ofPattern("HH:mm");

    //TODO usunac duplikacje poprzez uzycie w tej metodzie tej metody ponizej
    public TicketData findMovie(long reservationId) {
        Reservation reservation = reservationRepository.findOne(reservationId);
        long chosenMovie = reservation.getChosenMovieId();
        ScheduledMovie movie = scheduledMovieRepository.findOne(chosenMovie);
        LocalDateTime movieProjection = movie.getDateOfProjection();
        String projectionDate = movieProjection.format(formatter);
        String projectionHour = movieProjection.format(formatterHour);

        String movieTitle = movieRepository.findOne(movie.getMovieId()).getTitle();
        long cinemaHall = movie.getCinemaHallId();
        List<SeatAndPriceDetails> seatAndPriceDetails = new ArrayList<>();
        List<ChosenSeatAndPrice> chosenSeatsAndPrices = reservation.getChosenSeatsAndPrices();
        //TODO przy streamach uzywajac map nigdy nie mutujesz (a tutaj mutujesz uzywajac add)
        //mutujesz przy forEach - map zwraca nowa kolekcje, a forEach zwraca void
        // to mozna przepisac na - i tak bedzie lepiej:
//        List<SeatAndPriceDetails> seatAndPriceDetails1 = chosenSeatsAndPrices.stream().map(chosenSeatAndPrice ->
//                new SeatAndPriceDetails(
//                        seatRepository.findOne(chosenSeatAndPrice.getSeatId()),
//                        ticketPriceRepository.findOne(chosenSeatAndPrice.getTicketPriceId()))
//        ).collect(Collectors.toList());

        chosenSeatsAndPrices.stream().map(chosenSeatAndPrice ->
                seatAndPriceDetails.add(new SeatAndPriceDetails(
                        seatRepository.findOne(chosenSeatAndPrice.getSeatId()),
                        ticketPriceRepository.findOne(chosenSeatAndPrice.getTicketPriceId())))
        ).collect(Collectors.toList());
        return new TicketData(movieTitle, projectionDate, projectionHour, cinemaHall, seatAndPriceDetails);
    }

    public TicketData findMovie(long chosenMovie, List<ChosenSeatAndPrice> chosenSeatsAndPrices) {
        ScheduledMovie movie = scheduledMovieRepository.findOne(chosenMovie);
        LocalDateTime movieProjection = movie.getDateOfProjection();
        String projectionDate = movieProjection.format(formatter);
        String projectionHour = movieProjection.format(formatterHour);

        String movieTitle = movieRepository.findOne(movie.getMovieId()).getTitle();
        long cinemaHall = movie.getCinemaHallId();
        List<SeatAndPriceDetails> seatAndPriceDetails = new ArrayList<>();
        //TODO tutaj podobnie jak wyzej, nie uzywaj map i ArrayList.add
        chosenSeatsAndPrices.stream().map(chosenSeatAndPrice ->
                seatAndPriceDetails.add(
                        new SeatAndPriceDetails(seatRepository.findOne(chosenSeatAndPrice.getSeatId()),
                                ticketPriceRepository.findOne(chosenSeatAndPrice.getTicketPriceId())))
        ).collect(Collectors.toList());
        return new TicketData(movieTitle, projectionDate, projectionHour, cinemaHall, seatAndPriceDetails);
    }

}
