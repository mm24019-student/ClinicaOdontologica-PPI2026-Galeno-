package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 * Managed bean de la pestaña "Procedimientos de la Consulta" en Consulta.xhtml:
 * CRUD de los ConsultaProcedimiento de UNA consulta (el padre lo entrega
 * ConsultaModel con cargarDe).
 *
 * Hereda de AbstracdetallecrudModel (cargar por padre, asignar padre,
 * recargar). Como la entidad guarda el procedimiento como UUID suelto, este
 * bean mantiene el objeto procedimientoSeleccionado (ProcedimientoDAO) para el
 * autocomplete y traduce el UUID a nombre para mostrarlo en la tabla.
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ConsultaProcedimientoModel extends AbstracdetallecrudModel<ConsultaProcedimiento, Consulta> {

    @Inject
    private ConsultaProcedimientoDAO cpDAO;

    @Inject
    private ConsultaProcedimientoPasoDAO pasoDAO;

    // Solo hace falta el DAO de Procedimiento: sigue siendo un combo normal
    // porque idProcedimiento en la entidad es un UUID suelto, no una
    // relación @ManyToOne (a diferencia de idConsulta, que sí lo es).
    @Inject
    private ProcedimientoDAO procedimientoDAO;

    @Inject
    private SesionBean sesionBean;

    // Procedimientos del catálogo ya cargados (se piden a la base una sola vez).
    private List<Procedimiento> procedimientos;

    // Procedimiento elegido en el autocomplete; se guarda en registro solo como
    // UUID.
    private Procedimiento procedimientoSeleccionado;

    public Procedimiento getProcedimientoSeleccionado() {
        return procedimientoSeleccionado;
    }

    // Texto de la columna "Paso inicial" de la tabla: solo el primer paso del
    // procedimiento (el que se crea automaticamente), con el formato
    //   Recepción [CREADO] - Milena Mayorga (Atención al cliente)
    public String pasosTexto(ConsultaProcedimiento cp) {
        if (cp == null || cp.getIdConsultaProcedimiento() == null) {
            return "";
        }
        return pasoDAO.findByConsultaProcedimiento(cp.getIdConsultaProcedimiento())
                .stream()
                .findFirst()
                .map(this::lineaPaso)
                .orElse("");
    }

    private String lineaPaso(ConsultaProcedimientoPaso p) {
        String nombre = p.getIdProcedimientoPaso() == null
                ? "" : Objects.toString(p.getIdProcedimientoPaso().getNombre(), "");
        String linea = nombre + " [CREADO]";
        if (p.getIdPersonaRol() != null && p.getIdPersonaRol().getIdPersona() != null) {
            String persona = (Objects.toString(p.getIdPersonaRol().getIdPersona().getNombres(), "") + " "
                    + Objects.toString(p.getIdPersonaRol().getIdPersona().getApellidos(), "")).trim();
            String rol = p.getIdPersonaRol().getIdRol() == null
                    ? "" : Objects.toString(p.getIdPersonaRol().getIdRol().getNombre(), "");
            linea += " - " + persona + (rol.isEmpty() ? "" : " (" + rol + ")");
        }
        return linea;
    }

    // Cuando se elige un procedimiento en el autocomplete, se guarda en registro
    // solo su UUID (idProcedimiento). Si se borra el procedimiento, se guarda
    // null.
    public void setProcedimientoSeleccionado(Procedimiento procedimientoSeleccionado) {
        this.procedimientoSeleccionado = procedimientoSeleccionado;

        if (this.registro != null) {
            this.registro.setIdProcedimiento(
                    procedimientoSeleccionado == null
                            ? null
                            : procedimientoSeleccionado.getIdProcedimiento());
        }
    }

    //Llamado a la funcion filtrarActivos de AbstarcCrudModel para que filtre Procedimientos activos y inactivos
    public List<Procedimiento> completarProcedimientos(String query) {
        return filtrarActivos(getProcedimientos(), query, Procedimiento::getNombre, Procedimiento::getActivo);
    }

    // Al seleccionar una fila, busca el Procedimiento por su UUID para que el
    // autocomplete muestre el que ya tiene guardado ese registro.
    @Override
    public void onRowSelect(SelectEvent<ConsultaProcedimiento> event) {
        super.onRowSelect(event);

        UUID idProcedimiento = event.getObject().getIdProcedimiento();

        if (idProcedimiento != null) {
            this.procedimientoSeleccionado = procedimientoDAO.buscar(idProcedimiento);
        } else {
            this.procedimientoSeleccionado = null;
        }
    }

    @Override
    protected InterfaceDAO<ConsultaProcedimiento> getDAO() {
        return cpDAO;
    }

    //Activamos que si ah iniciado secion se motrara el boton nuevo 
    @Override
    protected boolean permitirAccion() {
        return exigirSesion(sesionBean);
    }

    // El procedimiento se guarda como UUID suelto, así que se busca con el DAO
    // para revisar su estado "activo" actual.
    @Override
    protected boolean validarAntesDeGuardar() {
        if (!requerir(registro.getIdProcedimiento(), "Seleccione el procedimiento",
                "El procedimiento es obligatorio")) {
            return false;
        }
        Procedimiento elegido = procedimientoDAO.buscar(registro.getIdProcedimiento());
        return requerir(elegido, "Procedimiento no encontrado", "El procedimiento elegido ya no existe")
                && requerirActivo(elegido.getActivo(), "Procedimiento inactivo",
                        "No se puede asignar un procedimiento inactivo a la consulta");
    }

    // ---- Los 3 métodos que pide AbstracdetallecrudModel ----
    @Override
    protected List<ConsultaProcedimiento> buscarPorPadre(UUID idPadre) {
        return cpDAO.findByConsulta(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(Consulta padre) {
        return padre.getIdConsulta();
    }

    @Override
    protected void asignarPadre(ConsultaProcedimiento hijo, Consulta padre) {
        hijo.setIdConsulta(padre);
    }

    // Registro nuevo: UUID generado y fecha de inicio = ahora.
    // configurarNuevoRegistro() (heredado) ya se encarga de asignar el
    // padre (la consulta activa) automáticamente después de esto.
    @Override
    protected ConsultaProcedimiento crearRegistroNuevo() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        cp.setFechaInicio(new Date());
        return cp;
    }

    @Override
    protected UUID obtenerId(ConsultaProcedimiento registro) {
        return registro.getIdConsultaProcedimiento();
    }

    // Además de asignar el padre (lo hace la clase base), limpia el procedimiento
    // elegido para que un registro nuevo no arrastre el anterior.
    @Override
    protected void configurarNuevoRegistro(ConsultaProcedimiento nuevoRegistro) {
        super.configurarNuevoRegistro(nuevoRegistro);
        this.procedimientoSeleccionado = null;
    }

    // Combo para elegir qué Procedimiento se está aplicando.
    public List<Procedimiento> getProcedimientos() {
        if (procedimientos == null) {
            procedimientos = procedimientoDAO.findRange(0, 100);
        }
        return procedimientos;
    }

    // Nombre del procedimiento a partir de su UUID suelto (para la tabla).
    public String nombreProcedimiento(UUID id) {
        if (id == null) {
            return "";
        }
        return getProcedimientos().stream()
                .filter(p -> id.equals(p.getIdProcedimiento()))
                .map(Procedimiento::getNombre)
                .findFirst()
                .orElse(id.toString());
    }
}