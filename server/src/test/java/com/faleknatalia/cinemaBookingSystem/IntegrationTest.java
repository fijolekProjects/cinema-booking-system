package com.faleknatalia.cinemaBookingSystem;

import com.faleknatalia.cinemaBookingSystem.controllers.MakeReservationController;
import com.faleknatalia.cinemaBookingSystem.controllers.WhatsOnController;
import com.faleknatalia.cinemaBookingSystem.dto.ChosenSeatAndPrice;
import com.faleknatalia.cinemaBookingSystem.model.SeatReservationByScheduledMovie;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class IntegrationTest {
    @Autowired
    private MakeReservationController makeReservationController;

    @Test
    public void seatReservationTest() {
        //given
        long scheduledMovieId = 1L;
        MockHttpSession session = new MockHttpSession();
        List<ChosenSeatAndPrice> chosenSeatAndPrices = Arrays.asList(new ChosenSeatAndPrice(2L, 1L));

        //when
        ResponseEntity<List<SeatReservationByScheduledMovie>> seatReservationEntity = makeReservationController.chosenSeat(session, scheduledMovieId, chosenSeatAndPrices);

        //then
        SeatReservationByScheduledMovie seatReservation = seatReservationEntity.getBody().get(0);
        Assert.assertEquals(scheduledMovieId, session.getAttribute("chosenMovieId"));
        Assert.assertEquals(chosenSeatAndPrices, session.getAttribute("chosenSeatsAndPrices"));
        Assert.assertEquals(seatReservation.getScheduledMovieId(), scheduledMovieId);
    }
}
