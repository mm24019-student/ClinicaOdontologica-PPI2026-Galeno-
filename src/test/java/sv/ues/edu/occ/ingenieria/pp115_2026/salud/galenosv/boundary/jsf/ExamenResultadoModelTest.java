package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenResultado;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de ExamenResultadoModel (detalle de OrdenExamen, pestaña
 * "Resultados"): carga filtrada por orden con cargarDe(), orden padre
 * asignada automáticamente a cada resultado nuevo, y recarga filtrada (no la
 * lista completa) después de crear, modificar o eliminar.
 */
@ExtendWith(MockitoExtension.class)
public class ExamenResultadoModelTest {

    @Mock
    private ExamenResultadoDAO examenResultadoDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private ExamenResultadoModel bean;

    private OrdenExamen orden;

    @BeforeEach
    public void setUp() {
        orden = new OrdenExamen(UUID.randomUUID());
    }

    // ---- getDAO() / obtenerId() / metodos del padre ----

    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(examenResultadoDAO, bean.getDAO());
    }

    @Test
    public void obtenerId_devuelveElIdDelResultado() {
        UUID id = UUID.randomUUID();

        assertEquals(id, bean.obtenerId(new ExamenResultado(id)));
    }

    @Test
    public void obtenerIdPadre_devuelveElIdDeLaOrden() {
        assertEquals(orden.getIdOrdenExamen(), bean.obtenerIdPadre(orden));
    }

    @Test
    public void buscarPorPadre_delegaEnFindByOrdenExamen() {
        List<ExamenResultado> esperado = Arrays.asList(new ExamenResultado(UUID.randomUUID()));
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen())).thenReturn(esperado);

        assertEquals(esperado, bean.buscarPorPadre(orden.getIdOrdenExamen()));
    }

    @Test
    public void asignarPadre_ligaElResultadoALaOrden() {
        ExamenResultado hijo = new ExamenResultado(UUID.randomUUID());

        bean.asignarPadre(hijo, orden);

        assertSame(orden, hijo.getIdOrdenExamen());
    }

    // ---- inicializar() ----

    @Test
    public void inicializar_arrancaVacioSinConsultarLaBase() {
        bean.inicializar();

        assertTrue(bean.getregistros().isEmpty());
        verifyNoInteractions(examenResultadoDAO);
    }

    // ---- cargarDe() ----

    @Test
    public void cargarDe_ordenValida_cargaSoloSusResultadosYReseteaElEstado() {
        List<ExamenResultado> esperado = Arrays.asList(new ExamenResultado(UUID.randomUUID()));
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen())).thenReturn(esperado);
        bean.setRegistro(new ExamenResultado(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.cargarDe(orden);

        assertEquals(esperado, bean.getregistros());
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    @Test
    public void cargarDe_ordenNula_dejaListaVaciaSinConsultar() {
        bean.cargarDe(null);

        assertTrue(bean.getregistros().isEmpty());
        verify(examenResultadoDAO, never()).findByOrdenExamen(any());
    }

    @Test
    public void cargarDe_ordenSinId_dejaListaVaciaSinConsultar() {
        bean.cargarDe(new OrdenExamen());

        assertTrue(bean.getregistros().isEmpty());
        verify(examenResultadoDAO, never()).findByOrdenExamen(any());
    }

    // ---- btnNuevoHandler() / crearRegistroNuevo() / configurarNuevoRegistro() ----

    @Test
    public void btnNuevoHandler_conOrdenActual_creaResultadoConFechaYLigadoALaOrden() {
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen())).thenReturn(Collections.emptyList());
        bean.cargarDe(orden);

        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(bean.getRegistro().getIdExamenResultado());
        assertNotNull(bean.getRegistro().getFechaCreacion());
        assertSame(orden, bean.getRegistro().getIdOrdenExamen());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void btnNuevoHandler_sinOrdenActual_creaResultadoSinOrden() {
        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNull(bean.getRegistro().getIdOrdenExamen());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    // ---- btnCrearhandler() ----

    @Test
    public void btnCrearhandler_exito_creaYRecargaSoloLosResultadosDeLaOrden() {
        List<ExamenResultado> delPadre = Arrays.asList(new ExamenResultado(UUID.randomUUID()));
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen()))
                .thenReturn(Collections.emptyList())
                .thenReturn(delPadre);
        bean.cargarDe(orden);
        ExamenResultado nuevo = new ExamenResultado(UUID.randomUUID());
        bean.setRegistro(nuevo);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(examenResultadoDAO).crear(nuevo);
        assertEquals(delPadre, bean.getregistros());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_registroNulo_agregaMensajeDeErrorYNoCrea() {
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen())).thenReturn(Collections.emptyList());
        bean.cargarDe(orden);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(examenResultadoDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    // ---- btnModificarHandler() ----

    @Test
    public void btnModificarHandler_exito_actualizaYRecargaSoloLosResultadosDeLaOrden() {
        List<ExamenResultado> delPadre = Arrays.asList(new ExamenResultado(UUID.randomUUID()));
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen()))
                .thenReturn(Collections.emptyList())
                .thenReturn(delPadre);
        bean.cargarDe(orden);
        ExamenResultado existente = new ExamenResultado(UUID.randomUUID());
        when(examenResultadoDAO.actualizar(existente)).thenReturn(existente);
        bean.setRegistro(existente);

        bean.btnModificarHandler();

        verify(examenResultadoDAO).actualizar(existente);
        assertEquals(delPadre, bean.getregistros());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    // ---- btnEliminarHandler() ----

    @Test
    public void btnEliminarHandler_exito_eliminaYRecargaSoloLosResultadosDeLaOrden() {
        ExamenResultado existente = new ExamenResultado(UUID.randomUUID());
        List<ExamenResultado> antes = Arrays.asList(existente);
        when(examenResultadoDAO.findByOrdenExamen(orden.getIdOrdenExamen()))
                .thenReturn(antes)
                .thenReturn(Collections.emptyList());
        bean.cargarDe(orden);
        bean.setRegistro(existente);

        bean.btnEliminarHandler();

        verify(examenResultadoDAO).eliminar(existente.getIdExamenResultado());
        assertTrue(bean.getregistros().isEmpty());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    @Test
    public void btnEliminarHandler_registroNulo_noHaceNada() {
        bean.btnEliminarHandler();

        verify(examenResultadoDAO, never()).eliminar(any());
    }
}