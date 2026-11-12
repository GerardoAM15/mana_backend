package pe.edu.upeu.mana.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class FechaConfig {

    @Bean
    Clock clock(@Value("${app.zona-horaria}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
