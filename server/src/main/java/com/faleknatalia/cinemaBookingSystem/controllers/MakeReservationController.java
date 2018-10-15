package com.faleknatalia.cinemaBookingSystem.controllers;

import com.faleknatalia.cinemaBookingSystem.dto.ChosenSeatAndPrice;
import com.faleknatalia.cinemaBookingSystem.dto.ReservationSummaryDto;
import com.faleknatalia.cinemaBookingSystem.model.PersonalData;
import com.faleknatalia.cinemaBookingSystem.model.Reservation;
import com.faleknatalia.cinemaBookingSystem.model.SeatReservationByScheduledMovie;
import com.faleknatalia.cinemaBookingSystem.repository.SeatReservationByScheduledMovieRepository;
import com.faleknatalia.cinemaBookingSystem.session.SessionService;
import com.faleknatalia.cinemaBookingSystem.ticket.TicketData;
import com.faleknatalia.cinemaBookingSystem.ticket.TicketDataService;
import com.faleknatalia.cinemaBookingSystem.validator.PersonalDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class MakeReservationController {

// uwagi ogolne
//- przy braku sesji na zakladce whatsOn leci http 500 - troche nieelegancko to wyglada. Moze lepiej tutaj explicite cos zwrocic, bez 500
//- brakuje przynajmniej jakiegos zdisablowania/wyszarzenia przycisku pay zaraz po kliknieciu
//- z ta walidacja formularza mozna sie klocic - no bo np jakis arab moze miec takie nazwisko ktorego nie obsluzy zaden regex itd - to jest troche sliska sprawa
//- po przejsciu do widoku siedzen nie wiadomo na ktory film kupujemy bilet
//- da sie wchodzic na urle dla starych filmow i da sie kupic dla nich bilet - tutaj powinna byc jakas walidacja i info we frontendzie, ze seans juz minal
// przypomnij sobie:
//  - jak dziala integracja z payu
//  - jak dziala sesja

    @Autowired
    private SeatReservationByScheduledMovieRepository seatReservationByScheduledMovieRepository;

    @Autowired
    private TicketDataService ticketDataService;

    @RequestMapping(value = "/cinemaHall/seats", method = RequestMethod.GET)
    public ResponseEntity<List<List<SeatReservationByScheduledMovie>>> seatsByCinemaHallAndMovie(HttpSession session, @RequestParam long scheduledMovieId) {
        List<SeatReservationByScheduledMovie> cinemaHallSeats = seatReservationByScheduledMovieRepository.findAllByScheduledMovieId(scheduledMovieId);
        Map<Integer, List<SeatReservationByScheduledMovie>> groupByRow =
                cinemaHallSeats.stream().collect(Collectors.groupingBy(seat -> seat.getSeat().getRowNumber()));
        //1. te czyszczenie sesja najlepiej do osobnej metody w stulu `cleanupSession()`, bo tutaj za bardzo smieci i nie widac logiki zwracania siedzen
        //2. a mzoe w ifie wystarczy tylko (extractedChosenMovieId != scheduledMovieId)?
        Long extractedChosenMovieId = Optional.ofNullable(SessionService.getChosenMovieId(session)).orElse(0L);
        List<ChosenSeatAndPrice> extractedChosenSeatsAndPrices = SessionService.getChosenSeatsAndPrices(session);
        List<ChosenSeatAndPrice> chosenSeatAndPrices = Optional.ofNullable(extractedChosenSeatsAndPrices).orElseGet(ArrayList::new);
        if (extractedChosenMovieId != 0L && extractedChosenMovieId != scheduledMovieId && !chosenSeatAndPrices.isEmpty()) {
            SessionService.removeChosenSeatsAndPrice(session);
            SessionService.removePersonalData(session);
            SessionService.removeReservation(session);
        }
        SessionService.setChosenMovieId(session, scheduledMovieId);
        return new ResponseEntity<>(new ArrayList<>(groupByRow.values()), HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "/cinemaHall/seats/choose/{scheduledMovieId}", method = RequestMethod.POST)
    public ResponseEntity<List<SeatReservationByScheduledMovie>> chosenSeat(HttpSession session, @PathVariable long scheduledMovieId, @RequestBody List<ChosenSeatAndPrice> chosenSeatsAndPrices) {
        List<Long> seatIds = chosenSeatsAndPrices.stream().map(seat -> seat.getSeatId()).collect(Collectors.toList());
        List<SeatReservationByScheduledMovie> seatSeatIdInAndScheduledMovieId = seatReservationByScheduledMovieRepository.findBySeatSeatIdInAndScheduledMovieId(seatIds, scheduledMovieId);
        SessionService.setChosenSeatsAndPrices(session, chosenSeatsAndPrices);
        return new ResponseEntity<>(seatSeatIdInAndScheduledMovieId, HttpStatus.OK);
    }

    @RequestMapping(value = "/cinemaHall/addPerson", method = RequestMethod.POST)
    public ResponseEntity<String> createReservation(HttpSession session, @RequestBody PersonalData personalData) {
        Optional<String> validationResult = PersonalDataValidator.validate(personalData);
        if (validationResult.isPresent()) {
            throw new IllegalArgumentException(validationResult.get());
        } else {
            List<ChosenSeatAndPrice> chosenSeatAndPrices = SessionService.getChosenSeatsAndPrices(session);
            long chosenMovieId = SessionService.getChosenMovieId(session);
            Reservation reservation = new Reservation(chosenMovieId, personalData, chosenSeatAndPrices);
            String reservationId = reservation.getReservationId();
            SessionService.setPersonalData(session, personalData);
            SessionService.setReservation(session, reservation);
            return new ResponseEntity<>(reservationId, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/reservationSummary", method = RequestMethod.GET)
    public ResponseEntity<ReservationSummaryDto> reservationSummary(HttpSession session) {
        List<ChosenSeatAndPrice> chosenSeatAndPrices = SessionService.getChosenSeatsAndPrices(session);
        long chosenMovieId = SessionService.getChosenMovieId(session);
        TicketData ticketData = ticketDataService.findMovie(chosenMovieId, chosenSeatAndPrices);
        PersonalData personalData = SessionService.getPersonalData(session);
        ReservationSummaryDto reservationSummaryDto = new ReservationSummaryDto(ticketData, personalData);
        return new ResponseEntity<>(reservationSummaryDto, HttpStatus.OK);
    }
}
