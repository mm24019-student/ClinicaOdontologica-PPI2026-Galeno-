package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de ExamenDAO. La lógica CRUD ya la cubre DefaultDAOTest; aquí solo
 * el EntityManager y la resolución de entidad / NamedQuery "Examen.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class ExamenDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Examen> query;

    @InjectMocks
    private ExamenDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findRange_usaLaNamedQueryDeExamenYAplicaElRango() {
        List<Examen> esperado = Arrays.asList(new Examen(UUID.randomUUID()));
        when(em.createNamedQuery("Examen.findAll", Examen.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findRange(0, 100));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void buscar_delegaEnFindConLaClaseExamen() {
        UUID id = UUID.randomUUID();
        Examen examen = new Examen(id);
        when(em.find(Examen.class, id)).thenReturn(examen);

        assertSame(examen, dao.buscar(id));
    }

    @Test
    public void crear_delegaEnPersist() {
        Examen examen = new Examen(UUID.randomUUID());

        dao.crear(examen);

        verify(em).persist(examen);
    }
}