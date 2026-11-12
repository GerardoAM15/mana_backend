package pe.edu.upeu.mana.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.mana.entity.Cliente;
import pe.edu.upeu.mana.dto.cliente.ClienteRequestDTO;
import pe.edu.upeu.mana.dto.cliente.ClienteResponseDTO;

@Component
public class ClienteMapper {

    public ClienteResponseDTO toResponse(Cliente e) {
        return new ClienteResponseDTO(e.getId(), e.getDni(), e.getNombre(), e.getApellido(), e.getTelefono(), e.getActivo(), e.getVersion());
    }

    public void actualizar(Cliente e, ClienteRequestDTO r) {
        e.setDni(r.getDni());
        e.setNombre(r.getNombre());
        e.setApellido(r.getApellido());
        e.setTelefono(r.getTelefono());
        e.setActivo(r.getActivo());
    }
}
