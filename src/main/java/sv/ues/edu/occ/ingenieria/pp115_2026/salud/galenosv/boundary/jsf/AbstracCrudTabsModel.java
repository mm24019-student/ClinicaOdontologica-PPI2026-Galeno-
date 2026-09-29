package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import org.primefaces.component.tabview.TabView;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;

/**
 * Superclase para los Model de una pantalla PADRE que tiene pestañas
 * (p:tabView) con
 * beans hijos (ej. ConsultaModel, ProcedimientoModel).
 *
 * Hereda todo el CRUD de AbstracCrudModel y le suma: recordar qué pestaña está
 * activa, volver a la pestaña 0 al pulsar Nuevo/Cancelar/seleccionar fila y
 * limpiar los beans hijos (hook resetearHijos). Cada subclase debe implementar
 * crearRegistroNuevoBase() en lugar de crearRegistroNuevo().
 *
 * @author antonio
 */
public abstract class AbstracCrudTabsModel<T> extends AbstracCrudModel<T> {

    private static final long serialVersionUID = 1L;

    // Ligar con binding="#{miModelo.tabView}" en el p:tabView de la vista.
    protected TabView tabView;

    // Índice de la pestaña activa (0 = la primera). La vista lo lee con
    // getActivo().
    protected int activeTabIndex = 0;

    // Getters y setters para ligar el p:tabView de la vista con este bean.
    public TabView getTabView() {
        return tabView;
    }

    public void setTabView(TabView tabView) {
        this.tabView = tabView;
    }

    public int getActivo() {
        return activeTabIndex;
    }

    public void setActivo(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    /**
     * Hook: cada subclase resetea aquí el/los bean(s) hijo(s) inyectados
     * (típicamente llamando a hijoModel.cargarDe(null)). Por defecto no hace
     * nada, por si algún padre con tabs no tuviera hijos que limpiar.
     */
    protected void resetearHijos() {
    }

    /**
     * Fuerza tanto el bean como el componente real a la pestaña 0.
     */
    protected void resetTab() {
        activeTabIndex = 0;
        if (tabView != null) {
            tabView.setActiveIndex(0);
        }
    }

    // Listener del cambio de pestaña. Aquí no hace nada; las subclases con hijos
    // (ej. ConsultaModel) lo sobrescriben para cargar el hijo de la pestaña
    // activada.
    public void onTabChange(TabChangeEvent event) {
    }

    // ---- Ya no se implementa crearRegistroNuevo() directamente: cada
    // subclase implementa crearRegistroNuevoBase(), y este método hace
    // el reset de tab + hijos alrededor, siempre en el mismo orden. ----
    @Override
    protected final T crearRegistroNuevo() {
        T nuevo = crearRegistroNuevoBase();
        resetTab();
        resetearHijos();
        return nuevo;
    }

    protected abstract T crearRegistroNuevoBase();

    // Además de cancelar la selección (clase base), vuelve a la pestaña 0 y limpia
    // los hijos.
    @Override
    public void btnCancelar() {
        super.btnCancelar();
        resetTab();
        resetearHijos();
    }

    // Además de seleccionar la fila (clase base), vuelve a la pestaña 0.
    @Override
    public void onRowSelect(SelectEvent<T> event) {
        super.onRowSelect(event);
        resetTab();
    }

}
