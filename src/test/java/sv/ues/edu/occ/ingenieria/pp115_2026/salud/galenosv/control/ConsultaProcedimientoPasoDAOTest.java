package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

/**
 *
 * @author antonio
 */
/**
 * Prueba de ConsultaProcedimientoPasoDAO. Lo genérico ya queda cubierto por
 * DefaultDAOTest; aquí solo el EntityManager, findByConsultaProcedimiento y
 * el eliminar() propio, que antes de borrar verifica que el paso no tenga
 * órdenes de examen asociadas (FK desde orden_examen).
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoPasoDAOTest {
 
    private static final String JPQL_POR_CONSULTA_PROCEDIMIENTO
            = "SELECT e FROM ConsultaProcedimientoPaso e WHERE e.idConsultaProcedimiento.idConsultaProcedimiento = :id";
 
    private static final String JPQL_CONTAR_ORDENES
            = "SELECT COUNT(o) FROM OrdenExamen o WHERE o.idConsultaProcedimientoPaso.idConsultaProcedimientoPaso = :id";
 
    @Mock
    private EntityManager em;
 
    @Mock
    private TypedQuery<ConsultaProcedimientoPaso> query;
 
    @Mock
    private TypedQuery<Long> queryConteo;
 
    @InjectMocks
    private ConsultaProcedimientoPasoDAO dao;
 
    private void ordenesAsociadas(UUID idPaso, Long total) {
        when(em.createQuery(JPQL_CONTAR_ORDENES, Long.class)).thenReturn(queryConteo);
        when(queryConteo.setParameter("id", idPaso)).thenReturn(queryConteo);
        when(queryConteo.getSingleResult()).thenReturn(total);
    }
 
    // ---- getEntityManager() ----
 
    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }
 
    // ---- findByConsultaProcedimiento() ----
 
    @Test
    public void findByConsultaProcedimiento_armaQueryConIdDelPadreYDevuelveResultados() {
        UUID idPadre = UUID.randomUUID();
        List<ConsultaProcedimientoPaso> esperado = Arrays.asList(
                new ConsultaProcedimientoPaso(UUID.randomUUID()),
                new ConsultaProcedimientoPaso(UUID.randomUUID()));
        when(em.createQuery(JPQL_POR_CONSULTA_PROCEDIMIENTO, ConsultaProcedimientoPaso.class)).thenReturn(query);
        when(query.setParameter("id", idPadre)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);
 
        List<ConsultaProcedimientoPaso> resultado = dao.findByConsultaProcedimiento(idPadre);
 
        assertEquals(esperado, resultado);
        verify(em).createQuery(JPQL_POR_CONSULTA_PROCEDIMIENTO, ConsultaProcedimientoPaso.class);
        verify(query).setParameter("id", idPadre);
    }
 
    @Test
    public void findByConsultaProcedimiento_sinPasos_devuelveListaVacia() {
        UUID idPadre = UUID.randomUUID();
        when(em.createQuery(JPQL_POR_CONSULTA_PROCEDIMIENTO, ConsultaProcedimientoPaso.class)).thenReturn(query);
        when(query.setParameter("id", idPadre)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
 
        assertTrue(dao.findByConsultaProcedimiento(idPadre).isEmpty());
    }
 
    // ---- eliminar() ----
 
    @Test
    public void eliminar_idNulo_lanzaIllegalArgumentSinConsultarLaBaseDeDatos() {
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(null));
 
        verifyNoInteractions(em);
    }
 
    @Test
    public void eliminar_conOrdenesDeExamen_lanzaIllegalStateConMensajeDeNegocioYNoBorra() {
        UUID idPaso = UUID.randomUUID();
        ordenesAsociadas(idPaso, 2L);
 
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.eliminar(idPaso));
 
        assertTrue(ex.getMessage().contains("órdenes de examen"));
        verify(em, never()).find(any(), any());
        verify(em, never()).remove(any());
    }
 
    @Test
    public void eliminar_sinOrdenesDeExamen_borraElPaso() {
        UUID idPaso = UUID.randomUUID();
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(idPaso);
        ordenesAsociadas(idPaso, 0L);
        when(em.find(ConsultaProcedimientoPaso.class, idPaso)).thenReturn(paso);
 
        dao.eliminar(idPaso);
 
        verify(em).remove(paso);
    }
 
    @Test
    public void eliminar_conteoNulo_seTrataComoSinOrdenesYBorra() {
        UUID idPaso = UUID.randomUUID();
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(idPaso);
        ordenesAsociadas(idPaso, null);
        when(em.find(ConsultaProcedimientoPaso.class, idPaso)).thenReturn(paso);
 
        dao.eliminar(idPaso);
 
        verify(em).remove(paso);
    }
 
    @Test
    public void eliminar_sinOrdenesPeroPasoInexistente_lanzaIllegalArgument() {
        UUID idPaso = UUID.randomUUID();
        ordenesAsociadas(idPaso, 0L);
        when(em.find(ConsultaProcedimientoPaso.class, idPaso)).thenReturn(null);
 
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(idPaso));
 
        verify(em, never()).remove(any());
    }
}
