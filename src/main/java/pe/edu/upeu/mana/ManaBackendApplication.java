package pe.edu.upeu.mana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ManaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManaBackendApplication.class, args);
    }
}
