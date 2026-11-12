package pe.edu.upeu.mana;

import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import pe.edu.upeu.mana.entity.*;
import pe.edu.upeu.mana.enums.TipoSerie;
import pe.edu.upeu.mana.repository.*;
import pe.edu.upeu.mana.service.CorrelativoComprobante;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="spring.jpa.hibernate.ddl-auto=validate")
@EnabledIfEnvironmentVariable(named="MANA_ORACLE_TEST",matches="true")
class ComprobanteCorrelativoOracleTest {
    @Autowired SerieComprobanteRepository series;
    @Autowired LocalRepository locales;
    @Autowired CorrelativoComprobante correlativos;
    @Autowired PlatformTransactionManager manager;
    @Test void correlativosConcurrentesConBloqueoRealSinDuplicados() throws Exception {
        var tx=new TransactionTemplate(manager);
        var fixture=tx.execute(status -> {
            var local=new Local(); local.setNombre("Prueba correlativos "+UUID.randomUUID()); locales.saveAndFlush(local);
            var usadas=series.findAll().stream().map(SerieComprobante::getSerie).toList();
            String nombre=java.util.stream.IntStream.rangeClosed(1,999).mapToObj(n -> "B%03d".formatted(n)).filter(n -> !usadas.contains(n)).findFirst().orElseThrow();
            var serie=new SerieComprobante(); serie.setLocalId(local.getId()); serie.setTipo(TipoSerie.BOLETA); serie.setSerie(nombre); series.saveAndFlush(serie);
            return new Long[]{local.getId(),serie.getId()};
        });
        try(var pool=Executors.newFixedThreadPool(4)) {
            var tareas=new ArrayList<Future<Integer>>();
            for(int i=0;i<20;i++) tareas.add(pool.submit(() -> tx.execute(status -> correlativos.siguiente(fixture[0],TipoSerie.BOLETA).numero())));
            var numeros=new HashSet<Integer>(); for(var t:tareas) numeros.add(t.get(30,TimeUnit.SECONDS));
            assertEquals(20,numeros.size()); assertTrue(numeros.containsAll(java.util.stream.IntStream.rangeClosed(1,20).boxed().toList()));
            assertEquals(20,series.findById(fixture[1]).orElseThrow().getUltimoNumero());
        } finally {
            tx.executeWithoutResult(status -> { series.deleteById(fixture[1]); series.flush(); locales.deleteById(fixture[0]); });
        }
    }
    @Test void formatoSunat() { assertEquals("B001-00000001",new CorrelativoComprobante.Numero(1L,"B001",1).formateado()); }
}
