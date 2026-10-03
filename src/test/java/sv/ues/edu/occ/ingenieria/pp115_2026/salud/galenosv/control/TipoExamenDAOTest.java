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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de TipoExamenDAO. La lógica CRUD ya la cubre DefaultDAOTest; aquí
 * solo el EntityManager y la resolución de entidad / NamedQuery
 * "TipoExamen.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class TipoExamenDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TipoExamen> query;

    @InjectMocks
    private TipoExamenDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findRange_usaLaNamedQueryDeTipoExamenYAplicaElRango() {
        List<TipoExamen> esperado = Arrays.asList(new TipoExamen(UUID.randomUUID()));
        when(em.createNamedQuery("TipoExamen.findAll", TipoExamen.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findRange(0, 100));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void buscar_delegaEnFindConLaClaseTipoExamen() {
        UUID id = UUID.randomUUID();
        TipoExamen tipo = new TipoExamen(id);
        when(em.find(TipoExamen.class, id)).thenReturn(tipo);

        assertSame(tipo, dao.buscar(id));
    }

    @Test
    public void crear_delegaEnPersist() {
        TipoExamen tipo = new TipoExamen(UUID.randomUUID());

        dao.crear(tipo);

        verify(em).persist(tipo);
    }
}