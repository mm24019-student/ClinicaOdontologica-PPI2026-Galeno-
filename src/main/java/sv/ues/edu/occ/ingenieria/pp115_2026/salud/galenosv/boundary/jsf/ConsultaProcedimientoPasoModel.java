package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

/**
 * Managed bean de la pestaña "Pasos del Procedimiento" en Consulta.xhtml: CRUD
 * de los ConsultaProcedimientoPaso de UN ConsultaProcedimiento (el padre lo
 * entrega ConsultaModel con cargarDe).
 *
 * Hereda de AbstracdetallecrudModel (cargar por padre, asignar padre,
 * recargar). Aquí se agrega el combo de Estado (PENDIENTE, EN_PROCESO,
 * COMPLETADO, CANCELADO) y el autocompletado de Persona/Rol responsable
 * (PersonaRolDAO).
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ConsultaProcedimientoPasoModel
        extends AbstracdetallecrudModel<ConsultaProcedimientoPaso, ConsultaProcedimiento> {

    // Valores permitidos para "estado". Deben coincidir con
    // ConsultaProcedimientoPaso.ESTADOS_VALIDOS_REGEX.
    private static final List<String> ESTADOS = List.of("PENDIENTE", "EN_PROCESO", "COMPLETADO", "CANCELADO");

    @Inject
    private ConsultaProcedimientoPasoDAO pasoDAO;

    @Inject
    private PersonaRolDAO personaRolDAO;

    @Inject
    private SesionBean sesionBean;

    private List<PersonaRol> personasRol;

    @Override
    protected InterfaceDAO<ConsultaProcedimientoPaso> getDAO() {
        return pasoDAO;
    }

     //Activamos que si ah iniciado secion se motrara el boton nuevo 
    @Override
    protected boolean permitirAccion() {
        return exigirSesion(sesionBean);
    }

    
    @Override
    protected void configurarNuevoRegistro(ConsultaProcedimientoPaso nuevoRegistro) {
        super.configurarNuevoRegistro(nuevoRegistro);
        nuevoRegistro.setIdPersonaRol(sesionBean.getPersonaRolActual());
    }

    // Texto que muestra la pantalla: en CREAR, el usuario con sesión;
    // en edición, el que quedó guardado en el paso.
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
            registro.setIdPersonaRol(sesionBean.getPersonaRolActual());
        }
        return requerir(registro.getIdPersonaRol(), "Sin sesión",
                "Seleccione un usuario en el selector de sesión para registrar el paso");
    }

    // ---- Los 3 métodos que pide AbstracdetallecrudModel ----
    @Override
    protected List<ConsultaProcedimientoPaso> buscarPorPadre(UUID idPadre) {
        return pasoDAO.findByConsultaProcedimiento(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(ConsultaProcedimiento padre) {
        return padre.getIdConsultaProcedimiento();
    }

    @Override
    protected void asignarPadre(ConsultaProcedimientoPaso hijo, ConsultaProcedimiento padre) {
        hijo.setIdConsultaProcedimiento(padre);
    }

    // Registro nuevo: UUID generado y estado inicial PENDIENTE.
    // configurarNuevoRegistro() (heredado) ya asigna el padre (el
    // ConsultaProcedimiento activo) automáticamente después de esto.
    @Override
    protected ConsultaProcedimientoPaso crearRegistroNuevo() {
        ConsultaProcedimientoPaso p = new ConsultaProcedimientoPaso(UUID.randomUUID());
        p.setEstado("PENDIENTE");
        p.setFechaInicio(new Date());
        return p;
    }

    @Override
    protected UUID obtenerId(ConsultaProcedimientoPaso registro) {
        return registro.getIdConsultaProcedimientoPaso();
    }

    // Opciones del combo "Estado".
    public List<String> getEstados() {
        return ESTADOS;
    }

    // Lista para el autoComplete de Persona/Rol (se carga solo la primera vez).
    public List<PersonaRol> getPersonasRol() {
        if (personasRol == null) {
            personasRol = personaRolDAO.findRange(0, 100);
        }
        return personasRol;
    }

    //Llamamos a la funcion de abtracCrudModel filtrarActivos para que filtre personalrol
    public List<PersonaRol> completarPersonasRol(String query) {
        return filtrarActivos(getPersonasRol(), query, this::etiquetaPersonaRol, this::personaRolActivo);
    }

    // Etiqueta para mostrar en el autoComplete de Persona/Rol.
    public String etiquetaPersonaRol(PersonaRol pr) {
        if (pr == null || pr.getIdPersona() == null) {
            return "";
        }
        String nombres = Objects.toString(pr.getIdPersona().getNombres(), "");
        String apellidos = Objects.toString(pr.getIdPersona().getApellidos(), "");
        String rol = pr.getIdRol() == null ? "" : Objects.toString(pr.getIdRol().getNombre(), "");
        return (nombres + " " + apellidos).trim() + (rol.isEmpty() ? "" : " - " + rol);
    }
}
