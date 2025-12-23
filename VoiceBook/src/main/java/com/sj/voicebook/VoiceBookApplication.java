package com.sj.voicebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VoiceBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceBookApplication.class, args);
    }

}
