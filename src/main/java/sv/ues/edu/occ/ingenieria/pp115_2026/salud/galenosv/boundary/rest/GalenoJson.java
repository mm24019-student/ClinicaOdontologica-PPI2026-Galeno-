package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;

// JSON-B compartido por servidor y cliente: ignora las propiedades tipo lista
// (rompe los ciclos entre entidades) y lo que agrega el weaving de EclipseLink.
public final class GalenoJson {

    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig()
            .withDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT)
            .withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
                @Override
                public boolean isVisible(Field field) {
                    return false;
                }

                @Override
                public boolean isVisible(Method method) {
                    if (method.getName().startsWith("_persistence")) {
                        return false;
                    }
                    Class<?> tipo = method.getParameterCount() == 0
                            ? method.getReturnType() : method.getParameterTypes()[0];
                    return !Collection.class.isAssignableFrom(tipo);
                }
            }));

    private GalenoJson() {
    }

    public static Jsonb get() {
        return JSONB;
    }
}