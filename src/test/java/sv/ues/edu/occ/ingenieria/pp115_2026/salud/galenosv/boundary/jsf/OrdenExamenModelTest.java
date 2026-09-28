package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenResultado;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de OrdenExamenModel (padre con pestañas; su hijo es
 * ExamenResultadoModel): manejo de pestañas heredado de AbstracCrudTabsModel,
 * reseteo del hijo, validación de la consulta, bloqueo de eliminación cuando
 * hay resultados, y el autocomplete de ConsultaProcedimientoPaso.
 */
@ExtendWith(MockitoExtension.class)
public class OrdenExamenModelTest {

    @Mock
    private OrdenExamenDAO ordenExamenDAO;
    @Mock
    private ExamenResultadoDAO examenResultadoDAO;
    @Mock
    private ConsultaProcedimientoPasoDAO consultaProcedimientoPasoDAO;
    @Mock
    private ExamenResultadoModel examenResultadoModel;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private OrdenExamenModel bean;

    private ConsultaProcedimientoPaso paso(String id, String estado) {
        ConsultaProcedimientoPaso p = new ConsultaProcedimientoPaso(UUID.fromString(id));
        p.setEstado(estado);
        return p;
    }

    private OrdenExamen ordenConConsulta() {
        OrdenExamen o = new OrdenExamen(UUID.randomUUID());
        o.setIdConsultaProcedimientoPaso(paso("aaaaaaaa-0000-0000-0000-000000000001", "EN_PROCESO"));
        return o;
    }

