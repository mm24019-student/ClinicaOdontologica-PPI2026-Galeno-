package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
/**
 * Prueba de ProcedimientoDAO. crear/eliminar/actualizar/buscar ya quedan
 * cubiertos por DefaultDAOTest (heredados sin cambios); aquí solo se prueba
 * que el DAO entregue el EntityManager y que, al heredar de DefaultDAO<Procedimiento>,
 * resuelva bien su entidad y su NamedQuery "Procedimiento.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class ProcedimientoDAOTest {
 
    @Mock
    private EntityManager em;
 
    @Mock
    private TypedQuery<Procedimiento> query;
 
    @InjectMocks
    private ProcedimientoDAO dao;
 
    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }
 
    @Test
    public void findRange_usaLaNamedQueryDeProcedimientoYAplicaElRango() {
        List<Procedimiento> esperado = Arrays.asList(new Procedimiento(UUID.randomUUID()));
        when(em.createNamedQuery("Procedimiento.findAll", Procedimiento.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);
 
        List<Procedimiento> resultado = dao.findRange(5, 20);
 
        assertEquals(esperado, resultado);
        verify(query).setFirstResult(5);
        verify(query).setMaxResults(20);
    }
 
    @Test
    public void buscar_delegaEnFindConLaClaseProcedimiento() {
        UUID id = UUID.randomUUID();
        Procedimiento procedimiento = new Procedimiento(id);
        when(em.find(Procedimiento.class, id)).thenReturn(procedimiento);
 
        assertSame(procedimiento, dao.buscar(id));
    }
}
 
