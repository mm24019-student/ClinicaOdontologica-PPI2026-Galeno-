package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Prueba de DefaultDAO totalmente aislada: no depende de ningún DAO ni
 * entidad reales del proyecto (ni RolDAO, ni Rol). Se prueba directamente
 * contra una subclase anónima mínima creada aquí mismo, usando una entidad
 * de prueba (DummyEntity) que tampoco existe en el dominio real.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class DefaultDAOTest {

    // Entidad "de mentira", solo para este test. A DefaultDAO no le importa
    // que no sea una @Entity real: solo necesita una clase concreta para
    // que getEntityClass() pueda resolver T = DummyEntity por reflexión.
    private static class DummyEntity {
    }

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<DummyEntity> query;

    private DefaultDAO<DummyEntity> dao;

    @BeforeEach
    public void setUp() {
        // Subclase anónima: su única razón de existir es darle a DefaultDAO
        // un getGenericSuperclass() válido y devolver el EntityManager mock.
        dao = new DefaultDAO<DummyEntity>() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        };
    }

    // ---- crear() ----

    @Test
    public void crear_registroNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> dao.crear(null));
        verifyNoInteractions(em);
    }

    @Test
    public void crear_exito_llamaPersist() {
        DummyEntity entidad = new DummyEntity();

        dao.crear(entidad);

        verify(em).persist(entidad);
    }

    @Test
    public void crear_persistLanzaExcepcion_seEnvuelveEnIllegalState() {
        DummyEntity entidad = new DummyEntity();
        doThrow(new RuntimeException("fallo bd")).when(em).persist(entidad);

        assertThrows(IllegalStateException.class, () -> dao.crear(entidad));
    }

    // ---- eliminar() ----

    @Test
    public void eliminar_idNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(null));
        verifyNoInteractions(em);
    }

    @Test
    public void eliminar_noExiste_lanzaExcepcion() {
        UUID id = UUID.randomUUID();
        when(em.find(DummyEntity.class, id)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(id));
        verify(em, never()).remove(any());
    }

    @Test
    public void eliminar_existe_loRemueve() {
        UUID id = UUID.randomUUID();
        DummyEntity entidad = new DummyEntity();
        when(em.find(DummyEntity.class, id)).thenReturn(entidad);

        dao.eliminar(id);

        verify(em).remove(entidad);
    }

    // ---- actualizar() ----

    @Test
    public void actualizar_registroNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> dao.actualizar(null));
        verifyNoInteractions(em);
    }

    @Test
    public void actualizar_exito_devuelveMerge() {
        DummyEntity entidad = new DummyEntity();
        DummyEntity actualizado = new DummyEntity();
        when(em.merge(entidad)).thenReturn(actualizado);

        DummyEntity resultado = dao.actualizar(entidad);

        assertSame(actualizado, resultado);
    }

    @Test
    public void actualizar_mergeLanzaExcepcion_seEnvuelveEnIllegalState() {
        DummyEntity entidad = new DummyEntity();
        when(em.merge(entidad)).thenThrow(new RuntimeException("fallo bd"));

        assertThrows(IllegalStateException.class, () -> dao.actualizar(entidad));
    }

    // ---- buscar() ----

    @Test
    public void buscar_idNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscar(null));
        verifyNoInteractions(em);
    }

    @Test
    public void buscar_delegaEnFind() {
        UUID id = UUID.randomUUID();
        DummyEntity entidad = new DummyEntity();
        when(em.find(DummyEntity.class, id)).thenReturn(entidad);

        DummyEntity resultado = dao.buscar(id);

        assertSame(entidad, resultado);
    }

    // ---- findRange() ----

    @Test
    public void findRange_parametrosInvalidos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0));
        verifyNoInteractions(em);
    }

    @Test
    public void findRange_armaQueryYDevuelveResultados() {
        List<DummyEntity> esperado = Arrays.asList(new DummyEntity(), new DummyEntity());
        when(em.createNamedQuery("DummyEntity.findAll", DummyEntity.class)).thenReturn(query);
        when(query.setFirstResult(0)).thenReturn(query);
        when(query.setMaxResults(10)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<DummyEntity> resultado = dao.findRange(0, 10);

        assertEquals(esperado, resultado);
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(10);
    }

    @Test
    public void findRange_queryLanzaExcepcion_seEnvuelveEnIllegalState() {
        when(em.createNamedQuery("DummyEntity.findAll", DummyEntity.class)).thenThrow(new RuntimeException("fallo bd"));

        assertThrows(IllegalStateException.class, () -> dao.findRange(0, 10));
    }

    // ---- buscarPorPadre() ----
    // Es protected en DefaultDAO; como este test está en el mismo paquete
    // "control", se puede llamar directo sin necesidad de exponerlo.

    @Test
    public void buscarPorPadre_armaQueryConElParametroDado() {
        UUID idPadre = UUID.randomUUID();
        List<DummyEntity> esperado = Arrays.asList(new DummyEntity());
        String jpql = "SELECT d FROM DummyEntity d WHERE d.padre.id = :id";

        when(em.createQuery(jpql, DummyEntity.class)).thenReturn(query);
        when(query.setParameter("id", idPadre)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<DummyEntity> resultado = dao.buscarPorPadre(jpql, "id", idPadre);

        assertEquals(esperado, resultado);
        verify(query).setParameter("id", idPadre);
    }
}