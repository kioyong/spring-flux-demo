package com.yong.example.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Log4j2
@SpringBootApplication
public class ClientApplication {

    @Autowired
    WebClient http;

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    WebClient http(WebClient.Builder builder) {
        return builder
//            .baseUrl("http://localhost:8080")
            .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        System.out.println("start");

//        Flux<String> s1 =Flux.just("abc");
//        Flux<Integer> s2 = Flux.just(1);
//
//        Flux.zip(s1,s2)
//            .map(tuple -> tuple.getT1() + ':' + tuple.getT2())
//            .subscribe(log::info);


        Flux<String> telemetry = null;//in

        Flux<Integer> out = null;//out

        Flux<Integer> integerFlux = out.takeUntilOther(telemetry);


//        // hedging
//        Flux<String> host1 = null;//todo host1
//        Flux<String> host2 = null;//todo host2
//        Flux<String> host3 = null;//todo host3
//
//        //select(fd*)
//        Flux<String> first = Flux.first(host1, host2, host3);


        Flux<String> timeout = http
            .get()
            .uri("http://localhost:8080/reservations")
            .retrieve()
            .bodyToFlux(Reservation.class)
            .map(Reservation::getName)
            .onErrorResume(exception -> Flux.just("error ~~~~"))
            .retryWhen(Retry.backoff(10, Duration.ofSeconds(1)))
            .timeout(Duration.ofSeconds(1));

        timeout.subscribe(log::info);


    }

}


@Data
@NoArgsConstructor
@AllArgsConstructor
class Reservation {
    private String id;
    private String name;
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