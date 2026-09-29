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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author antonio
 */
/**
 * Prueba de ConsultaDAO. crear/eliminar/actualizar ya quedan cubiertos por
 * DefaultDAOTest (heredados sin cambios); aquí solo se prueba que el DAO
 * entregue el EntityManager y que, al heredar de DefaultDAO<Consulta>,
 * resuelva bien su entidad y su NamedQuery "Consulta.findAll".
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaDAOTest {
 
    @Mock
    private EntityManager em;
 
    @Mock
    private TypedQuery<Consulta> query;
 
    @InjectMocks
    private ConsultaDAO dao;
 
    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }
 
    @Test
    public void findRange_usaLaNamedQueryDeConsultaYAplicaElRango() {
        List<Consulta> esperado = Arrays.asList(new Consulta(UUID.randomUUID()));
        when(em.createNamedQuery("Consulta.findAll", Consulta.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);
 
        List<Consulta> resultado = dao.findRange(0, 100);
 
        assertEquals(esperado, resultado);
        verify(query).setFirstResult(0);
        verify(query).setMaxResults(100);
    }
 
    @Test
    public void buscar_delegaEnFindConLaClaseConsulta() {
        UUID id = UUID.randomUUID();
        Consulta consulta = new Consulta(id);
        when(em.find(Consulta.class, id)).thenReturn(consulta);
 
        assertSame(consulta, dao.buscar(id));
    }
    
}