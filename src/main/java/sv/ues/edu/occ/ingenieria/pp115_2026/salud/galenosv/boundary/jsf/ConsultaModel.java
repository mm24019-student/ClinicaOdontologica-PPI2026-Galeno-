package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
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

    // Título de la pestaña que contiene procedimientos y pasos (debe coincidir
    // con el p:tab de Consulta.xhtml).
    private static final String TAB_PASOS = "Procedimientos";

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

    private PersonaRol pacienteEncontrado;   // lo que se elige en el diálogo

    private PersonaRol personaRolSeleccionado;

    // ---- Filtro de la tabla (fechas y clínica de la sesión) ----
    private static final int MAX_FILTRO = 100;

    // Zona horaria con la que se interpretan los días del filtro (la misma de los
    // p:datePicker de la pantalla), sin depender de la zona del servidor.
    private static final java.util.TimeZone ZONA = java.util.TimeZone.getTimeZone("America/El_Salvador");

    // Rango de fecha de inicio que escribe el usuario (null = sin límite).
    private Date fechaDesde = new Date();
    private Date fechaHasta = new Date();

    // Clínica con la que se cargó la lista por última vez. Sirve para saber si
    // la sesión cambió de clínica y hay que volver a cargar (ver sincronizarClinica).
    private UUID clinicaCargada;

    public PersonaRol getPersonaRolSeleccionado() {
        return personaRolSeleccionado;
    }

    public List<PersonaRol> completarPacientesPorDocumento(String query) {
        return personaRolDAO.buscarPacientesPorDocumento(query, idClinicaActual(), 20);
    }

    public void abrirBuscarPacientePorDocumento() {
        if (!isPuedeBuscarPaciente()) {
            mensaje(FacesMessage.SEVERITY_WARN, "No disponible",
                    "Solo puede buscar si hay una clínica seleccionada y está creando una consulta");
            return;
        }
        pacienteEncontrado = null;
        PrimeFaces.current().executeScript("PF('dlgPacienteDoc').show()");
    }

    // Le decimos a la clase padre qué DAO debe usar para las operaciones del CRUD.
    @Override
    protected InterfaceDAO<Consulta> getDAO() {
        return cDAO;
    }

    public PersonaRol getPacienteEncontrado() {
        return pacienteEncontrado;
    }

    public void setPacienteEncontrado(PersonaRol p) {
        this.pacienteEncontrado = p;
    }

    // Solo se puede buscar si hay clínica en la sesión y se está creando una consulta.
    public boolean isPuedeBuscarPaciente() {
        return estado == Estado_Crud.CREAR && idClinicaActual() != null;
    }

    public List<PersonaRol> completarPacientes(String query) {
        return personaRolDAO.buscarPacientes(query, idClinicaActual(), 20);
    }

    public void abrirBuscarPaciente() {
        if (!isPuedeBuscarPaciente()) {
            mensaje(FacesMessage.SEVERITY_WARN, "No disponible",
                    "Solo puede buscar si hay una clínica seleccionada y está creando una consulta");
            return;
        }
        pacienteEncontrado = null;
        PrimeFaces.current().executeScript("PF('dlgPaciente').show()");
    }

    public void seleccionarPaciente() {
        if (pacienteEncontrado == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "Seleccione un paciente", "Busque y elija un paciente de la lista");
            return;
        }
        setPersonaRolSeleccionado(pacienteEncontrado);   // también lo asigna al registro
        pacienteEncontrado = null;
        PrimeFaces.current().executeScript("PF('dlgPaciente').hide(); PF('dlgPacienteDoc').hide();");
    }

// Solo nombre (sin " - Paciente"), para la tabla y el formulario.
    public String etiquetaPaciente(PersonaRol pr) {
        if (pr == null || pr.getIdPersona() == null) {
            return "";
        }
        return (Objects.toString(pr.getIdPersona().getNombres(), "") + " "
                + Objects.toString(pr.getIdPersona().getApellidos(), "")).trim();
    }

    public String getNombrePaciente() {
        return registro == null ? "" : etiquetaPaciente(registro.getIdPersonaRol());
    }

