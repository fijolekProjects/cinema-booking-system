package com.faleknatalia.cinemaBookingSystem.payment;

import com.faleknatalia.cinemaBookingSystem.model.PersonalData;
import com.faleknatalia.cinemaBookingSystem.model.Reservation;
import com.faleknatalia.cinemaBookingSystem.model.TicketPrice;
import com.faleknatalia.cinemaBookingSystem.repository.PersonalDataRepository;
import com.faleknatalia.cinemaBookingSystem.repository.ReservationRepository;
import com.faleknatalia.cinemaBookingSystem.repository.SeatReservationByScheduledMovieRepository;

import com.faleknatalia.cinemaBookingSystem.repository.TicketPriceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentService {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PersonalDataRepository personalDataRepository;

    @Autowired
    SeatReservationByScheduledMovieRepository seatReservationByScheduledMovieRepository;

    @Autowired
    OrderRequestDBRepository orderRequestDBRepository;

    @Autowired
    TicketPriceRepository ticketPriceRepository;


    public AccessToken generateAccessToken(String client_id, String client_secret) {
        //TODO to musi byc w konfiguracji
        String url = "https://secure.snd.payu.com/pl/standard/user/oauth/authorize";

        RestTemplate restTemplate = new RestTemplate();

        //TODO to nie jest json? :P
        String requestJson = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s", client_id, client_secret);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        return restTemplate.postForObject(url, entity, AccessToken.class);
    }

    public OrderResponse generateOrder(AccessToken token, long reservationId, long personalDataId, String clientId) throws JsonProcessingException {

        //TODO te komentarze typu `Order data` i PRODUCT sa zbedne, trzeba usunac
        //Order data
        Reservation reservation = reservationRepository.findOne(reservationId);
        PersonalData personalData = personalDataRepository.findOne(personalDataId);
        List<TicketPrice> ticketPrices =
                reservation.getChosenSeatsAndPrices().stream().map(chosenSeatAndPrice -> ticketPriceRepository.findOne(chosenSeatAndPrice.getTicketPriceId())).collect(Collectors.toList());
        //TODO to musi byc w konfiguracji
        String url = "https://secure.snd.payu.com/api/v2_1/orders";
        //TODO przenies tworzenie restTemplate do momentu jego uzycia
        RestTemplate restTemplate = new RestTemplate();
        Buyer buyer = new Buyer(personalData.getEmail(), personalData.getPhoneNumber(), personalData.getName(), personalData.getSurname());

        //PRODUCT
        //TODO stream().map() zawsze cos zwraca!
        //moze byc
        //List<Product> products = ticketPrices.stream().map(ticketPrice ->
        //                products.add(new Product("Ticket", Integer.toString(ticketPrice.getTicketValue() * 100), "1")))
        //                .collect(Collectors.toList());
        List<Product> products = new ArrayList<>();
        ticketPrices.stream().map(ticketPrice ->
                products.add(new Product("Ticket", Integer.toString(ticketPrice.getTicketValue() * 100), "1")))
                .collect(Collectors.toList());
        String extOrderId = UUID.randomUUID().toString();
        //TODO zrob osobna metode toCents() ktora bedzie robila to `Integer.toString(ticketPrice.getTicketValue() * 100)`
        //TODO te wszystkie stringi do konfiguracji, no moze oprocz description i currency
        OrderRequest orderRequest = new OrderRequest(
                extOrderId,
                "http://localhost:8080/notify", "127.0.0.1",
                clientId,
                "Bilecik do kina", "PLN", Integer.toString(sumOfTicketPrice(ticketPrices) * 100), buyer, products, "http://localhost:3000/#/paymentSuccess");

        //zapis do bazy OrderRequest
        ObjectMapper mapper = new ObjectMapper();
        orderRequestDBRepository.save(new OrderRequestsAndResponseDB(extOrderId, reservationId, "request", mapper.writeValueAsString(orderRequest)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token.getAccess_token());

        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);


        return restTemplate.postForObject(url, entity, OrderResponse.class);

    }

    private int sumOfTicketPrice(List<TicketPrice> chosenSeatsPrice) {
        //TODO sprobuj przepisac na streamy, tam jest metoda sum
        int sum = 0;
        for (TicketPrice ticketPrice : chosenSeatsPrice) {
            sum = sum + ticketPrice.getTicketValue();
        }
        return sum;
    }

}
