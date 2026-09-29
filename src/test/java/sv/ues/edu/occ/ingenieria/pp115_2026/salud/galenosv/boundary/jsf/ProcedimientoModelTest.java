package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 **
 * Prueba de ProcedimientoModel (CRUD padre con pestañas). El flujo estándar de
 * AbstracCrudModel ya está cubierto por RolModelTest; aquí solo se prueba lo
 * propio: el registro nuevo (UUID + activo), y el reseteo de la pestaña de
 * Pasos (hijo) al crear, cancelar o cambiar de procedimiento.
 */
@ExtendWith(MockitoExtension.class)
public class ProcedimientoModelTest {
 
    @Mock
    private ProcedimientoDAO pdDAO;
    @Mock
    private ProcedimientoPasoModel procedimientoPasoModel;
    @Mock
    private FacesContext fc;
 
    @InjectMocks
    private ProcedimientoModel bean;
 
    // ---- getDAO() / obtenerId() ----
 
    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(pdDAO, bean.getDAO());
    }
 
    @Test
    public void obtenerId_devuelveElIdDelProcedimiento() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, bean.obtenerId(new Procedimiento(id)));
    }
 
    // ---- crearRegistroNuevoBase() ----
 
    @Test
    public void crearRegistroNuevoBase_creaProcedimientoActivoConIdGenerado() {
        Procedimiento nuevo = bean.crearRegistroNuevoBase();
 
        assertNotNull(nuevo.getIdProcedimiento());
        assertEquals(Boolean.TRUE, nuevo.getActivo());
    }
 
    @Test
    public void crearRegistroNuevoBase_generaUnIdDistintoEnCadaLlamada() {
        Procedimiento a = bean.crearRegistroNuevoBase();
        Procedimiento b = bean.crearRegistroNuevoBase();
 
        assertNotEquals(a.getIdProcedimiento(), b.getIdProcedimiento());
    }
 
    // ---- inicializar() ----
 
    @Test
    public void inicializar_cargaLosPrimeros100Procedimientos() {
        List<Procedimiento> esperado = Arrays.asList(
                new Procedimiento(UUID.randomUUID()), new Procedimiento(UUID.randomUUID()));
        when(pdDAO.findRange(0, 100)).thenReturn(esperado);
 
        bean.inicializar();
 
        assertEquals(esperado, bean.getregistros());
    }
 
    // ---- btnNuevoHandler() / resetearHijos() ----
 
    @Test
    public void btnNuevoHandler_creaRegistroActivoYPasaAEstadoCrear() {
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertNotNull(bean.getRegistro().getIdProcedimiento());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }
 
    @Test
    public void btnNuevoHandler_limpiaLaPestanaDePasos() {
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        verify(procedimientoPasoModel).cargarDe(null);
    }
 
    @Test
    public void btnNuevoHandler_regresaALaPrimeraPestana() {
        bean.setActivo(2);
        TabView tabView = mock(TabView.class);
        bean.setTabView(tabView);
 
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertEquals(0, bean.getActivo());
        verify(tabView).setActiveIndex(0);
    }
 
    // ---- btnCancelar() ----
 
    @Test
    public void btnCancelar_limpiaSeleccionYPestanaDePasos() {
        bean.setRegistro(new Procedimiento(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);
        bean.setActivo(1);
 
        bean.btnCancelar();
 
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(0, bean.getActivo());
        verify(procedimientoPasoModel).cargarDe(null);
    }
 
    // ---- onRowSelect() ----
 
    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_seleccionaElProcedimientoYVuelveALaPrimeraPestana() {
        Procedimiento elegido = new Procedimiento(UUID.randomUUID());
        SelectEvent<Procedimiento> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(elegido);
        bean.setActivo(2);
 
        bean.onRowSelect(event);
 
        assertSame(elegido, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
        assertEquals(0, bean.getActivo());
    }
}
 