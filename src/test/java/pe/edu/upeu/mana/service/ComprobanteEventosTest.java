package pe.edu.upeu.mana.service;
import pe.edu.upeu.mana.service.service.EnvioComprobanteService;
import pe.edu.upeu.mana.service.impl.EnvioComprobanteServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;
import pe.edu.upeu.mana.config.FacturacionProperties;
import pe.edu.upeu.mana.repository.ComprobanteRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComprobanteEventosTest {
    @Configuration @EnableTransactionManagement
    static class Config {
        @Bean EnvioComprobanteService envio() { return mock(EnvioComprobanteService.class); }
        @Bean ProcesadorComprobantes procesador(EnvioComprobanteService e) { return new ProcesadorComprobantes(e,mock(ComprobanteRepository.class),new FacturacionProperties(),mock(CorreoComprobante.class)); }
        @Bean PlatformTransactionManager manager() {
            return new AbstractPlatformTransactionManager() {
                protected Object doGetTransaction() { return new Object(); }
                protected void doBegin(Object t,TransactionDefinition d) {}
                protected void doCommit(DefaultTransactionStatus s) {}
                protected void doRollback(DefaultTransactionStatus s) {}
            };
        }
    }
    @Test void eventoSoloSeProcesaDespuesDelCommitYNoEnRollback() {
        try(var ctx=new AnnotationConfigApplicationContext(Config.class)) {
            var tx=new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)); var e=ctx.getBean(EnvioComprobanteService.class);
            tx.executeWithoutResult(s -> { ctx.publishEvent(new ComprobanteReservado(1L)); verifyNoInteractions(e); });
            verify(e).enviar(1L); clearInvocations(e);
            tx.executeWithoutResult(s -> { ctx.publishEvent(new ComprobanteReservado(2L)); s.setRollbackOnly(); });
            verifyNoInteractions(e);
        }
    }
    @Test void errorDeEnvioNoPropagaTrasCommit() {
        try(var ctx=new AnnotationConfigApplicationContext(Config.class)) {
            var tx=new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)); var e=ctx.getBean(EnvioComprobanteService.class);
            doThrow(new IllegalStateException("simulado")).when(e).enviar(1L);
            assertDoesNotThrow(() -> tx.executeWithoutResult(s -> ctx.publishEvent(new ComprobanteReservado(1L))));
        }
    }
}
