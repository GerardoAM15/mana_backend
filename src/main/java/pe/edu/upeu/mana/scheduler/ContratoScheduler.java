package pe.edu.upeu.mana.scheduler;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import pe.edu.upeu.mana.service.service.ContratoService;

@Component
public class ContratoScheduler {

    public ContratoScheduler(ContratoService contratos) {
        this.contratos = contratos;
    }

    private final ContratoService contratos;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Lima")
    public void actualizarEstados() {
        contratos.actualizarEstados();
    }
}
