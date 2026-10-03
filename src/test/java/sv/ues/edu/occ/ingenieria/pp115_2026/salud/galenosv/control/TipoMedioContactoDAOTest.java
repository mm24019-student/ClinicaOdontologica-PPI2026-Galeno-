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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de TipoMedioContactoDAO. La lógica CRUD ya la cubre DefaultDAOTest;
 * aquí solo el EntityManager y la resolución de entidad / NamedQuery
 * "TipoMedioContacto.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class TipoMedioContactoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TipoMedioContacto> query;

    @InjectMocks
    private TipoMedioContactoDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findRange_usaLaNamedQueryDeTipoMedioContactoYAplicaElRango() {
        List<TipoMedioContacto> esperado = Arrays.asList(new TipoMedioContacto(UUID.randomUUID()));
        when(em.createNamedQuery("TipoMedioContacto.findAll", TipoMedioContacto.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findRange(0, 100));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void buscar_delegaEnFindConLaClaseTipoMedioContacto() {
        UUID id = UUID.randomUUID();
        TipoMedioContacto tipo = new TipoMedioContacto(id);
        when(em.find(TipoMedioContacto.class, id)).thenReturn(tipo);

        assertSame(tipo, dao.buscar(id));
    }

    @Test
    public void crear_delegaEnPersist() {
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());

        dao.crear(tipo);

        verify(em).persist(tipo);
    }
}