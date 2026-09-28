package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

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
        List<ProcedimientoPasoExamen> lista = getregistros();
        if (lista == null) {
            return Collections.emptyList();
        }
        String texto = query == null ? "" : query.trim().toLowerCase();
        return lista.stream()
                .filter(e -> etiqueta(e).toLowerCase().contains(texto))
                .collect(Collectors.toList());
    }

    // Mismo texto que arma el itemLabel del autocomplete, para poder filtrar por él.
    public String etiqueta(ProcedimientoPasoExamen e) {
        if (e == null) {
            return "";
        }
        String nombreExamen = (e.getIdExamen() != null && e.getIdExamen().getNombre() != null)
                ? e.getIdExamen().getNombre() : "(sin examen)";
        String estado = Boolean.TRUE.equals(e.getActivo()) ? "ACTIVO" : "INACTIVO";
        return nombreExamen + " — " + estado;
    }

    // Se llama desde el botón "Gestionar Examen del Procedimiento": recarga
    // siempre la lista de este paso antes de abrir el diálogo, para no
    // depender de que ya se haya disparado el tabChange de tabsPaso.
    public void abrirGestionExamen(ProcedimientoPaso padre) {
        cargarDe(padre);
        btnAbrirDialogo();
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

        if (examenes == null) {
            examenes = eDAO.findRange(0, 100);
        }

        String texto = query == null
                ? ""
                : query.trim().toLowerCase();

        return examenes.stream()
                .filter(e -> e.getNombre() != null
                && e.getNombre().toLowerCase().contains(texto))
                .collect(Collectors.toList());
    }
}
