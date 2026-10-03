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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Prueba de MedioContactoDAO. crear/eliminar/actualizar/buscar/findRange ya
 * quedan cubiertos por DefaultDAOTest; aquí findByPersona, los helpers de
 * combo y la validación de duplicados existeMedioParaPersona.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class MedioContactoDAOTest {

    private static final String JPQL_EXISTE_MEDIO
            = "SELECT COUNT(m) FROM MedioContacto m "
            + "WHERE m.idPersona.idPersona = :persona "
            + "AND m.idTipoMedioContacto.idTipoMedioContacto = :tipo "
            + "AND LOWER(TRIM(m.valor)) = :valor "
            + "AND m.idMedioContacto <> :excluir";

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<MedioContacto> queryMedioContacto;

    @Mock
    private TypedQuery<Persona> queryPersona;

    @Mock
    private TypedQuery<TipoMedioContacto> queryTipoMedioContacto;

    @Mock
    private TypedQuery<Long> queryConteo;

    @InjectMocks
    private MedioContactoDAO dao;

    private void conteo(Long total) {
        when(em.createQuery(JPQL_EXISTE_MEDIO, Long.class)).thenReturn(queryConteo);
        when(queryConteo.setParameter(anyString(), any())).thenReturn(queryConteo);
        when(queryConteo.getSingleResult()).thenReturn(total);
    }

    // ---- findByPersona() ----

    @Test
    public void findByPersona_armaQueryConIdDePersonaYDevuelveResultados() {
        UUID idPersona = UUID.randomUUID();
        List<MedioContacto> esperado = Arrays.asList(
                new MedioContacto(UUID.randomUUID()), new MedioContacto(UUID.randomUUID()));
        String jpqlEsperado = "SELECT m FROM MedioContacto m WHERE m.idPersona.idPersona = :id";

        when(em.createQuery(jpqlEsperado, MedioContacto.class)).thenReturn(queryMedioContacto);
        when(queryMedioContacto.setParameter("id", idPersona)).thenReturn(queryMedioContacto);
        when(queryMedioContacto.getResultList()).thenReturn(esperado);

        List<MedioContacto> resultado = dao.findByPersona(idPersona);

        assertEquals(esperado, resultado);
        verify(em).createQuery(jpqlEsperado, MedioContacto.class);
        verify(queryMedioContacto).setParameter("id", idPersona);
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

    // ---- buscarTipoMedioContacto() ----

    @Test
    public void buscarTipoMedioContacto_idNulo_devuelveNullSinConsultar() {
        assertNull(dao.buscarTipoMedioContacto(null));
        verifyNoInteractions(em);
    }

    @Test
    public void buscarTipoMedioContacto_delegaEnFind() {
        UUID id = UUID.randomUUID();
        TipoMedioContacto tipo = new TipoMedioContacto(id);
        when(em.find(TipoMedioContacto.class, id)).thenReturn(tipo);

        assertSame(tipo, dao.buscarTipoMedioContacto(id));
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

    // ---- listarTiposMedioContacto() ----

    @Test
    public void listarTiposMedioContacto_limitaA100YDevuelveResultados() {
        List<TipoMedioContacto> esperado = Arrays.asList(new TipoMedioContacto(UUID.randomUUID()));
        when(em.createNamedQuery("TipoMedioContacto.findAll", TipoMedioContacto.class)).thenReturn(queryTipoMedioContacto);
        when(queryTipoMedioContacto.setMaxResults(100)).thenReturn(queryTipoMedioContacto);
        when(queryTipoMedioContacto.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.listarTiposMedioContacto());
        verify(queryTipoMedioContacto).setMaxResults(100);
    }

    // ---- existeMedioParaPersona() ----

    @Test
    public void existeMedioParaPersona_valorNulo_devuelveFalseSinConsultar() {
        assertFalse(dao.existeMedioParaPersona(UUID.randomUUID(), UUID.randomUUID(), null, null));
        verifyNoInteractions(em);
    }

    @Test
    public void existeMedioParaPersona_valorEnBlanco_devuelveFalseSinConsultar() {
        assertFalse(dao.existeMedioParaPersona(UUID.randomUUID(), UUID.randomUUID(), "   ", null));
        verifyNoInteractions(em);
    }

    @Test
    public void existeMedioParaPersona_hayCoincidencias_devuelveTrue() {
        conteo(1L);

        assertTrue(dao.existeMedioParaPersona(
                UUID.randomUUID(), UUID.randomUUID(), "a@b.com", UUID.randomUUID()));
    }

    @Test
    public void existeMedioParaPersona_sinCoincidencias_devuelveFalse() {
        conteo(0L);

        assertFalse(dao.existeMedioParaPersona(
                UUID.randomUUID(), UUID.randomUUID(), "a@b.com", UUID.randomUUID()));
    }

    @Test
    public void existeMedioParaPersona_normalizaElValorYEnviaTodosLosParametros() {
        UUID persona = UUID.randomUUID();
        UUID tipo = UUID.randomUUID();
        UUID excluir = UUID.randomUUID();
        conteo(0L);

        dao.existeMedioParaPersona(persona, tipo, "  Correo@Mail.COM  ", excluir);

        verify(queryConteo).setParameter("persona", persona);
        verify(queryConteo).setParameter("tipo", tipo);
        verify(queryConteo).setParameter("valor", "correo@mail.com");
        verify(queryConteo).setParameter("excluir", excluir);
    }

    @Test
    public void existeMedioParaPersona_idExcluirNulo_usaUuidCero() {
        conteo(0L);

        dao.existeMedioParaPersona(UUID.randomUUID(), UUID.randomUUID(), "x", null);

        verify(queryConteo).setParameter("excluir", new UUID(0L, 0L));
    }
}