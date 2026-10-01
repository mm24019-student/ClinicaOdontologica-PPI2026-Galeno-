package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 * Managed bean del diálogo "Gestionar Examen del Procedimiento": CRUD de los
 * ProcedimientoPasoExamen de UN paso (el paso se le entrega con
 * abrirGestionExamen(paso) o con cargarDe(paso)).
 *
 * Hereda de AbstracdetallecrudModel (cargar por padre, asignar padre,
 * recargar). Igual que ProcedimientoPasoSecuenciaModel, busca los registros ya
 * cargados con un p:autoComplete (completarExamenes / onItemSelect) y ofrece el
 * combo de exámenes del catálogo (ExamenDAO) para el formulario.
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ProcedimientoPasoExamenModel extends AbstracdetallecrudModel<ProcedimientoPasoExamen, ProcedimientoPaso> {

    @Inject
    private ProcedimientoPasoExamenDAO ppeDAO;

    @Inject
    private ExamenDAO eDAO;

    private List<Examen> examenes;

    // Igual que en ProcedimientoPasoSecuenciaModel: el p:autoComplete reemplaza
    // a la tabla siempre visible (límite de 3 tablas por pantalla). Guarda el
    // objeto elegido de las sugerencias; onItemSelect reutiliza el método
    // heredado que ya sabe cargar el formulario a partir del id.
    private ProcedimientoPasoExamen seleccionAutocomplete;

    public ProcedimientoPasoExamen getSeleccionAutocomplete() {
        return seleccionAutocomplete;
    }

    public void setSeleccionAutocomplete(ProcedimientoPasoExamen seleccionAutocomplete) {
        this.seleccionAutocomplete = seleccionAutocomplete;
    }

    // Listener del evento itemSelect: se toma el objeto directamente de
    // event.getObject() (no de this.seleccionAutocomplete) porque el value
    // del componente no siempre queda actualizado a tiempo cuando corre esto.
    public void onItemSelect(SelectEvent<ProcedimientoPasoExamen> event) {
        this.seleccionAutocomplete = event.getObject();
        if (seleccionAutocomplete != null) {
            btnSeleccionarRegistro(seleccionAutocomplete.getIdProcedimientoPasoExamen());
        }
    }

    // Respaldo manual: botón "Seleccionar" junto al autocomplete, para cuando
    // el clic en la sugerencia no dispara el itemSelect (ej. Enter en vez de clic).
    public void btnSeleccionarHandler() {
        if (seleccionAutocomplete != null) {
            btnSeleccionarRegistro(seleccionAutocomplete.getIdProcedimientoPasoExamen());
        }
    }

    // completeMethod del p:autoComplete: filtra SOLO los exámenes ya cargados
    // de este paso (getregistros(), no toda la tabla) por el mismo texto que
    // se le muestra al usuario (nombre del examen + estado).
    public List<ProcedimientoPasoExamen> completarExamenes(String query) {
        return filtrar(getregistros(), query, this::etiqueta);
    }

    // Mismo texto que arma el itemLabel del autocomplete, para poder filtrar por
    // él.
    public String etiqueta(ProcedimientoPasoExamen e) {
        if (e == null) {
            return "";
        }
        String nombreExamen = (e.getIdExamen() != null && e.getIdExamen().getNombre() != null)
                ? e.getIdExamen().getNombre()
                : "(sin examen)";
        String textoEstado = Boolean.TRUE.equals(e.getActivo()) ? "ACTIVO" : "INACTIVO";
        return nombreExamen + " — " + textoEstado;
    }

    // Se llama desde el botón "Gestionar Examen del Procedimiento": recarga
    // siempre la lista de este paso antes de abrir el diálogo, para no
    // depender de que ya se haya disparado el tabChange de tabsPaso.
    public void abrirGestionExamen(ProcedimientoPaso padre) {
        cargarDe(padre);
        btnAbrirDialogo();
    }

    // ---- Panel "Gestionar Examen" (lista de exámenes del paso + botón Nuevo) ----
    // Lista que muestra el p:dataTable del panel. Solo va a la base de datos
    // cuando el paso es distinto del que ya estaba cargado, así el panel se
    // llena solo al seleccionar un paso sin depender de ningún evento extra.
    public List<ProcedimientoPasoExamen> examenesDe(ProcedimientoPaso paso) {
        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            return Collections.emptyList();
        }
        boolean otroPaso = padreActual == null
                || !paso.getIdProcedimientoPaso().equals(padreActual.getIdProcedimientoPaso());
        if (otroPaso) {
            cargarDe(paso);
        }
        return getregistros();
    }

    // Botón "Nuevo examen" del panel: carga los exámenes del paso, prepara un
    // registro nuevo (queda en estado CREAR) y abre el diálogo ya con el
    // formulario visible.
    public void abrirNuevoExamen(ProcedimientoPaso paso) {
        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            mensaje(FacesMessage.SEVERITY_WARN, "Seleccione un paso",
                    "Guarde o seleccione un paso antes de agregarle exámenes");
            return;
        }
        cargarDe(paso);
        btnNuevoHandler(null);
        btnAbrirDialogo();
    }

    // Click en una fila del panel: selecciona el examen (estado MODIFICAR) y
    // abre el diálogo con el formulario cargado.
    public void onRowSelectAbrir(SelectEvent<ProcedimientoPasoExamen> event) {
        onRowSelect(event);
        btnAbrirDialogo();
    }

    // AbstracdetallecrudModel llama a este hook tras crear, modificar o
    // eliminar con éxito: se refresca la lista del panel y se cierra el diálogo.
    @Override
    protected void recargarLista() {
        super.recargarLista();
        this.mostrarDialogo = false;
    }

    @Override
    protected boolean validarAntesDeGuardar() {
        return requerir(registro.getIdExamen(), "Seleccione un examen", "El examen es obligatorio")
                && requerirActivo(registro.getIdExamen().getActivo(), "Examen inactivo",
                        "No se puede asignar un examen inactivo a un paso");
    }

    // Al cambiar de paso padre o cerrar el diálogo, limpiar lo que haya
    // quedado escrito/elegido en el autocomplete.
    @Override
    public void cargarDe(ProcedimientoPaso padre) {
        super.cargarDe(padre);
        this.seleccionAutocomplete = null;
    }

    @Override
    protected InterfaceDAO<ProcedimientoPasoExamen> getDAO() {
        return ppeDAO;
    }

    @Override
    protected ProcedimientoPasoExamen crearRegistroNuevo() {
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        r.setFechaCreacion(new Date());
        r.setActivo(Boolean.TRUE);
        return r;
    }

    @Override
    protected UUID obtenerId(ProcedimientoPasoExamen registro) {
        return registro.getIdProcedimientoPasoExamen();
    }

    // ---- Los 3 métodos que pide AbstracdetallecrudModel ----
    @Override
    protected List<ProcedimientoPasoExamen> buscarPorPadre(UUID idPadre) {
        return ppeDAO.findByProcedimientoPaso(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(ProcedimientoPaso padre) {
        return padre.getIdProcedimientoPaso();
    }

    @Override
    protected void asignarPadre(ProcedimientoPasoExamen hijo, ProcedimientoPaso padre) {
        hijo.setIdProcedimientoPaso(padre);
    }

    // Opciones del selector de examen (se cargan una vez por vista)
    public List<Examen> getExamenes() {
        if (examenes == null) {
            examenes = eDAO.findRange(0, 100);
        }
        return examenes;
    }

    public List<Examen> completarExamenesFormulario(String query) {
        return filtrarActivos(getExamenes(), query, Examen::getNombre, Examen::getActivo);
    }
}
