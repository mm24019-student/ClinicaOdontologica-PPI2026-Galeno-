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
import static org.mockito.Mockito.*;

/**
 * Prueba de DocumentoDAO. crear/eliminar/actualizar/buscar/findRange ya
 * quedan cubiertos por DefaultDAOTest (heredados sin cambios); aquí solo se
 * prueba lo propio de esta clase: findByPersona (usa buscarPorPadre con el
 * JPQL de Documento), y los helpers de combo listarPersonas/
 * listarTiposDocumento/buscarPersona/buscarTipoDocumento.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class DocumentoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Documento> queryDocumento;

    @Mock
    private TypedQuery<Persona> queryPersona;

    @Mock
    private TypedQuery<TipoDocumento> queryTipoDocumento;

    @InjectMocks
    private DocumentoDAO dao;

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

    // ---- buscarTipoDocumento() ----

    @Test
    public void buscarTipoDocumento_idNulo_devuelveNullSinConsultar() {
        TipoDocumento resultado = dao.buscarTipoDocumento(null);

        assertNull(resultado);
        verifyNoInteractions(em);
    }

    @Test
    public void buscarTipoDocumento_delegaEnFind() {
        UUID id = UUID.randomUUID();
        TipoDocumento tipo = new TipoDocumento(id);
        when(em.find(TipoDocumento.class, id)).thenReturn(tipo);

        TipoDocumento resultado = dao.buscarTipoDocumento(id);

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

    // ---- listarTiposDocumento() ----

    @Test
    public void listarTiposDocumento_limitaA100YDevuelveResultados() {
        List<TipoDocumento> esperado = Arrays.asList(new TipoDocumento(UUID.randomUUID()));
        when(em.createNamedQuery("TipoDocumento.findAll", TipoDocumento.class)).thenReturn(queryTipoDocumento);
        when(queryTipoDocumento.setMaxResults(100)).thenReturn(queryTipoDocumento);
        when(queryTipoDocumento.getResultList()).thenReturn(esperado);

        List<TipoDocumento> resultado = dao.listarTiposDocumento();

        assertEquals(esperado, resultado);
        verify(queryTipoDocumento).setMaxResults(100);
    }
}