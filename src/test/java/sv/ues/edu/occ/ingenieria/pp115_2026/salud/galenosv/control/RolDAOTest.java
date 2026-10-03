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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de RolDAO. La lógica CRUD ya la cubre DefaultDAOTest; aquí solo el
 * EntityManager y la resolución de entidad / NamedQuery "Rol.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class RolDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Rol> query;

    @InjectMocks
    private RolDAO dao;

    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findRange_usaLaNamedQueryDeRolYAplicaElRango() {
        List<Rol> esperado = Arrays.asList(new Rol(UUID.randomUUID()));
        when(em.createNamedQuery("Rol.findAll", Rol.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        assertEquals(esperado, dao.findRange(0, 100));
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }

    @Test
    public void buscar_delegaEnFindConLaClaseRol() {
        UUID id = UUID.randomUUID();
        Rol rol = new Rol(id);
        when(em.find(Rol.class, id)).thenReturn(rol);

        assertSame(rol, dao.buscar(id));
    }

    @Test
    public void crear_delegaEnPersist() {
        Rol rol = new Rol(UUID.randomUUID());

        dao.crear(rol);

        verify(em).persist(rol);
    }
}