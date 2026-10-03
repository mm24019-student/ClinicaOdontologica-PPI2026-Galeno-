package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
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
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de RolModel, usado como implementación concreta representativa de
 * AbstracCrudModel: RolModel (como ClinicaModel, TipoDocumentoModel y
 * TipoMedioContactoModel) no agrega lógica propia más allá de conectar el DAO,
 * así que probar el flujo estándar (Nuevo -> Crear/Modificar/Eliminar,
 * selección de fila, getRowData/getRowKey) aquí cubre indirectamente a esas 4
 * clases también.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class RolModelTest {

    @Mock
    private RolDAO rolDAO;

    @Mock
    private SesionBean sesionBean;

    @Mock
    private FacesContext fc;

    @InjectMocks
    private RolModel bean;

    // ---- btnNuevoHandler() / crearRegistroNuevo() ----
    @Test
    public void btnNuevoHandler_creaRegistroActivoConEstadoCrear() {
        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(bean.getRegistro());
        assertNotNull(bean.getRegistro().getIdRol());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    // ---- btnCrearhandler() ----
    @Test
    public void btnCrearhandler_registroNulo_agregaMensajeDeErrorYNoCrea() {
        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(rolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_exito_creaYRecargaLaLista() {
        Rol nuevo = new Rol(UUID.randomUUID());
        bean.setRegistro(nuevo);
        List<Rol> recargados = Arrays.asList(new Rol(UUID.randomUUID()));
        when(rolDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(rolDAO).crear(nuevo);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertNull(bean.getRegistro());
        assertEquals(recargados, bean.getregistros());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_daoLanzaExcepcion_agregaMensajeDeErrorSinReventar() {
        Rol nuevo = new Rol(UUID.randomUUID());
        bean.setRegistro(nuevo);
        doThrow(new RuntimeException("fallo bd")).when(rolDAO).crear(nuevo);

        assertDoesNotThrow(() -> bean.btnCrearhandler(mock(ActionEvent.class)));

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    // ---- btnModificarHandler() ----
    @Test
    public void btnModificarHandler_registroNulo_agregaMensajeDeErrorYNoActualiza() {
        bean.btnModificarHandler();

        verify(rolDAO, never()).actualizar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_exito_actualizaYRecargaLaLista() {
        Rol existente = new Rol(UUID.randomUUID());
        bean.setRegistro(existente);
        List<Rol> recargados = Arrays.asList(existente);
        when(rolDAO.actualizar(existente)).thenReturn(existente);
        when(rolDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnModificarHandler();

        verify(rolDAO).actualizar(existente);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertNull(bean.getRegistro());
        assertEquals(recargados, bean.getregistros());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_daoLanzaExcepcion_agregaMensajeDeErrorSinReventar() {
        Rol existente = new Rol(UUID.randomUUID());
        bean.setRegistro(existente);
        when(rolDAO.actualizar(existente)).thenThrow(new RuntimeException("fallo bd"));

        assertDoesNotThrow(() -> bean.btnModificarHandler());

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    // ---- btnEliminarHandler(UUID) ----
    @Test
    public void btnEliminarHandlerConId_listaVacia_agregaMensajeDeErrorYNoElimina() {
        bean.btnEliminarHandler(UUID.randomUUID());

        verify(rolDAO, never()).eliminar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnEliminarHandlerConId_idNulo_agregaMensajeDeErrorYNoElimina() {
        bean.setWrappedData(Arrays.asList(new Rol(UUID.randomUUID())));

        bean.btnEliminarHandler((UUID) null);

        verify(rolDAO, never()).eliminar(any());
    }

    @Test
    public void btnEliminarHandlerConId_exito_eliminaYRecargaLaLista() {
        UUID id = UUID.randomUUID();
        bean.setWrappedData(Arrays.asList(new Rol(id)));
        List<Rol> recargados = Collections.emptyList();
        when(rolDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnEliminarHandler(id);

        verify(rolDAO).eliminar(id);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    public void btnEliminarHandlerConId_daoLanzaExcepcion_agregaMensajeDeErrorSinReventar() {
        UUID id = UUID.randomUUID();
        bean.setWrappedData(Arrays.asList(new Rol(id)));
        doThrow(new RuntimeException("fallo bd")).when(rolDAO).eliminar(id);

        assertDoesNotThrow(() -> bean.btnEliminarHandler(id));

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    // ---- btnEliminarHandler() sin argumentos ----
    @Test
    public void btnEliminarHandlerSinArgumentos_registroNulo_noHaceNada() {
        bean.btnEliminarHandler();

        verify(rolDAO, never()).eliminar(any());
        verifyNoInteractions(fc);
    }

    @Test
    public void btnEliminarHandlerSinArgumentos_delegaConElIdDelRegistroActual() {
        UUID id = UUID.randomUUID();
        Rol registro = new Rol(id);
        bean.setRegistro(registro);
        bean.setWrappedData(Arrays.asList(registro));
        when(rolDAO.findRange(0, 100)).thenReturn(Collections.emptyList());

        bean.btnEliminarHandler();

        verify(rolDAO).eliminar(id);
    }

    // ---- btnCancelar() ----
    @Test
    public void btnCancelar_limpiaRegistroYEstado() {
        bean.setRegistro(new Rol(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.btnCancelar();

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    // ---- onRowSelect() ----
    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_fijaElRegistroYEstadoModificar() {
        Rol rol = new Rol(UUID.randomUUID());
        SelectEvent<Rol> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(rol);

        bean.onRowSelect(event);

        assertSame(rol, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
    }

    // ---- btnSeleccionarRegistro(UUID) ----
    @Test
    public void btnSeleccionarRegistro_idExistente_fijaElRegistroYEstadoModificar() {
        UUID id = UUID.randomUUID();
        Rol rol = new Rol(id);
        bean.setWrappedData(Arrays.asList(rol, new Rol(UUID.randomUUID())));

        bean.btnSeleccionarRegistro(id);

        assertSame(rol, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
    }

    @Test
    public void btnSeleccionarRegistro_listaVacia_noHaceNada() {
        bean.btnSeleccionarRegistro(UUID.randomUUID());

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    // ---- inicializar() ----
    @Test
    public void inicializar_cargaLosPrimeros100Registros() {
        List<Rol> esperado = Arrays.asList(new Rol(UUID.randomUUID()));
        when(rolDAO.findRange(0, 100)).thenReturn(esperado);

        bean.inicializar();

        assertEquals(esperado, bean.getregistros());
    }

    // ---- getRowData() / getRowKey() ----
    @Test
    public void getRowKey_devuelveElIdComoTexto() {
        Rol rol = new Rol(UUID.randomUUID());

        assertEquals(rol.getIdRol().toString(), bean.getRowKey(rol));
    }

    @Test
    public void getRowData_encuentraElRegistroPorSuRowKey() {
        Rol rol = new Rol(UUID.randomUUID());
        bean.setWrappedData(Arrays.asList(rol, new Rol(UUID.randomUUID())));

        Rol resultado = bean.getRowData(rol.getIdRol().toString());

        assertSame(rol, resultado);
    }

    @Test
    public void getRowData_sinCoincidencia_devuelveNull() {
        bean.setWrappedData(Arrays.asList(new Rol(UUID.randomUUID())));

        Rol resultado = bean.getRowData(UUID.randomUUID().toString());

        assertNull(resultado);
    }

    @Test
    public void getRowData_sinListaCargada_devuelveNullSinReventar() {
        Rol resultado = bean.getRowData(UUID.randomUUID().toString());

        assertNull(resultado);
    }
}
