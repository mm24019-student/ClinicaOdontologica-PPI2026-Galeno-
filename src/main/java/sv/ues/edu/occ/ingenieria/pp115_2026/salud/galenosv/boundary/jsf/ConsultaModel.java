package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

/**
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ConsultaModel extends AbstracCrudModel<Consulta> {

    // Inyectamos el DAO, que es quien guarda, busca y elimina
    // registros de ProcedimientoPaso en la base de datos.
    @Inject
    private ConsultaDAO cDAO;

    @Inject
    private PersonaRolDAO personaRolDAO;

    private List<PersonaRol> personasRol;

    // Le decimos a la clase padre qué DAO debe usar para las operaciones del CRUD.
    @Override
    protected InterfaceDAO<Consulta> getDAO() {
        return cDAO;
    }

    // Se ejecuta al pulsar "Nuevo": crea un registro vacío con un UUID generado
    @Override
    protected Consulta crearRegistroNuevo() {
        Consulta c = new Consulta(UUID.randomUUID());
        c.setFechaInicio(new Date());
        return c;
    }

    // Lista para el combo (se carga solo la primera vez que la página la pide).
    public List<PersonaRol> getPersonasRol() {
        if (personasRol == null) {
            personasRol = personaRolDAO.findRange(0, 100);
        }
        return personasRol;
    }

    // Texto que se ve en el combo y en la tabla: "Ana Pérez - Odontólogo".
    public String etiquetaPersonaRol(PersonaRol pr) {
        if (pr == null || pr.getIdPersona() == null) {
            return "";
        }
        String nombres = Objects.toString(pr.getIdPersona().getNombres(), "");
        String apellidos = Objects.toString(pr.getIdPersona().getApellidos(), "");
        String rol = pr.getIdRol() == null ? "" : Objects.toString(pr.getIdRol().getNombre(), "");
        return (nombres + " " + apellidos).trim() + (rol.isEmpty() ? "" : " - " + rol);
    }

    // Devuelve el UUID (llave primaria) del registro. La clase padre lo usa para
    // identificar cada fila de la tabla, seleccionar una y eliminarla.
    @Override
    protected UUID obtenerId(Consulta registro) {
        return registro.getIdConsulta();
    }
}
