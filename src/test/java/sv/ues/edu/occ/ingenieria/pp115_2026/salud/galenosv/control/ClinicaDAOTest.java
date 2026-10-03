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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de ClinicaDAO. La lógica CRUD ya la cubre DefaultDAOTest; aquí solo
 * se prueba que entregue el EntityManager y que resuelva su entidad y su
 * NamedQuery "Clinica.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class ClinicaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Clinica> query;

    @InjectMocks
    private ClinicaDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findRange_usaLaNamedQueryDeClinicaYAplicaElRango() {
        List<Clinica> esperado = Arrays.asList(new Clinica(UUID.randomUUID()));
        when(em.createNamedQuery("Clinica.findAll", Clinica.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findRange(0, 100));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void buscar_delegaEnFindConLaClaseClinica() {
        UUID id = UUID.randomUUID();
        Clinica clinica = new Clinica(id);
        when(em.find(Clinica.class, id)).thenReturn(clinica);

        assertSame(clinica, dao.buscar(id));
    }

    @Test
    public void crear_delegaEnPersist() {
        Clinica clinica = new Clinica(UUID.randomUUID());

        dao.crear(clinica);

        verify(em).persist(clinica);
    }
}