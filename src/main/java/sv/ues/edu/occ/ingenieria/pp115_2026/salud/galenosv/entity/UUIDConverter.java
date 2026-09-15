package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/**
 * @author oscar
 */
// autoApply = true: JPA aplica este convertidor automáticamente a todo
// atributo de tipo UUID en las entidades, sin tener que anotar cada campo
@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, UUID> {

    @Override
    // Se ejecuta al GUARDAR: convierte el UUID de la entidad Java
    // al valor que se escribe en la columna de la base de datos.
    // Aquí no hace ninguna transformación real, solo lo retorna igual.
    public UUID convertToDatabaseColumn(UUID uuid) {
        return uuid;
    }

    @Override
    // Se ejecuta al LEER: convierte el valor que viene de la columna
    // de la base de datos de vuelta al UUID que usa la entidad Java.
    // Igual que arriba, no transforma nada, es un "pass-through".
    public UUID convertToEntityAttribute(UUID db) {
        return db;
    }
}