// "DUI: 00000000-3 pasaporte: pasAABBCC"
    public String documentosDe(PersonaRol pr) {
        if (pr == null || pr.getIdPersona() == null || pr.getIdPersona().getDocumentoList() == null) {
            return "";
        }
        return pr.getIdPersona().getDocumentoList().stream()
                .map(d -> (d.getIdTipoDocumento() == null ? "" : d.getIdTipoDocumento().getNombre() + ": ") + d.getValor())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    //Activamos que si ah iniciado secion se motrara el boton nuevo 
    @Override
    protected boolean permitirAccion() {
        return exigirSesion(sesionBean);
    }

    @Override
    protected void resetearHijos() {
        // Si no hay consulta seleccionada (o se va a crear una nueva), la
        // pestaña "Pasos del Procedimiento" no debe conservar la lista de
        // procedimientos ni los pasos de la consulta anterior.
        consultaProcedimientoModel.cargarDe(null);
        consultaProcedimientoPasoModel.cargarDe(null);
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
        setPersonaRolSeleccionado(null);   // antes: sesionBean.getPersonaRolActual()
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
        return filtrarActivos(getPersonasRol(), query, this::etiquetaPersonaRol,
                pr -> personaRolActivo(pr) && personaRolDeLaClinicaActual(pr, sesionBean));
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

    // =====================================================================
    // Filtro por fechas y por clínica
    // =====================================================================
    // Clínica de la sesión abierta (null si no hay sesión o su rol no tiene clínica).
    private UUID idClinicaActual() {
        if (sesionBean == null || sesionBean.getClinicaActual() == null) {
            return null;
        }
        return sesionBean.getClinicaActual().getIdClinica();
    }

    // Texto que se muestra arriba de la tabla.
    public String getNombreClinicaFiltro() {
        if (sesionBean == null || !sesionBean.isAutenticado()) {
            return "Todas las clínicas (sin sesión)";
        }
        return sesionBean.getClinicaActual() == null
                ? "Sin clínica asignada"
                : sesionBean.getClinicaActual().getNombre();
    }

    // Botón "Buscar": filtra por el rango tal como está, sin validar el orden de las fechas.
    public void buscar() {
        recargarLista();
    }

    // Se llama en cada render de la página (f:event preRenderView): si el
    // usuario cambió de sesión y por tanto de clínica, la tabla se vuelve a
    // cargar para mostrar solo las consultas de la clínica nueva.
    public void sincronizarClinica() {
        if (!Objects.equals(idClinicaActual(), clinicaCargada)) {
            recargarLista();
        }
    }

    // Sin filtros activos se comporta como siempre (findRange). Con fechas o
    // con clínica de sesión usa la consulta filtrada del DAO.
    @Override
    protected void recargarLista() {
        UUID idClinica = idClinicaActual();
        clinicaCargada = idClinica;
        if (fechaDesde == null && fechaHasta == null && idClinica == null) {
            super.recargarLista();
            return;
        }
        setWrappedData(cDAO.buscarConFiltro(inicioDelDia(fechaDesde),
                inicioDelDiaSiguiente(fechaHasta), idClinica, MAX_FILTRO));
    }

    private Date inicioDelDia(Date d) {
        if (d == null) {
            return null;
        }
        Calendar c = Calendar.getInstance(ZONA);
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    // Para que "Hasta" incluya el día completo.
    private Date inicioDelDiaSiguiente(Date d) {
        Date inicio = inicioDelDia(d);
        if (inicio == null) {
            return null;
        }
        Calendar c = Calendar.getInstance(ZONA);
        c.setTime(inicio);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    // Devuelve el UUID (llave primaria) del registro. La clase padre lo usa para
    // identificar cada fila de la tabla, seleccionar una y eliminarla.
    @Override
    protected UUID obtenerId(Consulta registro) {
        return registro.getIdConsulta();
    }

    // Listener de tabChange de Consulta.xhtml. Al entrar a "Pasos del
    // Procedimiento" se cargan los procedimientos de la consulta actual y se
    // limpian los pasos, porque todavía no hay un procedimiento elegido.
    // "Datos de la Consulta" no necesita recargar nada.
    public void onTabChange(TabChangeEvent event) {
        if (TAB_PASOS.equals(event.getTab().getTitle())) {
            consultaProcedimientoModel.cargarDe(this.registro);
            consultaProcedimientoPasoModel.cargarDe(null);
        }
    }

    // Se ejecuta al elegir una fila de la tabla de procedimientos: selecciona
    // el procedimiento (autocomplete incluido) y carga sus pasos.
    public void onProcedimientoRowSelect(SelectEvent<ConsultaProcedimiento> event) {
        consultaProcedimientoModel.onRowSelect(event);
        consultaProcedimientoPasoModel.cargarDe(event.getObject());
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
        return requerir(registro.getIdPersonaRol(), "Seleccione un paciente",
                "Use \"Por Nombre\" para buscar y elegir el paciente de la consulta");
    }

}