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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de PersonaDAO. La lógica CRUD ya la cubre DefaultDAOTest; aquí solo
 * el EntityManager y la resolución de entidad / NamedQuery "Persona.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class PersonaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Persona> query;

    @InjectMocks
    private PersonaDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findRange_usaLaNamedQueryDePersonaYAplicaElRango() {
        List<Persona> esperado = Arrays.asList(new Persona(UUID.randomUUID()));
        when(em.createNamedQuery("Persona.findAll", Persona.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findRange(0, 100));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void buscar_delegaEnFindConLaClasePersona() {
        UUID id = UUID.randomUUID();
        Persona persona = new Persona(id);
        when(em.find(Persona.class, id)).thenReturn(persona);

        assertSame(persona, dao.buscar(id));
    }

    @Test
    public void crear_delegaEnPersist() {
        Persona persona = new Persona(UUID.randomUUID());

        dao.crear(persona);

        verify(em).persist(persona);
    }
}