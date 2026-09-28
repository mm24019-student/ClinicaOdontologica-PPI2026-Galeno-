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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de OrdenExamenDAO. No agrega consultas propias: el CRUD viene de
 * DefaultDAO (cubierto en DefaultDAOTest con una entidad de mentira). Aquí se
 * comprueba lo que DefaultDAOTest no puede: que con la entidad real
 * OrdenExamen el DAO resuelve bien su clase por reflexión (nombre de la NamedQuery
 * OrdenExamen.findAll, find/persist/merge con OrdenExamen.class) y usa el EntityManager
 * inyectado.
 */
@ExtendWith(MockitoExtension.class)
public class OrdenExamenDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<OrdenExamen> query;

    @InjectMocks
    private OrdenExamenDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void getFindAllQueryName_usaElNombreDeLaEntidad() {
        assertEquals("OrdenExamen.findAll", dao.getFindAllQueryName());
    }

    @Test
    public void findRange_usaLaNamedQueryOrdenExamenFindAll() {
        List<OrdenExamen> esperado = Arrays.asList(new OrdenExamen(UUID.randomUUID()));
        when(em.createNamedQuery("OrdenExamen.findAll", OrdenExamen.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<OrdenExamen> resultado = dao.findRange(0, 100);

        assertEquals(esperado, resultado);
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void crear_delegaEnPersist() {
        OrdenExamen entidad = new OrdenExamen(UUID.randomUUID());

        dao.crear(entidad);

        verify(em).persist(entidad);
    }

    @Test
    public void buscar_delegaEnFindConLaClaseOrdenExamen() {
        UUID id = UUID.randomUUID();
        OrdenExamen entidad = new OrdenExamen(id);
        when(em.find(OrdenExamen.class, id)).thenReturn(entidad);

        assertSame(entidad, dao.buscar(id));
    }

    @Test
    public void actualizar_delegaEnMerge() {
        OrdenExamen entidad = new OrdenExamen(UUID.randomUUID());
        when(em.merge(entidad)).thenReturn(entidad);

        assertSame(entidad, dao.actualizar(entidad));
    }

    @Test
    public void eliminar_existe_loRemueve() {
        UUID id = UUID.randomUUID();
        OrdenExamen entidad = new OrdenExamen(id);
        when(em.find(OrdenExamen.class, id)).thenReturn(entidad);

        dao.eliminar(id);

        verify(em).remove(entidad);
    }

    @Test
    public void eliminar_noExiste_lanzaExcepcionYNoRemueve() {
        UUID id = UUID.randomUUID();
        when(em.find(OrdenExamen.class, id)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(id));
        verify(em, never()).remove(any());
    }
}