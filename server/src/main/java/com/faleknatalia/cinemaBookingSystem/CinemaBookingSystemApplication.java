package com.faleknatalia.cinemaBookingSystem;

import com.faleknatalia.cinemaBookingSystem.model.CinemaHall;
import com.faleknatalia.cinemaBookingSystem.model.Movie;
import com.faleknatalia.cinemaBookingSystem.model.ScheduledMovie;
import com.faleknatalia.cinemaBookingSystem.model.TicketPrice;
import com.faleknatalia.cinemaBookingSystem.repository.*;
import com.faleknatalia.cinemaBookingSystem.data.CinemaHallGenerator;
import com.faleknatalia.cinemaBookingSystem.data.ScheduledMovieGenerator;
import com.faleknatalia.cinemaBookingSystem.data.SeatReservationByScheduledMovieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@SpringBootApplication
public class CinemaBookingSystemApplication implements CommandLineRunner {

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    ScheduledMovieRepository scheduledMovieRepository;

    @Autowired
    CinemaHallRepository cinemaHallRepository;

    @Autowired
    TicketPriceRepository ticketPriceRepository;

    @Autowired
    ScheduledMovieGenerator scheduledMovieGenerator;

    @Autowired
    SeatReservationByScheduledMovieGenerator seatReservationByScheduledMovieGenerator;

    @Autowired
    SeatReservationByScheduledMovieRepository seatReservationByScheduledMovieRepository;

    public static void main(String[] args) {
        SpringApplication.run(CinemaBookingSystemApplication.class, args);
    }

    public void run(String... strings) throws Exception {

        //TODO dodalbym w konfiguracji flage generateSampleData, i tutaj generowal dane jesli generateSampleData = true
        Movie movie1 = new Movie("The Prestige", "After a tragic accident two stage magicians engage in a battle to create the ultimate illusion whilst sacrificing everything they have to outwit the other.", 120, "http://1.fwcdn.pl/po/99/45/259945/7536864.6.jpg");
        Movie movie2 = new Movie("Catch me if you can", "A seasoned FBI agent pursues Frank Abagnale Jr. who, before this 19th birthday, successfully forged millions of dollars' worth of checks while posing as a Pan Am pilot, a doctor, and a legal prosecutor.", 125, "http://1.fwcdn.pl/po/23/62/32362/7519166.6.jpg");
        Movie movie3 = new Movie("12 Angry Men","A jury holdout attempts to prevent a miscarriage of justice by forcing his colleagues to reconsider the evidence.", 95, "http://1.fwcdn.pl/po/07/01/30701/7492190.6.jpg");
        Movie movie4 = new Movie("The Silence of the Lambs", "A young F.B.I. cadet must receive the help of an incarcerated and manipulative cannibal killer to help catch another serial killer, a madman who skins his victims." , 120, "http://1.fwcdn.pl/po/10/47/1047/7714177.6.jpg");
        Movie movie5 = new Movie("Life of Brian", "Born on the original Christmas in the stable next door to Jesus, Brian of Nazareth spends his life being mistaken for a messiah.", 95, "http://1.fwcdn.pl/po/10/99/1099/7364767.6.jpg");
        Movie movie6 = new Movie("Coco", "Aspiring musician Miguel, confronted with his family's ancestral ban on music, enters the Land of the Dead to find his great-great-grandfather, a legendary singer.", 120, "http://1.fwcdn.pl/po/22/60/752260/7813486.6.jpg");

        CinemaHall cinemaHallOne = new CinemaHallGenerator().generateCinemaHall(7,12);
        CinemaHall cinemaHallTwo = new CinemaHallGenerator().generateCinemaHall(8,14);

        cinemaHallRepository.save(cinemaHallOne);
        cinemaHallRepository.save(cinemaHallTwo);

        //TODO tutaj mozesz zapisywac cale listy
        movieRepository.save(movie1);
        movieRepository.save(movie2);
        movieRepository.save(movie3);
        movieRepository.save(movie4);
        movieRepository.save(movie5);
        movieRepository.save(movie6);

        Map<LocalDateTime, List<ScheduledMovie>> weekWhatsOn = scheduledMovieGenerator.generateWeekWhatsOn(1, LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
        weekWhatsOn.values().stream().forEach(s -> scheduledMovieRepository.save(s));

        Map<LocalDateTime, List<ScheduledMovie>> weekWhatsOn2 = scheduledMovieGenerator.generateWeekWhatsOn(2, LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
        weekWhatsOn2.values().stream().forEach(s -> scheduledMovieRepository.save(s));

        seatReservationByScheduledMovieRepository.save(seatReservationByScheduledMovieGenerator.generateSeatsReservationByScheduledMovies());

        TicketPrice ticketPriceForStudent = new TicketPrice("student",7);
        TicketPrice ticketPriceNormal = new TicketPrice("normal",10);
        TicketPrice ticketPriceForSenior = new TicketPrice("senior",8);
        ticketPriceRepository.save(ticketPriceNormal);
        ticketPriceRepository.save(ticketPriceForSenior);
        ticketPriceRepository.save(ticketPriceForStudent);

        //TODO przeniesc wszystkie save'y w jedno miejsce na koncu, oddzielic generowanie danych od zapisywania danych - to jest bardzo wazne
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }

}
