package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba directa de AbstracdetallecrudModel con un par Padre/Hijo de mentira.
 * Verifica que: arranca vacío sin ir a la base, cargarDe() filtra por padre,
 * el hijo nuevo nace ligado al padre, y tras crear/eliminar se vuelve a
 * filtrar por padre en vez de traer los 100 registros de todos.
 *
 * @author oscar
 */
public class AbstracdetallecrudModelTest {

    static class Padre {

        final UUID id;

        Padre(UUID id) {
            this.id = id;
        }
    }

    static class Hijo {

        final UUID id;
        Padre padre;

        Hijo(UUID id) {
            this.id = id;
        }
    }

    static class DetalleModel extends AbstracdetallecrudModel<Hijo, Padre> {

        private static final long serialVersionUID = 1L;

        private final transient InterfaceDAO<Hijo> dao;
        transient List<Hijo> respuesta = new ArrayList<>();
        final transient List<UUID> busquedas = new ArrayList<>();

        DetalleModel(InterfaceDAO<Hijo> dao, FacesContext fc) {
            this.dao = dao;
            this.fc = fc;
        }

        @Override
        protected InterfaceDAO<Hijo> getDAO() {
            return dao;
        }

        @Override
        protected Hijo crearRegistroNuevo() {
            return new Hijo(UUID.randomUUID());
        }

        @Override
        protected UUID obtenerId(Hijo registro) {
            return registro.id;
        }

        @Override
        protected List<Hijo> buscarPorPadre(UUID idPadre) {
            busquedas.add(idPadre);
            return respuesta;
        }

        @Override
        protected UUID obtenerIdPadre(Padre padre) {
            return padre.id;
        }

        @Override
        protected void asignarPadre(Hijo hijo, Padre padre) {
            hijo.padre = padre;
        }
    }

    private InterfaceDAO<Hijo> dao;
    private DetalleModel bean;
    private Padre padre;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        dao = mock(InterfaceDAO.class);
        bean = new DetalleModel(dao, mock(FacesContext.class));
        padre = new Padre(UUID.randomUUID());
    }

    // ---- inicializar() ----

    @Test
    public void inicializar_arrancaVacioSinConsultarLaBase() {
        bean.inicializar();

        assertTrue(bean.getregistros().isEmpty());
        verifyNoInteractions(dao);
        assertTrue(bean.busquedas.isEmpty());
    }

    // ---- cargarDe() ----

    @Test
    public void cargarDe_padreConId_cargaSoloLosHijosDeEsePadre() {
        Hijo h = new Hijo(UUID.randomUUID());
        bean.respuesta = Arrays.asList(h);

        bean.cargarDe(padre);

        assertEquals(Arrays.asList(padre.id), bean.busquedas);
        assertEquals(Arrays.asList(h), bean.getregistros());
        assertSame(padre, bean.padreActual);
    }

    @Test
    public void cargarDe_padreNulo_dejaListaVaciaSinBuscar() {
        bean.cargarDe(null);

        assertTrue(bean.getregistros().isEmpty());
        assertTrue(bean.busquedas.isEmpty());
        assertNull(bean.padreActual);
    }

    @Test
    public void cargarDe_padreSinId_dejaListaVaciaSinBuscar() {
        bean.cargarDe(new Padre(null));

        assertTrue(bean.getregistros().isEmpty());
        assertTrue(bean.busquedas.isEmpty());
    }

    @Test
    public void cargarDe_reseteaRegistroYEstadoPrevios() {
        bean.setRegistro(new Hijo(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.cargarDe(padre);

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    // ---- btnNuevoHandler() / configurarNuevoRegistro() ----

    @Test
    public void btnNuevoHandler_conPadre_elHijoNaceLigadoAlPadre() {
        bean.cargarDe(padre);

        bean.btnNuevoHandler(null);

        assertSame(padre, bean.getRegistro().padre);
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void btnNuevoHandler_sinPadre_elHijoNaceSinPadre() {
        bean.cargarDe(null);

        bean.btnNuevoHandler(null);

        assertNull(bean.getRegistro().padre);
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    // ---- recargarLista() tras crear / modificar / eliminar ----

    @Test
    public void btnCrearhandler_exito_recargaFiltrandoPorPadreYNoTraeLos100() {
        bean.cargarDe(padre);
        Hijo nuevo = new Hijo(UUID.randomUUID());
        bean.respuesta = Arrays.asList(nuevo);
        bean.setRegistro(nuevo);

        bean.btnCrearhandler(null);

        verify(dao).crear(nuevo);
        verify(dao, never()).findRange(anyInt(), anyInt());
        assertEquals(Arrays.asList(padre.id, padre.id), bean.busquedas);
        assertEquals(Arrays.asList(nuevo), bean.getregistros());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    @Test
    public void btnModificarHandler_exito_recargaFiltrandoPorPadre() {
        bean.cargarDe(padre);
        Hijo existente = new Hijo(UUID.randomUUID());
        bean.setRegistro(existente);

        bean.btnModificarHandler();

        verify(dao).actualizar(existente);
        verify(dao, never()).findRange(anyInt(), anyInt());
        assertEquals(2, bean.busquedas.size());
    }

    @Test
    public void btnEliminarHandler_exito_recargaFiltrandoPorPadre() {
        Hijo existente = new Hijo(UUID.randomUUID());
        bean.respuesta = Arrays.asList(existente);
        bean.cargarDe(padre);
        bean.setRegistro(existente);
        bean.respuesta = Collections.emptyList();

        bean.btnEliminarHandler();

        verify(dao).eliminar(existente.id);
        verify(dao, never()).findRange(anyInt(), anyInt());
        assertEquals(2, bean.busquedas.size());
        assertTrue(bean.getregistros().isEmpty());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }
}