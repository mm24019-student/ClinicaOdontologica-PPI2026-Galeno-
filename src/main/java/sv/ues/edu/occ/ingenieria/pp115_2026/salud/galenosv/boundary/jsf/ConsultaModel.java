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
 * Managed bean de la pantalla Consulta.xhtml: CRUD de la consulta (el padre)
 * con 3 pestañas: datos de la Consulta, "Procedimientos de la Consulta"
 * (ConsultaProcedimientoModel) y "Pasos del Procedimiento"
 * (ConsultaProcedimientoPasoModel).
 *
 * Hereda de AbstracCrudTabsModel (que hereda de AbstracCrudModel): de ahí salen
 * el CRUD, la lista y el manejo de pestañas. Aquí se agrega el autocompletado
 * de Persona/Rol (PersonaRolDAO), el reseteo de los dos hijos y el único
 * listener onTabChange que decide cuál hijo debe cargarse.
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
    private SesionBean sesionBean;

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
    
    //Activamos que si ah iniciado secion se motrara el boton nuevo 
    @Override
    protected boolean permitirAccion() {
        return exigirSesion(sesionBean);
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
        setPersonaRolSeleccionado(sesionBean.getPersonaRolActual());
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

    //Llamado a la funcion filtrarActivos de AbstarcCrudModel para que filtre personarol activos y inactivos
    public List<PersonaRol> completarPersonasRol(String query) {
        return filtrarActivos(getPersonasRol(), query, this::etiquetaPersonaRol, this::personaRolActivo);
    }

    // Texto que se ve en el combo y en la tabla: ejempl "Ana Pérez - Odontólogo".
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

    // Texto que muestra la pantalla: en CREAR, el usuario con sesión;
    // en edición, el que quedó guardado en la consulta.
    public String getPersonaRolEtiqueta() {
        if (estado == Estado_Crud.CREAR) {
            PersonaRol actual = sesionBean.getPersonaRolActual();
            return actual == null ? "Sin sesión: seleccione un usuario arriba" : etiquetaPersonaRol(actual);
        }
        return registro == null ? "" : etiquetaPersonaRol(registro.getIdPersonaRol());
    }

    @Override
    protected boolean validarAntesDeGuardar() {
        if (estado == Estado_Crud.CREAR) {
            // Se toma al guardar, por si cambió la sesión después de pulsar "Nuevo"
            setPersonaRolSeleccionado(sesionBean.getPersonaRolActual());
        }
        return requerir(registro.getIdPersonaRol(), "Sin sesión",
                "Seleccione un usuario en el selector de sesión para registrar la consulta");
    }

}
