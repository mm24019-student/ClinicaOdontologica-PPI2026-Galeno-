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
import static org.mockito.Mockito.*;

/**
 * Prueba de MedioContactoDAO. crear/eliminar/actualizar/buscar/findRange ya
 * quedan cubiertos por DefaultDAOTest (heredados sin cambios); aquí solo se
 * prueba lo propio de esta clase: findByPersona (usa buscarPorPadre con el
 * JPQL de MedioContacto), y los helpers de combo listarPersonas/
 * listarTiposMedioContacto/buscarPersona/buscarTipoMedioContacto.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class MedioContactoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<MedioContacto> queryMedioContacto;

    @Mock
    private TypedQuery<Persona> queryPersona;

    @Mock
    private TypedQuery<TipoMedioContacto> queryTipoMedioContacto;

    @InjectMocks
    private MedioContactoDAO dao;

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
        Persona resultado = dao.buscarPersona(null);

        assertNull(resultado);
        verifyNoInteractions(em);
    }

    @Test
    public void buscarPersona_delegaEnFind() {
        UUID id = UUID.randomUUID();
        Persona persona = new Persona(id);
        when(em.find(Persona.class, id)).thenReturn(persona);

        Persona resultado = dao.buscarPersona(id);

        assertSame(persona, resultado);
    }

    // ---- buscarTipoMedioContacto() ----

    @Test
    public void buscarTipoMedioContacto_idNulo_devuelveNullSinConsultar() {
        TipoMedioContacto resultado = dao.buscarTipoMedioContacto(null);

        assertNull(resultado);
        verifyNoInteractions(em);
    }

    @Test
    public void buscarTipoMedioContacto_delegaEnFind() {
        UUID id = UUID.randomUUID();
        TipoMedioContacto tipo = new TipoMedioContacto(id);
        when(em.find(TipoMedioContacto.class, id)).thenReturn(tipo);

        TipoMedioContacto resultado = dao.buscarTipoMedioContacto(id);

        assertSame(tipo, resultado);
    }

    // ---- listarPersonas() ----

    @Test
    public void listarPersonas_limitaA100YDevuelveResultados() {
        List<Persona> esperado = Arrays.asList(new Persona(UUID.randomUUID()));
        when(em.createNamedQuery("Persona.findAll", Persona.class)).thenReturn(queryPersona);
        when(queryPersona.setMaxResults(100)).thenReturn(queryPersona);
        when(queryPersona.getResultList()).thenReturn(esperado);

        List<Persona> resultado = dao.listarPersonas();

        assertEquals(esperado, resultado);
        verify(queryPersona).setMaxResults(100);
    }

    // ---- listarTiposMedioContacto() ----

    @Test
    public void listarTiposMedioContacto_limitaA100YDevuelveResultados() {
        List<TipoMedioContacto> esperado = Arrays.asList(new TipoMedioContacto(UUID.randomUUID()));
        when(em.createNamedQuery("TipoMedioContacto.findAll", TipoMedioContacto.class)).thenReturn(queryTipoMedioContacto);
        when(queryTipoMedioContacto.setMaxResults(100)).thenReturn(queryTipoMedioContacto);
        when(queryTipoMedioContacto.getResultList()).thenReturn(esperado);

        List<TipoMedioContacto> resultado = dao.listarTiposMedioContacto();

        assertEquals(esperado, resultado);
        verify(queryTipoMedioContacto).setMaxResults(100);
    }
}