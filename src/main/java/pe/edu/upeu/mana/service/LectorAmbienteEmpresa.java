package pe.edu.upeu.mana.service;

import java.util.*;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class LectorAmbienteEmpresa {
    private static final Set<String> VALORES = Set.of("beta", "produccion", "nubefact_beta", "nubefact_produccion");

    public Optional<String> leer(JsonNode environment) {
        if (environment == null || environment.isNull()) return Optional.empty();
        if (environment.isTextual()) return VALORES.contains(environment.asText()) ? Optional.of(environment.asText()) : Optional.empty();
        if (!environment.isObject()) return Optional.empty();
        // Formato real de GET /companies/{id} (verificado el 2026-11-10):
        // "environment": {"nombre": "beta", "fe_url": "...", "re_url": "...", ...}
        var nombre = environment.get("nombre");
        if (nombre != null && nombre.isTextual()) return VALORES.contains(nombre.asText()) ? Optional.of(nombre.asText()) : Optional.empty();
        // Respaldo por si APIsPERU cambia el nombre del campo: buscar un único valor conocido.
        var encontrados = new HashSet<String>();
        var pendientes = new ArrayDeque<JsonNode>();
        pendientes.add(environment);
        while (!pendientes.isEmpty()) {
            var nodo = pendientes.removeFirst();
            if (nodo.isTextual() && VALORES.contains(nodo.asText())) encontrados.add(nodo.asText());
            else if ((nodo.isObject() || nodo.isArray())) nodo.forEach(pendientes::addLast);
        }
        return encontrados.size() == 1 ? Optional.of(encontrados.iterator().next()) : Optional.empty();
    }
}
