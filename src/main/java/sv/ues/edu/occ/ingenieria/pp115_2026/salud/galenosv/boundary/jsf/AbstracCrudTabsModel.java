package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import org.primefaces.component.tabview.TabView;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author antonio
 */

public abstract class AbstracCrudTabsModel<T> extends AbstracCrudModel<T> {

    private static final long serialVersionUID = 1L;

    // Ligar con binding="#{miModelo.tabView}" en el p:tabView de la vista.
    protected TabView tabView;

    protected int activeTabIndex = 0;

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

    @Override
    public void btnCancelar() {
        super.btnCancelar();
        resetTab();
        resetearHijos();
    }

    @Override
    public void onRowSelect(SelectEvent<T> event) {
        super.onRowSelect(event);
        resetTab();
    }

}
