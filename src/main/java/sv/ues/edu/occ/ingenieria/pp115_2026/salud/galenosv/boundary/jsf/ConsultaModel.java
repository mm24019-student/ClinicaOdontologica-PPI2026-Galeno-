package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped; 
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primefaces.event.TabChangeEvent;
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
public class ConsultaModel extends AbstracCrudTabsModel<Consulta> {

    // Inyectamos el DAO, que es quien guarda, busca y elimina
    // registros de consulta en la base de datos.
    @Inject
    private ConsultaDAO cDAO;

    @Inject
    private PersonaRolDAO personaRolDAO;

    // Necesarios para el tabChange unificado (ver onTabChange más abajo):
    // solo así sabemos a cuál de los dos hay que avisarle que se activó.
    @Inject
    private ConsultaProcedimientoModel consultaProcedimientoModel;

    @Inject
    private ConsultaProcedimientoPasoModel consultaProcedimientoPasoModel;

    private List<PersonaRol> personasRol;

    private PersonaRol personaRolSeleccionado;

    public PersonaRol getPersonaRolSeleccionado() {
        return personaRolSeleccionado;
    }

    // Le decimos a la clase padre qué DAO debe usar para las operaciones del CRUD.
    @Override
    protected InterfaceDAO<Consulta> getDAO() {
        return cDAO;
    }

    @Override
    protected void resetearHijos() {
        // Si no hay consulta seleccionada (o se va a crear una nueva), la
    // pestaña "Procedimientos de la Consulta" no debe conservar la lista
    // ni el registro de la consulta anterior.
        consultaProcedimientoModel.cargarDe(null);
    }
    
    // Se ejecuta al pulsar "Nuevo": crea un registro vacío con un UUID generado
    @Override
    protected Consulta crearRegistroNuevoBase() {
        Consulta c = new Consulta(UUID.randomUUID());
        c.setFechaInicio(new Date());
        return c;
    }

    // Sin este setter, JSF no puede actualizar el modelo al enviar el
    // formulario (el p:autoComplete de Persona/Rol quedaría "de solo
    // lectura" y el guardado fallaría). Al asignar, se refleja también
    // en el registro que se va a guardar.
    public void setPersonaRolSeleccionado(PersonaRol personaRolSeleccionado) {
        this.personaRolSeleccionado = personaRolSeleccionado;
        if (this.registro != null) {
            this.registro.setIdPersonaRol(personaRolSeleccionado);
        }
    }

    // Al pulsar "Nuevo" no debe arrastrarse la selección anterior.
    @Override
    protected void configurarNuevoRegistro(Consulta nuevoRegistro) {
        this.personaRolSeleccionado = null;
        consultaProcedimientoModel.btnCancelar();
        consultaProcedimientoPasoModel.btnCancelar();
    }

    // Al seleccionar una fila de la tabla, el autoComplete debe mostrar
    // la persona/rol que ya tiene esa consulta guardada.
    @Override
    public void onRowSelect(org.primefaces.event.SelectEvent<Consulta> event) {
        super.onRowSelect(event);
        this.personaRolSeleccionado = event.getObject().getIdPersonaRol();
        consultaProcedimientoModel.btnCancelar();
        consultaProcedimientoPasoModel.btnCancelar();
    }

    // Lista para el combo (se carga solo la primera vez que la página la pide).
    public List<PersonaRol> getPersonasRol() {
        if (personasRol == null) {
            personasRol = personaRolDAO.findRange(0, 100);
        }
        return personasRol;
    }

    public List<PersonaRol> completarPersonasRol(String query) {
        String texto = query == null
                ? ""
                : query.trim().toLowerCase();

        return getPersonasRol().stream()
                .filter(pr -> etiquetaPersonaRol(pr)
                .toLowerCase()
                .contains(texto))
                .collect(java.util.stream.Collectors.toList());
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

    // Único listener de tabChange para las 3 pestañas de Consulta.xhtml.
    // Antes había un p:ajax por cada nivel (pestaña 2 y pestaña 3) sobre el
    // mismo p:tabView, y los dos se ejecutaban SIEMPRE sin importar a cuál
    // pestaña se estaba entrando: al ir a "Pasos del Procedimiento" también
    // se disparaba el cargarDe() de "Procedimientos de la Consulta", que
    // reinicia su estado a NINGUNO y por lo tanto la pestaña 3 (que depende
    // de que la 2 esté en MODIFICAR) volvía a quedar deshabilitada de
    // inmediato. Con un solo listener que mira el título de la pestaña
    // activada, cada hijo solo recarga cuando de verdad le toca.
    public void onTabChange(TabChangeEvent event) {
        String titulo = event.getTab().getTitle();
        if ("Procedimientos de la Consulta".equals(titulo)) {
            consultaProcedimientoModel.cargarDe(this.registro);
        } else if ("Pasos del Procedimiento".equals(titulo)) {
            consultaProcedimientoPasoModel.cargarDe(consultaProcedimientoModel.getRegistro());
        }
    }

}
