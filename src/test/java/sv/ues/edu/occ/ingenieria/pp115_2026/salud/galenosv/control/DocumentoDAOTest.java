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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Prueba de DocumentoDAO. crear/eliminar/actualizar/buscar/findRange ya
 * quedan cubiertos por DefaultDAOTest; aquí findByPersona, los helpers de
 * combo y las validaciones de duplicados existeTipoParaPersona /
 * existeValorParaTipo.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class DocumentoDAOTest {

    private static final String JPQL_TIPO_PARA_PERSONA
            = "SELECT COUNT(d) FROM Documento d "
            + "WHERE d.idPersona.idPersona = :persona "
            + "AND d.idTipoDocumento.idTipoDocumento = :tipo "
            + "AND d.idDocumento <> :excluir";

    private static final String JPQL_VALOR_PARA_TIPO
            = "SELECT COUNT(d) FROM Documento d "
            + "WHERE LOWER(TRIM(d.valor)) = :valor "
            + "AND d.idTipoDocumento.idTipoDocumento = :tipo "
            + "AND d.idDocumento <> :excluir";

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Documento> queryDocumento;

    @Mock
    private TypedQuery<Persona> queryPersona;

    @Mock
    private TypedQuery<TipoDocumento> queryTipoDocumento;

    @Mock
    private TypedQuery<Long> queryConteo;

    @InjectMocks
    private DocumentoDAO dao;

    private void conteo(String jpql, Long total) {
        when(em.createQuery(jpql, Long.class)).thenReturn(queryConteo);
        when(queryConteo.setParameter(anyString(), any())).thenReturn(queryConteo);
        when(queryConteo.getSingleResult()).thenReturn(total);
    }

    // ---- findByPersona() ----

    @Test
    public void findByPersona_armaQueryConIdDePersonaYDevuelveResultados() {
        UUID idPersona = UUID.randomUUID();
        List<Documento> esperado = Arrays.asList(
                new Documento(UUID.randomUUID()), new Documento(UUID.randomUUID()));
        String jpqlEsperado = "SELECT d FROM Documento d WHERE d.idPersona.idPersona = :id";

        when(em.createQuery(jpqlEsperado, Documento.class)).thenReturn(queryDocumento);
        when(queryDocumento.setParameter("id", idPersona)).thenReturn(queryDocumento);
        when(queryDocumento.getResultList()).thenReturn(esperado);

        List<Documento> resultado = dao.findByPersona(idPersona);

        assertEquals(esperado, resultado);
        verify(em).createQuery(jpqlEsperado, Documento.class);
        verify(queryDocumento).setParameter("id", idPersona);
    }

    // ---- buscarPersona() ----

    @Test
    public void buscarPersona_idNulo_devuelveNullSinConsultar() {
        assertNull(dao.buscarPersona(null));
        verifyNoInteractions(em);
    }

    @Test
    public void buscarPersona_delegaEnFind() {
        UUID id = UUID.randomUUID();
        Persona persona = new Persona(id);
        when(em.find(Persona.class, id)).thenReturn(persona);

        assertSame(persona, dao.buscarPersona(id));
    }

    // ---- buscarTipoDocumento() ----

    @Test
    public void buscarTipoDocumento_idNulo_devuelveNullSinConsultar() {
        assertNull(dao.buscarTipoDocumento(null));
        verifyNoInteractions(em);
    }

    @Test
    public void buscarTipoDocumento_delegaEnFind() {
        UUID id = UUID.randomUUID();
        TipoDocumento tipo = new TipoDocumento(id);
        when(em.find(TipoDocumento.class, id)).thenReturn(tipo);

        assertSame(tipo, dao.buscarTipoDocumento(id));
    }

    // ---- listarPersonas() ----

    @Test
    public void listarPersonas_limitaA100YDevuelveResultados() {
        List<Persona> esperado = Arrays.asList(new Persona(UUID.randomUUID()));
        when(em.createNamedQuery("Persona.findAll", Persona.class)).thenReturn(queryPersona);
        when(queryPersona.setMaxResults(100)).thenReturn(queryPersona);
        when(queryPersona.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.listarPersonas());
        verify(queryPersona).setMaxResults(100);
    }

    // ---- listarTiposDocumento() ----

    @Test
    public void listarTiposDocumento_limitaA100YDevuelveResultados() {
        List<TipoDocumento> esperado = Arrays.asList(new TipoDocumento(UUID.randomUUID()));
        when(em.createNamedQuery("TipoDocumento.findAll", TipoDocumento.class)).thenReturn(queryTipoDocumento);
        when(queryTipoDocumento.setMaxResults(100)).thenReturn(queryTipoDocumento);
        when(queryTipoDocumento.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.listarTiposDocumento());
        verify(queryTipoDocumento).setMaxResults(100);
    }

    // ---- existeTipoParaPersona() ----

    @Test
    public void existeTipoParaPersona_hayCoincidencias_devuelveTrue() {
        conteo(JPQL_TIPO_PARA_PERSONA, 1L);

        assertTrue(dao.existeTipoParaPersona(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    public void existeTipoParaPersona_sinCoincidencias_devuelveFalse() {
        conteo(JPQL_TIPO_PARA_PERSONA, 0L);

        assertFalse(dao.existeTipoParaPersona(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    public void existeTipoParaPersona_enviaPersonaTipoYIdAExcluir() {
        UUID persona = UUID.randomUUID();
        UUID tipo = UUID.randomUUID();
        UUID excluir = UUID.randomUUID();
        conteo(JPQL_TIPO_PARA_PERSONA, 0L);

        dao.existeTipoParaPersona(persona, tipo, excluir);

        verify(queryConteo).setParameter("persona", persona);
        verify(queryConteo).setParameter("tipo", tipo);
        verify(queryConteo).setParameter("excluir", excluir);
    }

    @Test
    public void existeTipoParaPersona_idExcluirNulo_usaUuidCero() {
        conteo(JPQL_TIPO_PARA_PERSONA, 0L);

        dao.existeTipoParaPersona(UUID.randomUUID(), UUID.randomUUID(), null);

        verify(queryConteo).setParameter("excluir", new UUID(0L, 0L));
    }

    // ---- existeValorParaTipo() ----

    @Test
    public void existeValorParaTipo_valorNulo_devuelveFalseSinConsultar() {
        assertFalse(dao.existeValorParaTipo(null, UUID.randomUUID(), null));
        verifyNoInteractions(em);
    }

    @Test
    public void existeValorParaTipo_valorEnBlanco_devuelveFalseSinConsultar() {
        assertFalse(dao.existeValorParaTipo("  ", UUID.randomUUID(), null));
        verifyNoInteractions(em);
    }

    @Test
    public void existeValorParaTipo_hayCoincidencias_devuelveTrue() {
        conteo(JPQL_VALOR_PARA_TIPO, 2L);

        assertTrue(dao.existeValorParaTipo("01234567-8", UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    public void existeValorParaTipo_sinCoincidencias_devuelveFalse() {
        conteo(JPQL_VALOR_PARA_TIPO, 0L);

        assertFalse(dao.existeValorParaTipo("01234567-8", UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    public void existeValorParaTipo_normalizaElValorYEnviaTipoEIdAExcluir() {
        UUID tipo = UUID.randomUUID();
        UUID excluir = UUID.randomUUID();
        conteo(JPQL_VALOR_PARA_TIPO, 0L);

        dao.existeValorParaTipo("  AbC-123  ", tipo, excluir);

        verify(queryConteo).setParameter("valor", "abc-123");
        verify(queryConteo).setParameter("tipo", tipo);
        verify(queryConteo).setParameter("excluir", excluir);
    }

    @Test
    public void existeValorParaTipo_idExcluirNulo_usaUuidCero() {
        conteo(JPQL_VALOR_PARA_TIPO, 0L);

        dao.existeValorParaTipo("abc", UUID.randomUUID(), null);

        verify(queryConteo).setParameter("excluir", new UUID(0L, 0L));
    }
}