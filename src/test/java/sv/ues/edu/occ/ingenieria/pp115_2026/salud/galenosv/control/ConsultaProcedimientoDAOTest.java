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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;

/**
 *
 * @author antonio
 */
/**
 * Prueba de ConsultaProcedimientoDAO. Lo genérico ya queda cubierto por
 * DefaultDAOTest; aquí solo el EntityManager y findByConsulta.
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoDAOTest {
 
    private static final String JPQL_POR_CONSULTA
            = "SELECT e FROM ConsultaProcedimiento e WHERE e.idConsulta.idConsulta = :id";
 
    @Mock
    private EntityManager em;
 
    @Mock
    private TypedQuery<ConsultaProcedimiento> query;
 
    @InjectMocks
    private ConsultaProcedimientoDAO dao;
 
    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }
 
    @Test
    public void findByConsulta_armaQueryConIdDeLaConsultaYDevuelveResultados() {
        UUID idConsulta = UUID.randomUUID();
        List<ConsultaProcedimiento> esperado = Arrays.asList(
                new ConsultaProcedimiento(UUID.randomUUID()),
                new ConsultaProcedimiento(UUID.randomUUID()));
        when(em.createQuery(JPQL_POR_CONSULTA, ConsultaProcedimiento.class)).thenReturn(query);
        when(query.setParameter("id", idConsulta)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);
 
        List<ConsultaProcedimiento> resultado = dao.findByConsulta(idConsulta);
 
        assertEquals(esperado, resultado);
        verify(em).createQuery(JPQL_POR_CONSULTA, ConsultaProcedimiento.class);
        verify(query).setParameter("id", idConsulta);
    }
 
    @Test
    public void findByConsulta_sinProcedimientos_devuelveListaVacia() {
        UUID idConsulta = UUID.randomUUID();
        when(em.createQuery(JPQL_POR_CONSULTA, ConsultaProcedimiento.class)).thenReturn(query);
        when(query.setParameter("id", idConsulta)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
 
        assertTrue(dao.findByConsulta(idConsulta).isEmpty());
    }
}
