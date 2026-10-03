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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 * Prueba de ProcedimientoPasoSecuenciaDAO. Lo genérico ya queda cubierto por
 * DefaultDAOTest; aquí el EntityManager, findByProcedimientoPaso y
 * findByProcedimiento (todas las secuencias de un procedimiento en una sola
 * consulta, para armar el árbol y detectar ciclos).
 */
@ExtendWith(MockitoExtension.class)
public class ProcedimientoPasoSecuenciaDAOTest {

    private static final String JPQL_POR_PASO
            = "SELECT s FROM ProcedimientoPasoSecuencia s WHERE s.idProcedimientoPaso.idProcedimientoPaso = :id";

    private static final String JPQL_POR_PROCEDIMIENTO
            = "SELECT s FROM ProcedimientoPasoSecuencia s WHERE s.idProcedimientoPaso.idProcedimiento.idProcedimiento = :id";

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProcedimientoPasoSecuencia> query;

    @InjectMocks
    private ProcedimientoPasoSecuenciaDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    // ---- findByProcedimientoPaso() ----

    @Test
    public void findByProcedimientoPaso_armaQueryConIdDelPasoYDevuelveResultados() {
        UUID idPaso = UUID.randomUUID();
        List<ProcedimientoPasoSecuencia> esperado = Arrays.asList(
                new ProcedimientoPasoSecuencia(UUID.randomUUID()),
                new ProcedimientoPasoSecuencia(UUID.randomUUID()));
        when(em.createQuery(JPQL_POR_PASO, ProcedimientoPasoSecuencia.class)).thenReturn(query);
        when(query.setParameter("id", idPaso)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<ProcedimientoPasoSecuencia> resultado = dao.findByProcedimientoPaso(idPaso);

        assertEquals(esperado, resultado);
        verify(em).createQuery(JPQL_POR_PASO, ProcedimientoPasoSecuencia.class);
        verify(query).setParameter("id", idPaso);
    }

    @Test
    public void findByProcedimientoPaso_sinSecuencias_devuelveListaVacia() {
        UUID idPaso = UUID.randomUUID();
        when(em.createQuery(JPQL_POR_PASO, ProcedimientoPasoSecuencia.class)).thenReturn(query);
        when(query.setParameter("id", idPaso)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertTrue(dao.findByProcedimientoPaso(idPaso).isEmpty());
    }

    // ---- findByProcedimiento() ----

    @Test
    public void findByProcedimiento_armaQueryConIdDelProcedimientoYDevuelveResultados() {
        UUID idProcedimiento = UUID.randomUUID();
        List<ProcedimientoPasoSecuencia> esperado = Arrays.asList(
                new ProcedimientoPasoSecuencia(UUID.randomUUID()),
                new ProcedimientoPasoSecuencia(UUID.randomUUID()));
        when(em.createQuery(JPQL_POR_PROCEDIMIENTO, ProcedimientoPasoSecuencia.class)).thenReturn(query);
        when(query.setParameter("id", idProcedimiento)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<ProcedimientoPasoSecuencia> resultado = dao.findByProcedimiento(idProcedimiento);

        assertEquals(esperado, resultado);
        verify(em).createQuery(JPQL_POR_PROCEDIMIENTO, ProcedimientoPasoSecuencia.class);
        verify(query).setParameter("id", idProcedimiento);
    }

    @Test
    public void findByProcedimiento_sinSecuencias_devuelveListaVacia() {
        UUID idProcedimiento = UUID.randomUUID();
        when(em.createQuery(JPQL_POR_PROCEDIMIENTO, ProcedimientoPasoSecuencia.class)).thenReturn(query);
        when(query.setParameter("id", idProcedimiento)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertTrue(dao.findByProcedimiento(idProcedimiento).isEmpty());
    }
}