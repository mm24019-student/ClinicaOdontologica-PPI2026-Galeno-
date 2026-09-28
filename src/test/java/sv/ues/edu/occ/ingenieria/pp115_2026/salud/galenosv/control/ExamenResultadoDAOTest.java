package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenResultado;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de ExamenResultadoDAO. El CRUD heredado ya está cubierto por
 * DefaultDAOTest; aquí se prueba lo propio: findByOrdenExamen (filtra por el
 * id de la orden y ordena por fecha de creación descendente) y que la clase
 * de la entidad se resuelva bien (ExamenResultado.findAll).
 */
@ExtendWith(MockitoExtension.class)
public class ExamenResultadoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ExamenResultado> query;

    @InjectMocks
    private ExamenResultadoDAO dao;

    // ---- findByOrdenExamen() ----

    @Test
    public void findByOrdenExamen_filtraPorIdDeOrdenYDevuelveResultados() {
        UUID idOrden = UUID.randomUUID();
        List<ExamenResultado> esperado = Arrays.asList(
                new ExamenResultado(UUID.randomUUID()), new ExamenResultado(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(ExamenResultado.class))).thenReturn(query);
        when(query.setParameter("id", idOrden)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<ExamenResultado> resultado = dao.findByOrdenExamen(idOrden);

        assertEquals(esperado, resultado);
        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(jpql.capture(), eq(ExamenResultado.class));
        assertTrue(jpql.getValue().contains("r.idOrdenExamen.idOrdenExamen = :id"));
        verify(query).setParameter("id", idOrden);
    }

    @Test
    public void findByOrdenExamen_ordenaPorFechaDeCreacionDescendente() {
        when(em.createQuery(anyString(), eq(ExamenResultado.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        dao.findByOrdenExamen(UUID.randomUUID());

        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(jpql.capture(), eq(ExamenResultado.class));
        assertTrue(jpql.getValue().contains("ORDER BY r.fechaCreacion DESC"));
    }

    @Test
    public void findByOrdenExamen_sinResultados_devuelveListaVacia() {
        when(em.createQuery(anyString(), eq(ExamenResultado.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertTrue(dao.findByOrdenExamen(UUID.randomUUID()).isEmpty());
    }

    // ---- heredado de DefaultDAO, con la entidad real ----

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void getFindAllQueryName_usaElNombreDeLaEntidad() {
        assertEquals("ExamenResultado.findAll", dao.getFindAllQueryName());
    }

    @Test
    public void crear_delegaEnPersist() {
        ExamenResultado entidad = new ExamenResultado(UUID.randomUUID());

        dao.crear(entidad);

        verify(em).persist(entidad);
    }
}