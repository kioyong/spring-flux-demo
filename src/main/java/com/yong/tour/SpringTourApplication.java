package com.yong.tour;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class SpringTourApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTourApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction(ReservationRepository rr) {
        return route()
                .GET("reservations", r -> ok().body(rr.findAll(), Reservation.class))
                .build();
    }
}

@Configuration
class WebSocketConfiguration {

    @Bean
    SimpleUrlHandlerMapping mapping(WebSocketHandler wsh) {
        return new SimpleUrlHandlerMapping(Map.of("/ws/greetings", wsh), 10);
    }

    @Bean
    WebSocketHandler webSocketHandler(GreetingService greetingService) {
        return session -> {
            Flux<WebSocketMessage> socketMessageFlux = session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .map(GreetingRequest::new)
                    .flatMap(greetingService::greet)
                    .map(GreetingResponse::getMessage)
                    .map(session::textMessage);
            return session.send(socketMessageFlux);
        };
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingRequest {
    private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GreetingResponse {
    private String message;

}

@Service
class GreetingService {

    Flux<GreetingResponse> greet(GreetingRequest request) {
        return Flux
                .fromStream(Stream.generate(() -> new GreetingResponse("Hello, " + request.getName() + " @ " + Instant.now() + "!")))
                .delayElements(Duration.ofSeconds(1));
    }
}

@Log4j2
@Component
@RequiredArgsConstructor
class Initializer {

    private final ReactiveCrudRepository reactiveCrudRepository;

//    @EventListener(ApplicationReadyEvent.class)
//    public void begin() {
//        var names = Flux.just("LiangYong", "ZhaoXian", "YingWen", "ChengYu", "KaiWen", "GuanHeng", "HuangXin", "XiaoXi")
//                .map(name -> new Reservation(null, name))
//                .flatMap(reservation -> reactiveCrudRepository.save(reservation));
//
//        reactiveCrudRepository.deleteAll()
//                .thenMany(names)
//                .thenMany(reactiveCrudRepository.findAll())
//                .subscribe(log::info);
//    }
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, String> {

    @Aggregation
    List<String>  getAllIdList();
}@Data
@NoArgsConstructor
@AllArgsConstructor
class Reservation {
    @Id
    private String id;
    private String name;
}
