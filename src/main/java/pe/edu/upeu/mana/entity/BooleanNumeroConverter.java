package pe.edu.upeu.mana.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanNumeroConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean valor) {
        return valor == null ? null : (valor ? 1 : 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Integer valor) {
        if (valor == null)
            return null;
        if (valor != 0 && valor != 1)
            throw new IllegalArgumentException("Valor booleano inválido");
        return valor == 1;
    }
}
