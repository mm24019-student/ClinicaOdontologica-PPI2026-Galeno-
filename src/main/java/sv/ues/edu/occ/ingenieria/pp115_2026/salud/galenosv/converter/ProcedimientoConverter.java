package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;


/**
 * Convertidor JSF de Procedimiento: traduce entre el objeto y su UUID en texto para
 * que los combos y autocomplete de las páginas (nombre "procedimientoConverter") puedan
 * enviar y recibir la entidad. La lógica común está en AbstractEntityConverter;
 * aquí solo se indica qué DAO busca la entidad (dao) y cuál es su llave (id).
 *
 * @author antonio
 *FacesConverter(value = "consultaConverter", managed = true) indica que esta clase es un convertidor de JSF para la entidad Consulta.
 * El atributo value define el nombre del convertidor, que se puede usar en las páginas JSF para convertir entre objetos Consulta y sus
 * representaciones en cadena.
 */
@FacesConverter(value = "procedimientoConverter", managed = true)
public class ProcedimientoConverter extends AbstractEntityConverter<Procedimiento> {

   // DAO con el que se busca la entidad por su id cuando llega el texto del formulario.
    @Inject
    private ProcedimientoDAO dao;
 
    // Le entrega a AbstractEntityConverter el DAO que debe usar.
    @Override
    protected InterfaceDAO<Procedimiento> dao() {
        return dao;
    }
 
    // Le indica a AbstractEntityConverter cuál es la llave primaria de la entidad.
    @Override
    protected UUID id(Procedimiento entidad) {
        return entidad.getIdProcedimiento();
    }
}