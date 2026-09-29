package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ProcedimientoPasoSecuenciaModel extends AbstracdetallecrudModel<ProcedimientoPasoSecuencia, ProcedimientoPaso> {

    @Inject
    private ProcedimientoPasoSecuenciaDAO dao;
    @Inject
    private ProcedimientoPasoDAO pasoDAO;

    private List<ProcedimientoPaso> pasos;

    private ProcedimientoPaso pasoReferenciaSeleccionado;

    public ProcedimientoPaso getPasoReferenciaSeleccionado() {
        return pasoReferenciaSeleccionado;
    }

    public void setPasoReferenciaSeleccionado(ProcedimientoPaso pasoReferenciaSeleccionado) {
        this.pasoReferenciaSeleccionado = pasoReferenciaSeleccionado;
        // JSF puede llamar a este setter cuando todavía no hay registro
        // (por ejemplo al cerrar el diálogo), y sin este chequeo daría
        // NullPointerException.
        if (this.registro != null) {
            this.registro.setIdProcedimientoPasoReferencia(
                    pasoReferenciaSeleccionado == null
                            ? null
                            : pasoReferenciaSeleccionado.getIdProcedimientoPaso()
            );
        }
    }

    // Usado por el p:autoComplete (reemplaza al p:selectOneListbox/p:dataTable
    // para no pasar del límite de 3 tablas por pantalla). Guarda el objeto que
    // el usuario eligió de las sugerencias; onAutocompleteSelect() reutiliza
    // el método heredado que ya sabe cargar el formulario a partir del id.
    private ProcedimientoPasoSecuencia seleccionAutocomplete;

    public ProcedimientoPasoSecuencia getSeleccionAutocomplete() {
        return seleccionAutocomplete;
    }

    public void setSeleccionAutocomplete(ProcedimientoPasoSecuencia seleccionAutocomplete) {
        this.seleccionAutocomplete = seleccionAutocomplete;
    }

    // Listener del evento itemSelect del p:autoComplete. IMPORTANTE: se toma
    // el objeto directamente de event.getObject() (igual que
    // onRolAutocompleteSelect/onClinicaAutocompleteSelect en PersonaModel) en
    // vez de leer this.seleccionAutocomplete, porque el value del componente
    // no siempre queda actualizado a tiempo cuando corre este listener.
    public void onItemSelect(SelectEvent<ProcedimientoPasoSecuencia> event) {
        this.seleccionAutocomplete = event.getObject();
        if (seleccionAutocomplete != null) {
            btnSeleccionarRegistro(seleccionAutocomplete.getIdProcedimientoPasoSecuencia());
        }
    }

    // Respaldo manual: botón "Seleccionar" junto al autocomplete. Aquí sí se
    // puede confiar en this.seleccionAutocomplete porque el propio commandButton
    // ya forzó la actualización normal del value antes de invocar este método.
    public void btnSeleccionarHandler() {
        if (seleccionAutocomplete != null) {
            btnSeleccionarRegistro(seleccionAutocomplete.getIdProcedimientoPasoSecuencia());
        }
    }

    // completeMethod del p:autoComplete: filtra SOLO las secuencias ya
    // cargadas de este paso (getregistros(), no toda la tabla) por el mismo
    // texto que se le muestra al usuario (tipo + nombre del paso de referencia).
    public List<ProcedimientoPasoSecuencia> completarSecuencias(String query) {
        return filtrar(getregistros(), query, this::etiqueta);
    }

    // Mismo texto que arma el itemLabel del autocomplete, para poder filtrar por él.
    // Público porque también se usa directamente como itemLabel en el XHTML.
    // OJO: el guard de null es SOLO para s==null (el valor todavía no elegido en
    // el campo; si se dejara el "—" escrito directo en el EL, PrimeFaces lo evalúa
    // igual con var=null y queda un guion suelto). Un registro real con
    // tipoSecuencia vacío (como los que insertaste a mano en la BD) NO debe dar
    // cadena vacía, porque entonces ninguna búsqueda de texto lo encuentra nunca:
    // se muestra como "(sin tipo)" para que siga siendo visible/buscable.
    public String etiqueta(ProcedimientoPasoSecuencia s) {
        if (s == null) {
            return "";
        }
        String tipo = (s.getTipoSecuencia() == null || s.getTipoSecuencia().isBlank())
                ? "(sin tipo)" : s.getTipoSecuencia();
        String refNombre = nombrePaso(s.getIdProcedimientoPasoReferencia());
        return refNombre.isBlank() ? tipo : tipo + " — " + refNombre;
    }

    // Al seleccionar una secuencia existente (desde el autocomplete de
    // búsqueda), sincroniza también el objeto que ve el autocomplete
    // "Paso de referencia": registro solo guarda el UUID
    // (idProcedimientoPasoReferencia), pero el <p:autoComplete> necesita el
    // objeto ProcedimientoPaso completo para poder mostrarlo.
    @Override
    public void btnSeleccionarRegistro(UUID id) {
        super.btnSeleccionarRegistro(id);
        sincronizarPasoReferencia();
    }

    private void sincronizarPasoReferencia() {
        if (registro != null && registro.getIdProcedimientoPasoReferencia() != null) {
            UUID idRef = registro.getIdProcedimientoPasoReferencia();
            this.pasoReferenciaSeleccionado = getPasos().stream()
                    .filter(p -> idRef.equals(p.getIdProcedimientoPaso()))
                    .findFirst()
                    .orElseGet(() -> pasoDAO.buscar(idRef));
        } else {
            this.pasoReferenciaSeleccionado = null;
        }
    }

    // Se llama desde el botón "Gestionar Secuencia": antes solo abría el diálogo
    // (btnAbrirDialogo) confiando en que cargarDe() ya se hubiera ejecutado por el
    // tabChange de tabsPaso. Si el usuario no alternaba de pestaña, cargarDe nunca
    // corría para el paso actual y el diálogo se abría con la lista vacía hasta
    // cambiar de pestaña y volver. Ahora se recarga aquí mismo, siempre, al abrir.
    public void abrirGestionSecuencia(ProcedimientoPaso padre) {
        cargarDe(padre);
        btnAbrirDialogo();
    }

    // Al cambiar de paso padre o cerrar el diálogo, limpiar también lo que
    // haya quedado escrito/elegido en el autocomplete.
    @Override
    public void cargarDe(ProcedimientoPaso padre) {
        super.cargarDe(padre);
        this.seleccionAutocomplete = null;
        this.pasoReferenciaSeleccionado = null;
    }

    @Override
    public void btnCerrarDialogo() {
        super.btnCerrarDialogo();
        this.seleccionAutocomplete = null;
        this.pasoReferenciaSeleccionado = null;
    }

    @Override
    public void btnNuevoHandler(jakarta.faces.event.ActionEvent ae) {
        super.btnNuevoHandler(ae);
        this.pasoReferenciaSeleccionado = null;
    }

    @Override
    protected InterfaceDAO<ProcedimientoPasoSecuencia> getDAO() {
        return dao;
    }

    @Override
    protected ProcedimientoPasoSecuencia crearRegistroNuevo() {
        return new ProcedimientoPasoSecuencia(UUID.randomUUID());   // esta tabla no tiene "activo"
    }

    @Override
    protected UUID obtenerId(ProcedimientoPasoSecuencia registro) {
        return registro.getIdProcedimientoPasoSecuencia();
    }

    @Override
    protected List<ProcedimientoPasoSecuencia> buscarPorPadre(UUID idPadre) {
        return dao.findByProcedimientoPaso(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(ProcedimientoPaso padre) {
        return padre.getIdProcedimientoPaso();
    }

    @Override
    protected void asignarPadre(ProcedimientoPasoSecuencia hijo, ProcedimientoPaso padre) {
        hijo.setIdProcedimientoPaso(padre);
    }

    // Lista para los dos combos (se carga solo la primera vez)
    public List<ProcedimientoPaso> getPasos() {
        if (pasos == null) {
            pasos = pasoDAO.findRange(0, 100);
        }
        return pasos;
    }

    public List<ProcedimientoPaso> completarPasos(String query) {
        return filtrar(getPasos(), query, ProcedimientoPaso::getNombre);
    }

    // La referencia es un UUID suelto; este método busca el nombre del paso para mostrarlo en la tabla.
    // Primero busca en la lista cacheada (getPasos(), limitada a 100 para no golpear la BD en cada
    // llamada); si el paso referenciado no está en ese rango (más de 100 pasos en el sistema), se hace
    // una búsqueda puntual por id en vez de conformarse con mostrar el UUID en crudo.
    public String nombrePaso(UUID id) {
        if (id == null) {
            return "";
        }
        String nombreEnCache = getPasos().stream()
                .filter(p -> id.equals(p.getIdProcedimientoPaso()))
                .map(ProcedimientoPaso::getNombre)
                .findFirst()
                .orElse(null);
        if (nombreEnCache != null) {
            return nombreEnCache;
        }
        ProcedimientoPaso encontrado = pasoDAO.buscar(id);
        return encontrado != null ? encontrado.getNombre() : id.toString();
    }
}
