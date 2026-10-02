package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Managed bean de la pestaña "Pasos" dentro de Procedimiento.xhtml: CRUD de los
 * ProcedimientoPaso de UN procedimiento (el padre lo entrega ProcedimientoModel
 * llamando a cargarDe).
 *
 * Hereda de AbstracdetallecrudModel: de ahí sale cargar solo los hijos del
 * padre,
 * asignar el padre a cada paso nuevo y volver a filtrar tras guardar o
 * eliminar.
 * Aquí se indica cómo buscar por padre (ProcedimientoPasoDAO), cómo nace un
 * paso
 * nuevo y se ofrece el autocompletado de Rol (RolDAO).
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ProcedimientoPasoModel extends AbstracdetallecrudModel<ProcedimientoPaso, Procedimiento> {

    // Inyectamos el DAO, que es quien guarda, busca y elimina
    // registros de ProcedimientoPaso en la base de datos.
    @Inject
    private ProcedimientoPasoDAO pdDAO;

    // Inyectamos el DAO de Procedimiento por que necesitamos el UUId
    @Inject
    private RolDAO rolDAO;

    // Guarda/lee la relación "este paso depende de otro paso".
    @Inject
    private ProcedimientoPasoSecuenciaDAO secDAO;

    // Valor que se guarda en tipo_secuencia (máx. 20 caracteres) para las
    // dependencias creadas desde el campo "Depende de".
    private static final String TIPO_DEPENDENCIA = "DEPENDE";

    private List<Rol> roles;

    // Raíz del árbol que muestra la p:treeTable (pasos sin dependencia arriba,
    // sus dependientes como hijos).
    private TreeNode<ProcedimientoPaso> raiz = new DefaultTreeNode<>(null, null);
    private TreeNode<ProcedimientoPaso> nodoSeleccionado;

    // id del paso -> nombre del paso del que depende (para la columna "Depende de").
    private Map<UUID, String> nombreDependencias = new HashMap<>();

    // Paso del que depende el paso que se está editando (null = paso inicial).
    // No tiene campo en pantalla: al crear un paso se asigna solo (el último
    // paso del procedimiento) y al editar se conserva el que ya tenía.
    private ProcedimientoPaso dependeDe;

    // Datos capturados en validarAntesDeGuardar para sincronizar la secuencia
    // cuando el guardado ya tuvo éxito (recargarLista).
    private ProcedimientoPaso pasoPorSincronizar;
    private ProcedimientoPaso dependenciaPorSincronizar;

    // Le decimos a la clase padre qué DAO debe usar para las operaciones del CRUD.
    @Override
    protected InterfaceDAO<ProcedimientoPaso> getDAO() {
        return pdDAO;
    }

    // ---- Los 3 métodos que pide AbstracDetalleCrudModel ----
    @Override
    protected List<ProcedimientoPaso> buscarPorPadre(UUID idPadre) {
        return pdDAO.findByProcedimiento(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(Procedimiento padre) {
        return padre.getIdProcedimiento();
    }

    @Override
    protected void asignarPadre(ProcedimientoPaso hijo, Procedimiento padre) {
        hijo.setIdProcedimiento(padre);
    }

        public List<Rol> completarRoles(String query) {
        if (roles == null) {
            roles = rolDAO.findRange(0, 100);
        }
        return filtrarActivos(roles, query, Rol::getNombre, Rol::getActivo);
    }

    // Se ejecuta al pulsar "Nuevo": crea un registro vacío con un UUID generado
    // automáticamente y con "Activo" marcado por defecto. La clase padre lo
    // guarda en "registro" y el formulario lo muestra.
    @Override
    protected ProcedimientoPaso crearRegistroNuevo() {
        ProcedimientoPaso p = new ProcedimientoPaso(UUID.randomUUID());
        p.setIndicaFin(Boolean.FALSE);
        return p;
    }

    // Devuelve el UUID (llave primaria) del registro. La clase padre lo usa para
    // identificar cada fila de la tabla, seleccionar una y eliminarla.
    @Override
    protected UUID obtenerId(ProcedimientoPaso registro) {
        return registro.getIdProcedimientoPaso();
    }


    // =====================================================================
    // "Depende de" + árbol de pasos
    // =====================================================================
    public TreeNode<ProcedimientoPaso> getRaiz() {
        return raiz;
    }

    public TreeNode<ProcedimientoPaso> getNodoSeleccionado() {
        return nodoSeleccionado;
    }

    public void setNodoSeleccionado(TreeNode<ProcedimientoPaso> nodoSeleccionado) {
        this.nodoSeleccionado = nodoSeleccionado;
    }

    // Texto de la columna "Depende de" del árbol.
    public String textoDependeDe(ProcedimientoPaso p) {
        if (p == null) {
            return "";
        }
        return nombreDependencias.getOrDefault(p.getIdProcedimientoPaso(), "—");
    }

    // Texto de solo lectura del formulario: de qué paso depende el paso actual.
    public String getTextoDependeDe() {
        return dependeDe == null ? "Ninguno (paso inicial)" : dependeDe.getNombre();
    }

    // Click en una fila del árbol: equivale al rowSelect de la tabla anterior.
    public void onNodeSelect(NodeSelectEvent event) {
        Object dato = event.getTreeNode().getData();
        if (dato instanceof ProcedimientoPaso) {
            this.registro = (ProcedimientoPaso) dato;
            this.estado = Estado_Crud.MODIFICAR;
            this.dependeDe = buscarDependencia(this.registro.getIdProcedimientoPaso());
        }
    }

    @Override
    public void cargarDe(Procedimiento padre) {
        super.cargarDe(padre);
        this.dependeDe = null;
        construirArbol();
    }

    @Override
    public void btnNuevoHandler(jakarta.faces.event.ActionEvent ae) {
        super.btnNuevoHandler(ae);
        // El paso nuevo depende automáticamente del paso anterior (el último
        // del árbol); si es el primero del procedimiento queda sin dependencia.
        this.dependeDe = ultimoPaso();
    }

    // Último paso en el orden en que se muestra el árbol (recorrido en
    // profundidad). En una cadena de pasos es el paso más reciente.
    private ProcedimientoPaso ultimoPaso() {
        List<ProcedimientoPaso> orden = new ArrayList<>();
        recorrer(raiz, orden);
        return orden.isEmpty() ? null : orden.get(orden.size() - 1);
    }

    private void recorrer(TreeNode<ProcedimientoPaso> nodo, List<ProcedimientoPaso> orden) {
        for (TreeNode<ProcedimientoPaso> hijo : nodo.getChildren()) {
            orden.add(hijo.getData());
            recorrer(hijo, orden);
        }
    }

    @Override
    public void btnCancelar() {
        super.btnCancelar();
        this.dependeDe = null;
        this.nodoSeleccionado = null;
    }

    // paso -> paso del que depende, según las secuencias del procedimiento.
    private Map<UUID, UUID> mapaDependencias() {
        Map<UUID, UUID> mapa = new HashMap<>();
        if (padreActual == null || padreActual.getIdProcedimiento() == null) {
            return mapa;
        }
        for (ProcedimientoPasoSecuencia s : secDAO.findByProcedimiento(padreActual.getIdProcedimiento())) {
            if (s.getIdProcedimientoPasoReferencia() != null && s.getIdProcedimientoPaso() != null) {
                mapa.putIfAbsent(s.getIdProcedimientoPaso().getIdProcedimientoPaso(),
                        s.getIdProcedimientoPasoReferencia());
            }
        }
        return mapa;
    }

    private ProcedimientoPaso buscarDependencia(UUID idPaso) {
        UUID idRef = mapaDependencias().get(idPaso);
        if (idRef == null) {
            return null;
        }
        List<ProcedimientoPaso> lista = getregistros();
        return lista == null ? null : lista.stream()
                .filter(p -> idRef.equals(p.getIdProcedimientoPaso()))
                .findFirst().orElse(null);
    }

    private void construirArbol() {
        raiz = new DefaultTreeNode<>(null, null);
        nodoSeleccionado = null;
        nombreDependencias = new HashMap<>();
        List<ProcedimientoPaso> lista = getregistros();
        if (lista == null || lista.isEmpty()) {
            return;
        }
        Map<UUID, UUID> dep = mapaDependencias();
        Set<UUID> ids = new HashSet<>();
        lista.forEach(p -> ids.add(p.getIdProcedimientoPaso()));
        Map<UUID, String> nombres = new HashMap<>();
        lista.forEach(p -> nombres.put(p.getIdProcedimientoPaso(), p.getNombre()));
        nombreDependencias = new HashMap<>();
        dep.forEach((paso, ref) -> {
            if (nombres.containsKey(ref)) {
                nombreDependencias.put(paso, nombres.get(ref));
            }
        });
        Map<UUID, List<ProcedimientoPaso>> hijos = new HashMap<>();
        List<ProcedimientoPaso> raices = new ArrayList<>();
        for (ProcedimientoPaso p : lista) {
            UUID ref = dep.get(p.getIdProcedimientoPaso());
            if (ref == null || !ids.contains(ref)) {
                raices.add(p);
            } else {
                hijos.computeIfAbsent(ref, k -> new ArrayList<>()).add(p);
            }
        }
        Set<UUID> visitados = new HashSet<>();
        for (ProcedimientoPaso r : raices) {
            agregarNodo(raiz, r, hijos, visitados);
        }
        // Respaldo por si hubiera datos viejos con ciclos: que ningún paso desaparezca.
        for (ProcedimientoPaso p : lista) {
            if (!visitados.contains(p.getIdProcedimientoPaso())) {
                agregarNodo(raiz, p, hijos, visitados);
            }
        }
    }

    private void agregarNodo(TreeNode<ProcedimientoPaso> padre, ProcedimientoPaso p,
            Map<UUID, List<ProcedimientoPaso>> hijos, Set<UUID> visitados) {
        if (!visitados.add(p.getIdProcedimientoPaso())) {
            return;
        }
        TreeNode<ProcedimientoPaso> nodo = new DefaultTreeNode<>(p, padre);
        nodo.setExpanded(true);
        for (ProcedimientoPaso h : hijos.getOrDefault(p.getIdProcedimientoPaso(), new ArrayList<>())) {
            agregarNodo(nodo, h, hijos, visitados);
        }
    }

    // ---- Validar antes de guardar: no depender de sí mismo ni formar ciclos ----
    @Override
    protected boolean validarAntesDeGuardar() {
        if (estado == Estado_Crud.CREAR) {
    if (!requerir(registro.getIdRol(), "Seleccione un rol", "El rol es obligatorio")
            || !requerirActivo(registro.getIdRol().getActivo(), "Rol inactivo",
                    "No se puede asignar un rol inactivo a un paso")) {
        return false;
    }
}
        if (dependeDe != null) {
            UUID propio = registro.getIdProcedimientoPaso();
            Map<UUID, UUID> dep = mapaDependencias();
            UUID actual = dependeDe.getIdProcedimientoPaso();
            Set<UUID> vistos = new HashSet<>();
            while (actual != null && vistos.add(actual)) {
                if (actual.equals(propio)) {
                    mensaje(FacesMessage.SEVERITY_ERROR, "Dependencia inválida",
                            "Ese paso depende (directa o indirectamente) de este mismo paso");
                    return false;
                }
                actual = dep.get(actual);
            }
        }
        pasoPorSincronizar = registro;
        dependenciaPorSincronizar = dependeDe;
        return true;
    }

    // Se ejecuta tras guardar/eliminar con éxito: primero deja la secuencia en
    // sintonía con "Depende de" y luego recarga lista y árbol.
    @Override
    protected void recargarLista() {
        if (pasoPorSincronizar != null) {
            ProcedimientoPaso paso = pasoPorSincronizar;
            ProcedimientoPaso dep = dependenciaPorSincronizar;
            pasoPorSincronizar = null;
            dependenciaPorSincronizar = null;
            sincronizarSecuencia(paso, dep);
        }
        super.recargarLista();
    }

    private void sincronizarSecuencia(ProcedimientoPaso paso, ProcedimientoPaso dep) {
        List<ProcedimientoPasoSecuencia> actuales = secDAO.findByProcedimientoPaso(paso.getIdProcedimientoPaso());
        if (dep == null) {
            for (ProcedimientoPasoSecuencia s : actuales) {
                secDAO.eliminar(s.getIdProcedimientoPasoSecuencia());
            }
            return;
        }
        if (actuales.isEmpty()) {
            ProcedimientoPasoSecuencia s = new ProcedimientoPasoSecuencia(UUID.randomUUID());
            s.setIdProcedimientoPaso(paso);
            s.setTipoSecuencia(TIPO_DEPENDENCIA);
            s.setIdProcedimientoPasoReferencia(dep.getIdProcedimientoPaso());
            secDAO.crear(s);
            return;
        }
        ProcedimientoPasoSecuencia s = actuales.get(0);
        s.setIdProcedimientoPasoReferencia(dep.getIdProcedimientoPaso());
        if (s.getTipoSecuencia() == null || s.getTipoSecuencia().isBlank()) {
            s.setTipoSecuencia(TIPO_DEPENDENCIA);
        }
        secDAO.actualizar(s);
        for (int i = 1; i < actuales.size(); i++) {
            secDAO.eliminar(actuales.get(i).getIdProcedimientoPasoSecuencia());
        }
    }

    // No se puede borrar un paso del que otros pasos dependen.
    @Override
    protected boolean tieneRegistrosDependientes(ProcedimientoPaso paso) {
        return mapaDependencias().containsValue(paso.getIdProcedimientoPaso());
    }

    // Antes de borrar el paso hay que borrar su propia fila de secuencia (FK).
    @Override
    public void btnEliminarHandler(UUID id) {
        ProcedimientoPaso objetivo = id == null ? null : getRowData(id.toString());
        if (objetivo != null && !tieneRegistrosDependientes(objetivo)) {
            for (ProcedimientoPasoSecuencia s : secDAO.findByProcedimientoPaso(id)) {
                secDAO.eliminar(s.getIdProcedimientoPasoSecuencia());
            }
        }
        super.btnEliminarHandler(id);
    }

}