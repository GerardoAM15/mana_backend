package pe.edu.upeu.mana.config;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.*;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "fechaAuditoria")
public class AuditoriaConfig {

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return Optional.of(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken) ? auth.getName() : "sistema");
        };
    }

    @Bean
    DateTimeProvider fechaAuditoria(Clock clock) {
        return () -> Optional.of(OffsetDateTime.now(clock));
    }
}