    // ---- getDAO() / obtenerId() ----

    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(ordenExamenDAO, bean.getDAO());
    }

    @Test
    public void obtenerId_devuelveElIdDeLaOrden() {
        UUID id = UUID.randomUUID();

        assertEquals(id, bean.obtenerId(new OrdenExamen(id)));
    }

    // ---- btnNuevoHandler() / crearRegistroNuevoBase() ----

    @Test
    public void btnNuevoHandler_creaOrdenConFechaEstadoCrearYReseteaHijoYPestana() {
        bean.setActivo(1);

        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(bean.getRegistro().getIdOrdenExamen());
        assertNotNull(bean.getRegistro().getFechaCreacion());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
        assertEquals(0, bean.getActivo());
        verify(examenResultadoModel).cargarDe(null);
    }

    // ---- btnCancelar() ----

    @Test
    public void btnCancelar_limpiaEstadoReseteaPestanaYHijo() {
        bean.setRegistro(new OrdenExamen(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);
        bean.setActivo(1);

        bean.btnCancelar();

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(0, bean.getActivo());
        verify(examenResultadoModel).cargarDe(null);
    }

    @Test
    public void btnCancelar_conTabViewVinculado_fuerzaElComponenteALaPrimeraPestana() {
        TabView tabView = mock(TabView.class);
        bean.setTabView(tabView);

        bean.btnCancelar();

        verify(tabView).setActiveIndex(0);
    }

    // ---- onRowSelect() / seleccionarOrdenExamen() ----

    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_fijaOrdenEstadoModificarYVuelveALaPrimeraPestana() {
        OrdenExamen orden = new OrdenExamen(UUID.randomUUID());
        SelectEvent<OrdenExamen> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(orden);
        bean.setActivo(1);

        bean.onRowSelect(event);

        assertSame(orden, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
        assertEquals(0, bean.getActivo());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void seleccionarOrdenExamen_cargaLosResultadosDeLaOrden() {
        OrdenExamen orden = new OrdenExamen(UUID.randomUUID());
        SelectEvent<OrdenExamen> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(orden);

        bean.seleccionarOrdenExamen(event);

        verify(examenResultadoModel).cargarDe(orden);
    }

    // ---- btnCrearhandler() / consultaSeleccionada() ----

    @Test
    public void btnCrearhandler_sinConsulta_agregaMensajeDeErrorYNoCrea() {
        bean.setRegistro(new OrdenExamen(UUID.randomUUID()));

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(ordenExamenDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_registroNulo_agregaMensajeDeErrorYNoCrea() {
        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(ordenExamenDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_conConsulta_creaYRecargaLaLista() {
        OrdenExamen orden = ordenConConsulta();
        bean.setRegistro(orden);
        List<OrdenExamen> recargados = Arrays.asList(orden);
        when(ordenExamenDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(ordenExamenDAO).crear(orden);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- btnModificarHandler() / consultaSeleccionada() ----

    @Test
    public void btnModificarHandler_sinConsulta_agregaMensajeDeErrorYNoActualiza() {
        bean.setRegistro(new OrdenExamen(UUID.randomUUID()));

        bean.btnModificarHandler();

        verify(ordenExamenDAO, never()).actualizar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_conConsulta_actualizaYRecargaLaLista() {
        OrdenExamen orden = ordenConConsulta();
        bean.setRegistro(orden);
        List<OrdenExamen> recargados = Arrays.asList(orden);
        when(ordenExamenDAO.actualizar(orden)).thenReturn(orden);
        when(ordenExamenDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnModificarHandler();

        verify(ordenExamenDAO).actualizar(orden);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- tieneRegistrosDependientes() / btnEliminarHandler() ----

    @Test
    public void tieneRegistrosDependientes_conResultados_devuelveTrue() {
        OrdenExamen orden = new OrdenExamen(UUID.randomUUID());
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen()))
                .thenReturn(Arrays.asList(new ExamenResultado(UUID.randomUUID())));

        assertTrue(bean.tieneRegistrosDependientes(orden));
    }

    @Test
    public void tieneRegistrosDependientes_sinResultados_devuelveFalse() {
        OrdenExamen orden = new OrdenExamen(UUID.randomUUID());
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen()))
                .thenReturn(Collections.emptyList());

        assertFalse(bean.tieneRegistrosDependientes(orden));
    }

    @Test
    public void btnEliminarHandler_conResultados_advierteYNoElimina() {
        OrdenExamen orden = new OrdenExamen(UUID.randomUUID());
        bean.setRegistro(orden);
        bean.setWrappedData(Arrays.asList(orden));
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen()))
                .thenReturn(Arrays.asList(new ExamenResultado(UUID.randomUUID())));

        bean.btnEliminarHandler();

        verify(ordenExamenDAO, never()).eliminar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_WARN, captor.getValue().getSeverity());
    }

    @Test
    public void btnEliminarHandler_sinResultados_eliminaYRecargaLaLista() {
        OrdenExamen orden = new OrdenExamen(UUID.randomUUID());
        bean.setRegistro(orden);
        bean.setWrappedData(Arrays.asList(orden));
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen()))
                .thenReturn(Collections.emptyList());
        when(ordenExamenDAO.findRange(0, 100)).thenReturn(Collections.emptyList());

        bean.btnEliminarHandler();

        verify(ordenExamenDAO).eliminar(orden.getIdOrdenExamen());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    // ---- getPasosConsulta() ----

    @Test
    public void getPasosConsulta_cacheaEntreLlamadas() {
        when(consultaProcedimientoPasoDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(paso("aaaaaaaa-0000-0000-0000-000000000001", "EN_PROCESO")));

        bean.getPasosConsulta();
        bean.getPasosConsulta();

        verify(consultaProcedimientoPasoDAO, times(1)).findRange(0, 100);
    }

    // ---- completarPasosConsulta() ----

    @Test
    public void completarPasosConsulta_filtraPorEstadoSinImportarMayusculas() {
        ConsultaProcedimientoPaso enProceso = paso("aaaaaaaa-0000-0000-0000-000000000001", "EN_PROCESO");
        ConsultaProcedimientoPaso completado = paso("bbbbbbbb-0000-0000-0000-000000000002", "COMPLETADO");
        when(consultaProcedimientoPasoDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(enProceso, completado));

        List<ConsultaProcedimientoPaso> resultado = bean.completarPasosConsulta("en_pro");

        assertEquals(1, resultado.size());
        assertSame(enProceso, resultado.get(0));
    }

    @Test
    public void completarPasosConsulta_filtraPorFragmentoDelId() {
        ConsultaProcedimientoPaso enProceso = paso("aaaaaaaa-0000-0000-0000-000000000001", "EN_PROCESO");
        ConsultaProcedimientoPaso completado = paso("bbbbbbbb-0000-0000-0000-000000000002", "COMPLETADO");
        when(consultaProcedimientoPasoDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(enProceso, completado));

        List<ConsultaProcedimientoPaso> resultado = bean.completarPasosConsulta("bbbbbbbb");

        assertEquals(1, resultado.size());
        assertSame(completado, resultado.get(0));
    }

    @Test
    public void completarPasosConsulta_consultaVaciaONula_devuelveTodos() {
        when(consultaProcedimientoPasoDAO.findRange(0, 100)).thenReturn(Arrays.asList(
                paso("aaaaaaaa-0000-0000-0000-000000000001", "EN_PROCESO"),
                paso("bbbbbbbb-0000-0000-0000-000000000002", "COMPLETADO")));

        assertEquals(2, bean.completarPasosConsulta("").size());
        assertEquals(2, bean.completarPasosConsulta(null).size());
    }

    @Test
    public void completarPasosConsulta_estadoNulo_soloCoincideConConsultaVaciaOPorId() {
        ConsultaProcedimientoPaso sinEstado = paso("aaaaaaaa-0000-0000-0000-000000000001", null);
        when(consultaProcedimientoPasoDAO.findRange(0, 100)).thenReturn(Arrays.asList(sinEstado));

        assertEquals(1, bean.completarPasosConsulta("").size());
        assertEquals(1, bean.completarPasosConsulta("aaaaaaaa").size());
        assertEquals(0, bean.completarPasosConsulta("zzz").size());
    }

    @Test
    public void completarPasosConsulta_limitaA20Resultados() {
        List<ConsultaProcedimientoPaso> muchos = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            muchos.add(paso(UUID.randomUUID().toString(), "EN_PROCESO"));
        }
        when(consultaProcedimientoPasoDAO.findRange(0, 100)).thenReturn(muchos);

        assertEquals(20, bean.completarPasosConsulta("en_proceso").size());
    }
}