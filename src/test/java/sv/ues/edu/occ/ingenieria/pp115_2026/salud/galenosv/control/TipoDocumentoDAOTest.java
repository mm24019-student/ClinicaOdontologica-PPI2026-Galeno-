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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Prueba de TipoDocumentoDAO. La lógica CRUD ya la cubre DefaultDAOTest; aquí
 * el EntityManager, la NamedQuery "TipoDocumento.findAll" y existeNombre()
 * (validación de duplicados sin distinguir mayúsculas ni espacios).
 */
@ExtendWith(MockitoExtension.class)
public class TipoDocumentoDAOTest {

    private static final String JPQL_EXISTE_NOMBRE
            = "SELECT COUNT(t) FROM TipoDocumento t "
            + "WHERE LOWER(TRIM(t.nombre)) = :nombre "
            + "AND t.idTipoDocumento <> :excluir";

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TipoDocumento> query;

    @Mock
    private TypedQuery<Long> queryConteo;

    @InjectMocks
    private TipoDocumentoDAO dao;

    // El DAO encadena setParameter(...).setParameter(...).getSingleResult(),
    // así que cada setParameter debe devolver el mismo mock.
    private void conteo(Long total) {
        when(em.createQuery(JPQL_EXISTE_NOMBRE, Long.class)).thenReturn(queryConteo);
        when(queryConteo.setParameter(anyString(), any())).thenReturn(queryConteo);
        when(queryConteo.getSingleResult()).thenReturn(total);
    }

    // ---- genéricos ----

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findRange_usaLaNamedQueryDeTipoDocumentoYAplicaElRango() {
        List<TipoDocumento> esperado = Arrays.asList(new TipoDocumento(UUID.randomUUID()));
        when(em.createNamedQuery("TipoDocumento.findAll", TipoDocumento.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findRange(0, 100));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void buscar_delegaEnFindConLaClaseTipoDocumento() {
        UUID id = UUID.randomUUID();
        TipoDocumento tipo = new TipoDocumento(id);
        when(em.find(TipoDocumento.class, id)).thenReturn(tipo);

        assertSame(tipo, dao.buscar(id));
    }

    // ---- existeNombre() ----

    @Test
    public void existeNombre_nombreNulo_devuelveFalseSinConsultar() {
        assertFalse(dao.existeNombre(null, UUID.randomUUID()));
        verifyNoInteractions(em);
    }

    @Test
    public void existeNombre_nombreEnBlanco_devuelveFalseSinConsultar() {
        assertFalse(dao.existeNombre("   ", UUID.randomUUID()));
        verifyNoInteractions(em);
    }

    @Test
    public void existeNombre_hayCoincidencias_devuelveTrue() {
        conteo(1L);

        assertTrue(dao.existeNombre("DUI", UUID.randomUUID()));
    }

    @Test
    public void existeNombre_sinCoincidencias_devuelveFalse() {
        conteo(0L);

        assertFalse(dao.existeNombre("DUI", UUID.randomUUID()));
    }

    @Test
    public void existeNombre_normalizaElNombreYEnviaElIdAExcluir() {
        UUID idExcluir = UUID.randomUUID();
        conteo(0L);

        dao.existeNombre("  DuI  ", idExcluir);

        verify(queryConteo).setParameter("nombre", "dui");
        verify(queryConteo).setParameter("excluir", idExcluir);
    }

    @Test
    public void existeNombre_idExcluirNulo_usaUuidCeroParaNoExcluirNada() {
        conteo(0L);

        dao.existeNombre("DUI", null);

        verify(queryConteo).setParameter("excluir", new UUID(0L, 0L));
    }
}