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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenTipoExamen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de ExamenTipoExamenDAO. El CRUD heredado ya está cubierto por
 * DefaultDAOTest; aquí se prueba lo propio: findByExamen (filtra por el id
 * del examen y ordena por fecha de creación descendente) y que la clase de
 * la entidad se resuelva bien (ExamenTipoExamen.findAll).
 */
@ExtendWith(MockitoExtension.class)
public class ExamenTipoExamenDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ExamenTipoExamen> query;

    @InjectMocks
    private ExamenTipoExamenDAO dao;

    // ---- findByExamen() ----

    @Test
    public void findByExamen_filtraPorIdDeExamenYDevuelveResultados() {
        UUID idExamen = UUID.randomUUID();
        List<ExamenTipoExamen> esperado = Arrays.asList(
                new ExamenTipoExamen(UUID.randomUUID()), new ExamenTipoExamen(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(ExamenTipoExamen.class))).thenReturn(query);
        when(query.setParameter("idExamen", idExamen)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<ExamenTipoExamen> resultado = dao.findByExamen(idExamen);

        assertEquals(esperado, resultado);
        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(jpql.capture(), eq(ExamenTipoExamen.class));
        assertTrue(jpql.getValue().contains("e.idExamen.idExamen = :idExamen"));
        verify(query).setParameter("idExamen", idExamen);
    }

    @Test
    public void findByExamen_ordenaPorFechaDeCreacionDescendente() {
        when(em.createQuery(anyString(), eq(ExamenTipoExamen.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        dao.findByExamen(UUID.randomUUID());

        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(jpql.capture(), eq(ExamenTipoExamen.class));
        assertTrue(jpql.getValue().contains("ORDER BY e.fechaCreacion DESC"));
    }

    @Test
    public void findByExamen_sinResultados_devuelveListaVacia() {
        when(em.createQuery(anyString(), eq(ExamenTipoExamen.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertTrue(dao.findByExamen(UUID.randomUUID()).isEmpty());
    }

    // ---- heredado de DefaultDAO, con la entidad real ----

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void getFindAllQueryName_usaElNombreDeLaEntidad() {
        assertEquals("ExamenTipoExamen.findAll", dao.getFindAllQueryName());
    }

    @Test
    public void crear_delegaEnPersist() {
        ExamenTipoExamen entidad = new ExamenTipoExamen(UUID.randomUUID());

        dao.crear(entidad);

        verify(em).persist(entidad);
    }
}