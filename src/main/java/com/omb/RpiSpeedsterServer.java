package com.omb;

import com.omb.board.RpiSpeedster;
import com.omb.board.Speedster;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RpiSpeedsterServer {

    public static void main(String... args) {
        SpringApplication.run(RpiSpeedsterServer.class, args);
    }

    @Bean
    public Speedster getSpeedster() {
        RpiSpeedster speedster = new RpiSpeedster();
        speedster.initialize();
        return speedster;
    }

}
