package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Prueba de ConsultaDAO. crear/eliminar/actualizar ya quedan cubiertos por
 * DefaultDAOTest; aquí el EntityManager, "Consulta.findAll" y
 * buscarConFiltro(), que arma el JPQL según los filtros que vengan no nulos.
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Consulta> query;

    @InjectMocks
    private ConsultaDAO dao;

    private String jpqlGenerado() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(captor.capture(), eq(Consulta.class));
        return captor.getValue();
    }

    // ---- genéricos ----

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

    // ---- buscarConFiltro() ----

    @Test
    public void buscarConFiltro_sinFiltros_noAgregaWhereNiParametros() {
        List<Consulta> esperado = Arrays.asList(new Consulta(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<Consulta> resultado = dao.buscarConFiltro(null, null, null, 50);

        assertEquals(esperado, resultado);
        String jpql = jpqlGenerado();
        assertFalse(jpql.contains("WHERE"));
        assertTrue(jpql.endsWith("ORDER BY c.fechaInicio DESC"));
        verify(query, never()).setParameter(anyString(), any());
        verify(query).setMaxResults(50);
    }

    @Test
    public void buscarConFiltro_traePersonaRolYClinicaConJoinFetch() {
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        dao.buscarConFiltro(null, null, null, 10);

        String jpql = jpqlGenerado();
        assertTrue(jpql.contains("LEFT JOIN FETCH c.idPersonaRol pr"));
        assertTrue(jpql.contains("LEFT JOIN FETCH pr.idPersona"));
        assertTrue(jpql.contains("LEFT JOIN FETCH pr.idRol"));
        assertTrue(jpql.contains("LEFT JOIN FETCH pr.idClinica"));
    }

    @Test
    public void buscarConFiltro_soloDesde_filtraPorFechaInicioMayorOIgual() {
        Date desde = new Date(1_000L);
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        dao.buscarConFiltro(desde, null, null, 10);

        String jpql = jpqlGenerado();
        assertTrue(jpql.contains("WHERE c.fechaInicio >= :desde ORDER BY"));
        assertFalse(jpql.contains(":hasta"));
        assertFalse(jpql.contains(":idClinica"));
        verify(query).setParameter("desde", desde);
        verify(query, times(1)).setParameter(anyString(), any());
    }

    @Test
    public void buscarConFiltro_soloHasta_filtraPorFechaInicioMenorExclusivo() {
        Date hasta = new Date(2_000L);
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        dao.buscarConFiltro(null, hasta, null, 10);

        assertTrue(jpqlGenerado().contains("WHERE c.fechaInicio < :hasta ORDER BY"));
        verify(query).setParameter("hasta", hasta);
        verify(query, times(1)).setParameter(anyString(), any());
    }

    @Test
    public void buscarConFiltro_soloClinica_filtraPorIdDeClinica() {
        UUID idClinica = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        dao.buscarConFiltro(null, null, idClinica, 10);

        assertTrue(jpqlGenerado().contains("WHERE pr.idClinica.idClinica = :idClinica ORDER BY"));
        verify(query).setParameter("idClinica", idClinica);
        verify(query, times(1)).setParameter(anyString(), any());
    }

    @Test
    public void buscarConFiltro_todosLosFiltros_losCombinaConAndYEnviaTodosLosParametros() {
        Date desde = new Date(1_000L);
        Date hasta = new Date(2_000L);
        UUID idClinica = UUID.randomUUID();
        when(em.createQuery(anyString(), eq(Consulta.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        dao.buscarConFiltro(desde, hasta, idClinica, 25);

        assertTrue(jpqlGenerado().contains(
                "WHERE c.fechaInicio >= :desde AND c.fechaInicio < :hasta "
                + "AND pr.idClinica.idClinica = :idClinica ORDER BY c.fechaInicio DESC"));
        verify(query).setParameter("desde", desde);
        verify(query).setParameter("hasta", hasta);
        verify(query).setParameter("idClinica", idClinica);
        verify(query).setMaxResults(25);
    }
}