package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba directa de AbstracCrudModel usando una subclase concreta mínima
 * (ItemModel) definida dentro del propio test. Complementa a RolModelTest, que
 * la cubre de forma indirecta: aquí se cubren además los hooks
 * (validarAntesDeGuardar, tieneRegistrosDependientes, configurarNuevoRegistro),
 * el diálogo, requerir() y filtrar().
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class AbstracCrudModelTest {

    // ---- Entidad y bean de mentira, solo para este test ----
    static class Item {

        final UUID id;
        final String nombre;

        Item(UUID id) {
            this(id, null);
        }

        Item(UUID id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    static class ItemModel extends AbstracCrudModel<Item> {

        private static final long serialVersionUID = 1L;

        private final transient InterfaceDAO<Item> dao;
        boolean dependientes = false;
        boolean valido = true;
        final List<Item> configurados = new ArrayList<>();

        ItemModel(InterfaceDAO<Item> dao, FacesContext fc) {
            this.dao = dao;
            this.fc = fc;
        }

        @Override
        protected InterfaceDAO<Item> getDAO() {
            return dao;
        }

        @Override
        protected Item crearRegistroNuevo() {
            return new Item(UUID.randomUUID());
        }

        @Override
        protected UUID obtenerId(Item registro) {
            return registro.id;
        }

        @Override
        protected boolean tieneRegistrosDependientes(Item registro) {
            return dependientes;
        }

        @Override
        protected void configurarNuevoRegistro(Item nuevoRegistro) {
            configurados.add(nuevoRegistro);
        }

        @Override
        protected boolean validarAntesDeGuardar() {
            if (!valido) {
                mensaje(FacesMessage.SEVERITY_ERROR, "Invalido", "Falta un dato");
            }
            return valido;
        }
    }

    @Mock
    private InterfaceDAO<Item> dao;
    @Mock
    private FacesContext fc;

    private ItemModel bean;

    @BeforeEach
    public void setUp() {
        bean = new ItemModel(dao, fc);
    }

    private FacesMessage ultimoMensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        return captor.getValue();
    }

    // ---- inicializar() ----

    @Test
    public void inicializar_cargaLosPrimeros100Registros() {
        List<Item> esperado = Arrays.asList(new Item(UUID.randomUUID()));
        when(dao.findRange(0, 100)).thenReturn(esperado);

        bean.inicializar();

        assertEquals(esperado, bean.getregistros());
    }

    // ---- btnNuevoHandler() ----

    @Test
    public void btnNuevoHandler_creaRegistroLoConfiguraYPasaAEstadoCrear() {
        bean.btnNuevoHandler(null);

        assertNotNull(bean.getRegistro());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
        assertEquals(1, bean.configurados.size());
        assertSame(bean.getRegistro(), bean.configurados.get(0));
    }

    // ---- btnCrearhandler() ----

    @Test
    public void btnCrearhandler_registroNulo_agregaMensajeDeErrorYNoCrea() {
        bean.btnCrearhandler(null);

        verify(dao, never()).crear(any());
        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnCrearhandler_validacionFalla_noCreaYConservaElRegistro() {
        Item nuevo = new Item(UUID.randomUUID());
        bean.setRegistro(nuevo);
        bean.setEstado(Estado_Crud.CREAR);
        bean.valido = false;

        bean.btnCrearhandler(null);

        verify(dao, never()).crear(any());
        assertSame(nuevo, bean.getRegistro());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnCrearhandler_exito_creaLimpiaYRecargaLaLista() {
        Item nuevo = new Item(UUID.randomUUID());
        List<Item> recargados = Arrays.asList(nuevo);
        when(dao.findRange(0, 100)).thenReturn(recargados);
        bean.setRegistro(nuevo);
        bean.setEstado(Estado_Crud.CREAR);

        bean.btnCrearhandler(null);

        verify(dao).crear(nuevo);
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
        assertEquals(FacesMessage.SEVERITY_INFO, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnCrearhandler_daoLanzaExcepcion_muestraElMensajeDeLaExcepcion() {
        Item nuevo = new Item(UUID.randomUUID());
        doThrow(new IllegalStateException("boom")).when(dao).crear(nuevo);
        bean.setRegistro(nuevo);

        bean.btnCrearhandler(null);

        FacesMessage m = ultimoMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, m.getSeverity());
        assertEquals("boom", m.getDetail());
        assertSame(nuevo, bean.getRegistro());
    }

    // ---- btnModificarHandler() ----

    @Test
    public void btnModificarHandler_registroNulo_agregaMensajeDeErrorYNoActualiza() {
        bean.btnModificarHandler();

        verify(dao, never()).actualizar(any());
        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnModificarHandler_exito_actualizaYRecarga() {
        Item existente = new Item(UUID.randomUUID());
        when(dao.findRange(0, 100)).thenReturn(Collections.emptyList());
        bean.setRegistro(existente);
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.btnModificarHandler();

        verify(dao).actualizar(existente);
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(FacesMessage.SEVERITY_INFO, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnModificarHandler_daoLanzaExcepcion_agregaMensajeDeError() {
        Item existente = new Item(UUID.randomUUID());
        doThrow(new IllegalStateException("boom")).when(dao).actualizar(existente);
        bean.setRegistro(existente);

        bean.btnModificarHandler();

        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    // ---- btnEliminarHandler() ----

    @Test
    public void btnEliminarHandlerConId_idNulo_agregaMensajeDeErrorYNoElimina() {
        bean.setRegistros(Arrays.asList(new Item(UUID.randomUUID())));

        bean.btnEliminarHandler((UUID) null);

        verify(dao, never()).eliminar(any());
        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnEliminarHandlerConId_listaVacia_agregaMensajeDeErrorYNoElimina() {
        bean.setRegistros(Collections.emptyList());

        bean.btnEliminarHandler(UUID.randomUUID());

        verify(dao, never()).eliminar(any());
        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnEliminarHandlerConId_conDependientes_avisaYNoElimina() {
        Item item = new Item(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(item));
        bean.dependientes = true;

        bean.btnEliminarHandler(item.id);

        verify(dao, never()).eliminar(any());
        assertEquals(FacesMessage.SEVERITY_WARN, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnEliminarHandlerConId_exito_eliminaLimpiaYRecarga() {
        Item item = new Item(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(item));
        bean.setRegistro(item);
        bean.setEstado(Estado_Crud.MODIFICAR);
        when(dao.findRange(0, 100)).thenReturn(Collections.emptyList());

        bean.btnEliminarHandler(item.id);

        verify(dao).eliminar(item.id);
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertTrue(bean.getregistros().isEmpty());
        assertEquals(FacesMessage.SEVERITY_INFO, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnEliminarHandlerConId_idQueNoEstaEnLaLista_intentaEliminarDeTodasFormas() {
        bean.setRegistros(Arrays.asList(new Item(UUID.randomUUID())));
        UUID otroId = UUID.randomUUID();
        when(dao.findRange(0, 100)).thenReturn(Collections.emptyList());

        bean.btnEliminarHandler(otroId);

        verify(dao).eliminar(otroId);
    }

    @Test
    public void btnEliminarHandlerConId_daoLanzaExcepcion_agregaMensajeDeError() {
        Item item = new Item(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(item));
        doThrow(new IllegalStateException("fk")).when(dao).eliminar(item.id);

        bean.btnEliminarHandler(item.id);

        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    @Test
    public void btnEliminarHandlerSinArgumentos_registroNulo_noHaceNada() {
        bean.btnEliminarHandler();

        verify(dao, never()).eliminar(any());
        verifyNoInteractions(fc);
    }

    @Test
    public void btnEliminarHandlerSinArgumentos_usaElIdDelRegistroActual() {
        Item item = new Item(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(item));
        bean.setRegistro(item);
        when(dao.findRange(0, 100)).thenReturn(Collections.emptyList());

        bean.btnEliminarHandler();

        verify(dao).eliminar(item.id);
    }

    // ---- seleccion / cancelar ----

    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_fijaElRegistroYEstadoModificar() {
        Item item = new Item(UUID.randomUUID());
        SelectEvent<Item> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(item);

        bean.onRowSelect(event);

        assertSame(item, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
    }

    @Test
    public void btnSeleccionarRegistro_idExistente_fijaElRegistroYEstadoModificar() {
        Item item = new Item(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(item));

        bean.btnSeleccionarRegistro(item.id);

        assertSame(item, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
    }

    @Test
    public void btnSeleccionarRegistro_idNuloOInexistente_noCambiaNada() {
        bean.setRegistros(Arrays.asList(new Item(UUID.randomUUID())));

        bean.btnSeleccionarRegistro(null);
        bean.btnSeleccionarRegistro(UUID.randomUUID());

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    @Test
    public void btnCancelar_limpiaRegistroYEstado() {
        bean.setRegistro(new Item(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.btnCancelar();

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    // ---- dialogo ----

    @Test
    public void dialogo_empiezaCerrado() {
        assertFalse(bean.mostrarDialogo);
    }

    @Test
    public void btnAbrirDialogo_muestraElDialogo() {
        bean.btnAbrirDialogo();

        assertTrue(bean.mostrarDialogo);
    }

    @Test
    public void btnCerrarDialogo_cierraYLimpiaLaSeleccion() {
        bean.btnAbrirDialogo();
        bean.setRegistro(new Item(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.btnCerrarDialogo();

        assertFalse(bean.mostrarDialogo);
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    // ---- requerir() ----

    @Test
    public void requerir_valorNulo_agregaMensajeDeErrorYDevuelveFalse() {
        assertFalse(bean.requerir(null, "Falta", "detalle"));

        FacesMessage m = ultimoMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, m.getSeverity());
        assertEquals("Falta", m.getSummary());
        assertEquals("detalle", m.getDetail());
    }

    @Test
    public void requerir_valorPresente_devuelveTrueSinMensaje() {
        assertTrue(bean.requerir("algo", "Falta", "detalle"));

        verifyNoInteractions(fc);
    }

    // ---- filtrar() ----

    @Test
    public void filtrar_fuenteNula_devuelveListaVacia() {
        assertTrue(bean.<String>filtrar(null, "a", s -> s).isEmpty());
    }

    @Test
    public void filtrar_ignoraMayusculasYEspaciosDelTexto() {
        List<String> resultado = bean.filtrar(Arrays.asList("Alfa", "beta", "GAMMA"), "  AL ", s -> s);

        assertEquals(Arrays.asList("Alfa"), resultado);
    }

    @Test
    public void filtrar_textoNuloOVacio_devuelveTodo() {
        List<String> fuente = Arrays.asList("Alfa", "beta");

        assertEquals(fuente, bean.filtrar(fuente, null, s -> s));
        assertEquals(fuente, bean.filtrar(fuente, "", s -> s));
    }

    @Test
    public void filtrar_etiquetaNulaCuentaComoTextoVacio() {
        Item sinNombre = new Item(UUID.randomUUID(), null);
        Item conNombre = new Item(UUID.randomUUID(), "Pedro");
        List<Item> fuente = Arrays.asList(sinNombre, conNombre);

        assertEquals(fuente, bean.filtrar(fuente, "", i -> i.nombre));
        assertEquals(Arrays.asList(conNombre), bean.filtrar(fuente, "ped", i -> i.nombre));
    }

    // ---- SelectableDataModel ----

    @Test
    public void getRowKey_devuelveElIdComoTexto() {
        Item item = new Item(UUID.randomUUID());

        assertEquals(item.id.toString(), bean.getRowKey(item));
    }

    @Test
    public void getRowData_encuentraPorRowKey() {
        Item item = new Item(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(new Item(UUID.randomUUID()), item));

        assertSame(item, bean.getRowData(item.id.toString()));
    }

    @Test
    public void getRowData_sinCoincidenciaSinListaOClaveNula_devuelveNull() {
        assertNull(bean.getRowData("x"));

        bean.setRegistros(Arrays.asList(new Item(UUID.randomUUID())));

        assertNull(bean.getRowData(UUID.randomUUID().toString()));
        assertNull(bean.getRowData(null));
    }
}