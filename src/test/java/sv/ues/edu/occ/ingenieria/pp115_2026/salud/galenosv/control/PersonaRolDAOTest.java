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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de PersonaRolDAO. crear/eliminar/actualizar/buscar/findRange ya
 * quedan cubiertos por DefaultDAOTest (heredados sin cambios); aquí solo se
 * prueba lo propio de esta clase: findByPersona (usa buscarPorPadre con el
 * JPQL de PersonaRol), y los helpers de combo listarPersonas/listarRoles/
 * listarClinicas/buscarPersona/buscarRol/buscarClinica.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class PersonaRolDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<PersonaRol> queryPersonaRol;

    @Mock
    private TypedQuery<Persona> queryPersona;

    @Mock
    private TypedQuery<Rol> queryRol;

    @Mock
    private TypedQuery<Clinica> queryClinica;

    @InjectMocks
    private PersonaRolDAO dao;

    // ---- findByPersona() ----

    @Test
    public void findByPersona_armaQueryConIdDePersonaYDevuelveResultados() {
        UUID idPersona = UUID.randomUUID();
        List<PersonaRol> esperado = Arrays.asList(
                new PersonaRol(UUID.randomUUID()), new PersonaRol(UUID.randomUUID()));
        String jpqlEsperado = "SELECT pr FROM PersonaRol pr WHERE pr.idPersona.idPersona = :id";

        when(em.createQuery(jpqlEsperado, PersonaRol.class)).thenReturn(queryPersonaRol);
        when(queryPersonaRol.setParameter("id", idPersona)).thenReturn(queryPersonaRol);
        when(queryPersonaRol.getResultList()).thenReturn(esperado);

        List<PersonaRol> resultado = dao.findByPersona(idPersona);

        assertEquals(esperado, resultado);
        verify(em).createQuery(jpqlEsperado, PersonaRol.class);
        verify(queryPersonaRol).setParameter("id", idPersona);
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

    // ---- buscarRol() ----

    @Test
    public void buscarRol_idNulo_devuelveNullSinConsultar() {
        assertNull(dao.buscarRol(null));
        verifyNoInteractions(em);
    }

    @Test
    public void buscarRol_delegaEnFind() {
        UUID id = UUID.randomUUID();
        Rol rol = new Rol(id);
        when(em.find(Rol.class, id)).thenReturn(rol);

        assertSame(rol, dao.buscarRol(id));
    }

    // ---- buscarClinica() ----

    @Test
    public void buscarClinica_idNulo_devuelveNullSinConsultar() {
        assertNull(dao.buscarClinica(null));
        verifyNoInteractions(em);
    }

    @Test
    public void buscarClinica_delegaEnFind() {
        UUID id = UUID.randomUUID();
        Clinica clinica = new Clinica(id);
        when(em.find(Clinica.class, id)).thenReturn(clinica);

        assertSame(clinica, dao.buscarClinica(id));
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

    // ---- listarRoles() ----

    @Test
    public void listarRoles_limitaA100YDevuelveResultados() {
        List<Rol> esperado = Arrays.asList(new Rol(UUID.randomUUID()));
        when(em.createNamedQuery("Rol.findAll", Rol.class)).thenReturn(queryRol);
        when(queryRol.setMaxResults(100)).thenReturn(queryRol);
        when(queryRol.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.listarRoles());
        verify(queryRol).setMaxResults(100);
    }

    // ---- listarClinicas() ----

    @Test
    public void listarClinicas_limitaA100YDevuelveResultados() {
        List<Clinica> esperado = Arrays.asList(new Clinica(UUID.randomUUID()));
        when(em.createNamedQuery("Clinica.findAll", Clinica.class)).thenReturn(queryClinica);
        when(queryClinica.setMaxResults(100)).thenReturn(queryClinica);
        when(queryClinica.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.listarClinicas());
        verify(queryClinica).setMaxResults(100);
    }
}