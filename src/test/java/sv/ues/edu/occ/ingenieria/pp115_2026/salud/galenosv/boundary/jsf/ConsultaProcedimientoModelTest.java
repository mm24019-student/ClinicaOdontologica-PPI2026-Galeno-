package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
/**
 * Prueba de ConsultaProcedimientoModel (detalle de Consulta). Aquí solo se
 * prueba lo propio: la referencia al Procedimiento (UUID suelto en la
 * entidad, objeto en el autocomplete), onRowSelect() que reconstruye ese
 * objeto, registro nuevo con fecha de inicio y nombreProcedimiento().
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoModelTest {
 
    @Mock
    private ConsultaProcedimientoDAO cpDAO;
    @Mock
    private ProcedimientoDAO procedimientoDAO;
    @Mock
    private FacesContext fc;
 
    @InjectMocks
    private ConsultaProcedimientoModel bean;
 
    private Consulta consulta;
 
    @BeforeEach
    public void setUp() {
        consulta = new Consulta(UUID.randomUUID());
    }
 
    private Procedimiento nuevoProcedimiento(String nombre) {
        Procedimiento p = new Procedimiento(UUID.randomUUID());
        p.setNombre(nombre);
        return p;
    }
 
    @SuppressWarnings("unchecked")
    private SelectEvent<ConsultaProcedimiento> eventoCon(ConsultaProcedimiento cp) {
        SelectEvent<ConsultaProcedimiento> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(cp);
        return event;
    }
 
    // ---- getDAO() / obtenerId() / los 3 métodos del detalle ----
 
    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(cpDAO, bean.getDAO());
    }
 
    @Test
    public void obtenerId_devuelveElIdDelConsultaProcedimiento() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, bean.obtenerId(new ConsultaProcedimiento(id)));
    }
 
    @Test
    public void obtenerIdPadre_devuelveElIdDeLaConsulta() {
        assertEquals(consulta.getIdConsulta(), bean.obtenerIdPadre(consulta));
    }
 
    @Test
    public void asignarPadre_ligaElProcedimientoALaConsulta() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
 
        bean.asignarPadre(cp, consulta);
 
        assertSame(consulta, cp.getIdConsulta());
    }
 
    @Test
    public void buscarPorPadre_delegaEnFindByConsulta() {
        List<ConsultaProcedimiento> esperado = Arrays.asList(new ConsultaProcedimiento(UUID.randomUUID()));
        when(cpDAO.findByConsulta(consulta.getIdConsulta())).thenReturn(esperado);
 
        assertEquals(esperado, bean.buscarPorPadre(consulta.getIdConsulta()));
    }
 
    // ---- inicializar() / cargarDe() ----
 
    @Test
    public void inicializar_arrancaVacioSinConsultarLaBaseDeDatos() {
        bean.inicializar();
 
        assertTrue(bean.getregistros().isEmpty());
        verifyNoInteractions(cpDAO);
    }
 
    @Test
    public void cargarDe_consultaConId_cargaSoloSusProcedimientos() {
        List<ConsultaProcedimiento> esperado = Arrays.asList(new ConsultaProcedimiento(UUID.randomUUID()));
        when(cpDAO.findByConsulta(consulta.getIdConsulta())).thenReturn(esperado);
 
        bean.cargarDe(consulta);
 
        assertEquals(esperado, bean.getregistros());
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }
 
    @Test
    public void cargarDe_consultaNula_dejaListaVaciaSinConsultar() {
        bean.cargarDe(null);
 
        assertTrue(bean.getregistros().isEmpty());
        verify(cpDAO, never()).findByConsulta(any());
    }
 
    // ---- btnNuevoHandler() / crearRegistroNuevo() / configurarNuevoRegistro() ----
 
    @Test
    public void btnNuevoHandler_creaRegistroConFechaDeInicioYLigadoALaConsultaActual() {
        when(cpDAO.findByConsulta(consulta.getIdConsulta())).thenReturn(Collections.emptyList());
        bean.cargarDe(consulta);
 
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertNotNull(bean.getRegistro().getIdConsultaProcedimiento());
        assertNotNull(bean.getRegistro().getFechaInicio());
        assertSame(consulta, bean.getRegistro().getIdConsulta());
        assertNull(bean.getRegistro().getIdProcedimiento());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }
 
    @Test
    public void btnNuevoHandler_noArrastraElProcedimientoSeleccionadoAntes() {
        bean.setProcedimientoSeleccionado(nuevoProcedimiento("Endodoncia"));
 
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertNull(bean.getProcedimientoSeleccionado());
    }
 
    // ---- btnCrearhandler() + recargarLista() ----
 
    @Test
    public void btnCrearhandler_exito_creaYRecargaSoloLosProcedimientosDeLaConsulta() {
        UUID idConsulta = consulta.getIdConsulta();
        when(cpDAO.findByConsulta(idConsulta)).thenReturn(Collections.emptyList());
        bean.cargarDe(consulta);
        bean.btnNuevoHandler(mock(ActionEvent.class));
        ConsultaProcedimiento nuevo = bean.getRegistro();
 
        bean.btnCrearhandler(mock(ActionEvent.class));
 
        verify(cpDAO).crear(nuevo);
        verify(cpDAO, times(2)).findByConsulta(idConsulta);
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }
 
    // ---- setProcedimientoSeleccionado() ----
 
    @Test
    public void setProcedimientoSeleccionado_conRegistro_guardaElUuidDelProcedimiento() {
        Procedimiento p = nuevoProcedimiento("Endodoncia");
        bean.setRegistro(new ConsultaProcedimiento(UUID.randomUUID()));
 
        bean.setProcedimientoSeleccionado(p);
 
        assertSame(p, bean.getProcedimientoSeleccionado());
        assertEquals(p.getIdProcedimiento(), bean.getRegistro().getIdProcedimiento());
    }
 
    @Test
    public void setProcedimientoSeleccionado_nulo_limpiaElUuidDelProcedimiento() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        cp.setIdProcedimiento(UUID.randomUUID());
        bean.setRegistro(cp);
 
        bean.setProcedimientoSeleccionado(null);
 
        assertNull(bean.getProcedimientoSeleccionado());
        assertNull(cp.getIdProcedimiento());
    }
 
    @Test
    public void setProcedimientoSeleccionado_sinRegistro_noLanzaExcepcion() {
        Procedimiento p = nuevoProcedimiento("Endodoncia");
 
        assertDoesNotThrow(() -> bean.setProcedimientoSeleccionado(p));
 
        assertSame(p, bean.getProcedimientoSeleccionado());
    }
 
    // ---- onRowSelect() ----
 
    @Test
    public void onRowSelect_conProcedimiento_lobuscaPorIdParaMostrarloEnElAutocomplete() {
        Procedimiento p = nuevoProcedimiento("Endodoncia");
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        cp.setIdProcedimiento(p.getIdProcedimiento());
        when(procedimientoDAO.buscar(p.getIdProcedimiento())).thenReturn(p);
 
        bean.onRowSelect(eventoCon(cp));
 
        assertSame(cp, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
        assertSame(p, bean.getProcedimientoSeleccionado());
    }
 
    @Test
    public void onRowSelect_sinProcedimiento_dejaLaSeleccionEnNullSinConsultar() {
        bean.setProcedimientoSeleccionado(nuevoProcedimiento("Anterior"));
 
        bean.onRowSelect(eventoCon(new ConsultaProcedimiento(UUID.randomUUID())));
 
        assertNull(bean.getProcedimientoSeleccionado());
        verifyNoInteractions(procedimientoDAO);
    }
 
    // ---- getProcedimientos() / completarProcedimientos() ----
 
    @Test
    public void getProcedimientos_cacheaElCatalogoEntreLlamadas() {
        when(procedimientoDAO.findRange(0, 100)).thenReturn(Arrays.asList(nuevoProcedimiento("Endodoncia")));
 
        bean.getProcedimientos();
        bean.getProcedimientos();
 
        verify(procedimientoDAO, times(1)).findRange(0, 100);
    }
 
    @Test
    public void completarProcedimientos_filtraPorNombreSinImportarMayusculas() {
        Procedimiento endodoncia = nuevoProcedimiento("Endodoncia");
        Procedimiento limpieza = nuevoProcedimiento("Limpieza dental");
        when(procedimientoDAO.findRange(0, 100)).thenReturn(Arrays.asList(endodoncia, limpieza));
 
        List<Procedimiento> resultado = bean.completarProcedimientos("ENDO");
 
        assertEquals(1, resultado.size());
        assertSame(endodoncia, resultado.get(0));
    }
 
    // ---- nombreProcedimiento() ----
 
    @Test
    public void nombreProcedimiento_idNulo_devuelveCadenaVaciaSinConsultar() {
        assertEquals("", bean.nombreProcedimiento(null));
        verifyNoInteractions(procedimientoDAO);
    }
 
    @Test
    public void nombreProcedimiento_existente_devuelveSuNombre() {
        Procedimiento p = nuevoProcedimiento("Endodoncia");
        when(procedimientoDAO.findRange(0, 100)).thenReturn(Arrays.asList(p));
 
        assertEquals("Endodoncia", bean.nombreProcedimiento(p.getIdProcedimiento()));
    }
 
    @Test
    public void nombreProcedimiento_inexistente_devuelveElUuidEnTexto() {
        UUID id = UUID.randomUUID();
        when(procedimientoDAO.findRange(0, 100)).thenReturn(Collections.emptyList());
 
        assertEquals(id.toString(), bean.nombreProcedimiento(id));
    }
}
 