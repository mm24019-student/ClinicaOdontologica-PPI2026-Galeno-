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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author antonio
 */
/**
 * Prueba de ProcedimientoPasoDAO. crear/eliminar/actualizar/buscar/findRange
 * ya quedan cubiertos por DefaultDAOTest (heredados sin cambios); aquí solo
 * se prueba lo propio: el EntityManager y findByProcedimiento (usa
 * buscarPorPadre con el JPQL de ProcedimientoPaso).
 */
@ExtendWith(MockitoExtension.class)
public class ProcedimientoPasoDAOTest {
 
    private static final String JPQL_POR_PROCEDIMIENTO
            = "SELECT pp FROM ProcedimientoPaso pp WHERE pp.idProcedimiento.idProcedimiento = :id";
 
    @Mock
    private EntityManager em;
 
    @Mock
    private TypedQuery<ProcedimientoPaso> query;
 
    @InjectMocks
    private ProcedimientoPasoDAO dao;
 
    // ---- getEntityManager() ----
 
    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }
 
    // ---- findByProcedimiento() ----
 
    @Test
    public void findByProcedimiento_armaQueryConIdDelProcedimientoYDevuelveResultados() {
        UUID idProcedimiento = UUID.randomUUID();
        List<ProcedimientoPaso> esperado = Arrays.asList(
                new ProcedimientoPaso(UUID.randomUUID()), new ProcedimientoPaso(UUID.randomUUID()));
        when(em.createQuery(JPQL_POR_PROCEDIMIENTO, ProcedimientoPaso.class)).thenReturn(query);
        when(query.setParameter("id", idProcedimiento)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);
 
        List<ProcedimientoPaso> resultado = dao.findByProcedimiento(idProcedimiento);
 
        assertEquals(esperado, resultado);
        verify(em).createQuery(JPQL_POR_PROCEDIMIENTO, ProcedimientoPaso.class);
        verify(query).setParameter("id", idProcedimiento);
    }
 
    @Test
    public void findByProcedimiento_sinPasos_devuelveListaVacia() {
        UUID idProcedimiento = UUID.randomUUID();
        when(em.createQuery(JPQL_POR_PROCEDIMIENTO, ProcedimientoPaso.class)).thenReturn(query);
        when(query.setParameter("id", idProcedimiento)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
 
        assertTrue(dao.findByProcedimiento(idProcedimiento).isEmpty());
    }
}
