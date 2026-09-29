package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba directa de AbstracCrudTabsModel con una subclase mínima que registra
 * en qué orden se invocan crearRegistroNuevoBase(), resetTab() y
 * resetearHijos().
 *
 * @author oscar
 */
public class AbstracCrudTabsModelTest {

    static class Item {

        final UUID id = UUID.randomUUID();
    }

    static class TabsModel extends AbstracCrudTabsModel<Item> {

        private static final long serialVersionUID = 1L;

        final transient List<String> eventos = new ArrayList<>();

        @Override
        protected InterfaceDAO<Item> getDAO() {
            return null; // no se usa en estos tests
        }

        @Override
        protected Item crearRegistroNuevoBase() {
            eventos.add("base");
            return new Item();
        }

        @Override
        protected UUID obtenerId(Item registro) {
            return registro.id;
        }

        @Override
        protected void resetearHijos() {
            eventos.add("hijos");
        }

        @Override
        protected void resetTab() {
            eventos.add("tab");
            super.resetTab();
        }
    }

    private TabsModel bean;

    @BeforeEach
    public void setUp() {
        bean = new TabsModel();
    }

    // ---- getters / setters ----

    @Test
    public void activo_empiezaEnCeroYSePuedeCambiar() {
        assertEquals(0, bean.getActivo());

        bean.setActivo(2);

        assertEquals(2, bean.getActivo());
    }

    @Test
    public void tabView_guardaLaReferenciaVinculada() {
        assertNull(bean.getTabView());
        TabView tabView = mock(TabView.class);

        bean.setTabView(tabView);

        assertSame(tabView, bean.getTabView());
    }

    // ---- crearRegistroNuevo() (via btnNuevoHandler) ----

    @Test
    public void btnNuevoHandler_creaBaseLuegoResetTabYLuegoHijos() {
        bean.setActivo(2);

        bean.btnNuevoHandler(null);

        assertEquals(Arrays.asList("base", "tab", "hijos"), bean.eventos);
        assertNotNull(bean.getRegistro());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
        assertEquals(0, bean.getActivo());
    }

    // ---- resetTab() ----

    @Test
    public void resetTab_sinTabView_soloReseteaElIndiceDelBean() {
        bean.setActivo(3);

        bean.resetTab();

        assertEquals(0, bean.getActivo());
    }

    @Test
    public void resetTab_conTabView_tambienFuerzaElComponente() {
        TabView tabView = mock(TabView.class);
        bean.setTabView(tabView);
        bean.setActivo(3);

        bean.resetTab();

        assertEquals(0, bean.getActivo());
        verify(tabView).setActiveIndex(0);
    }

    // ---- btnCancelar() ----

    @Test
    public void btnCancelar_limpiaSeleccionReseteaPestanaYHijos() {
        bean.setRegistro(new Item());
        bean.setEstado(Estado_Crud.MODIFICAR);
        bean.setActivo(1);

        bean.btnCancelar();

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(0, bean.getActivo());
        assertEquals(Arrays.asList("tab", "hijos"), bean.eventos);
    }

    // ---- onRowSelect() ----

    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_fijaRegistroYVuelveAPrimeraPestanaSinTocarLosHijos() {
        Item item = new Item();
        SelectEvent<Item> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(item);
        bean.setActivo(2);

        bean.onRowSelect(event);

        assertSame(item, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
        assertEquals(0, bean.getActivo());
        // Los hijos los recarga cada subclase con el registro real; aquí no se resetean.
        assertEquals(Arrays.asList("tab"), bean.eventos);
    }

    // ---- onTabChange() ----

    @Test
    public void onTabChange_esUnHookVacioQueNoReventaNiCambiaElEstado() {
        bean.setActivo(1);

        assertDoesNotThrow(() -> bean.onTabChange(null));

        assertEquals(1, bean.getActivo());
        assertTrue(bean.eventos.isEmpty());
    }
}