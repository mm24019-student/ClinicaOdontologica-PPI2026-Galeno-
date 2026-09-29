package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author antonio
 */
//FacesConverter(value = "consultaConverter", managed = true) indica que esta clase es un convertidor de JSF para la entidad Consulta.
//El atributo value define el nombre del convertidor, que se puede usar en las páginas JSF para convertir entre objetos Consulta y sus
//representaciones en cadena.
@FacesConverter(value = "consultaConverter", managed = true)
public class ConsultaConverter extends AbstractEntityConverter<Consulta> {

    @Inject
    private ConsultaDAO dao;

    @Override
    protected InterfaceDAO<Consulta> dao() {
        return dao;
    }

    @Override
    protected UUID id(Consulta entidad) {
        return entidad.getIdConsulta();
    }
}
