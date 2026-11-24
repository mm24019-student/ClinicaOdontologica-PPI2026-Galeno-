package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
/**
 * Prueba de ConsultaProcedimientoPasoService. Lo genérico ya queda cubierto por
 * DefaultServiceTest; aquí solo el EntityManager, findByConsultaProcedimiento y
 * el eliminar() propio, que antes de borrar verifica que el paso no tenga
 * órdenes de examen asociadas (FK desde orden_examen).
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoPasoServiceTest {
 
    private static final String JPQL_POR_CONSULTA_PROCEDIMIENTO
            = "SELECT DISTINCT e FROM ConsultaProcedimientoPaso e "
            + "JOIN FETCH e.idConsultaProcedimiento cp "
            + "LEFT JOIN FETCH e.idProcedimientoPaso pp "
            + "LEFT JOIN FETCH pp.idProcedimiento "
            + "LEFT JOIN FETCH pp.idRol "
            + "LEFT JOIN FETCH e.idPersonaRol pr "
            + "LEFT JOIN FETCH pr.idPersona "
            + "LEFT JOIN FETCH pr.idRol "
            + "LEFT JOIN FETCH pr.idClinica "
            + "WHERE cp.idConsultaProcedimiento = :id "
            + "ORDER BY e.fechaInicio";
 
    private static final String JPQL_CONTAR_ORDENES
            = "SELECT COUNT(o) FROM OrdenExamen o WHERE o.idConsultaProcedimientoPaso.idConsultaProcedimientoPaso = :id";

    private static final String JPQL_ANTERIORES
            = "SELECT s FROM ProcedimientoPasoSecuencia s "
            + "WHERE s.idProcedimientoPaso.idProcedimientoPaso = :id "
            + "AND s.idProcedimientoPasoReferencia IS NOT NULL";

    private static final String JPQL_PASO_DE_CONSULTA
            = "SELECT p FROM ConsultaProcedimientoPaso p "
            + "WHERE p.idConsultaProcedimiento.idConsultaProcedimiento = :idConsulta "
            + "AND p.idProcedimientoPaso.idProcedimientoPaso = :idPaso";
 
    @Mock
    private EntityManager em;
 
    @Mock
    private TypedQuery<ConsultaProcedimientoPaso> query;
 
    @Mock
    private TypedQuery<Long> queryConteo;

    @Mock
    private TypedQuery<ProcedimientoPasoSecuencia> querySecuencias;

    @Mock
    private TypedQuery<ConsultaProcedimientoPaso> queryPasoConsulta;

    @InjectMocks

    private ConsultaProcedimientoPasoService dao;
 
    private void ordenesAsociadas(UUID idPaso, Long total) {
        when(em.createQuery(JPQL_CONTAR_ORDENES, Long.class)).thenReturn(queryConteo);
        when(queryConteo.setParameter("id", idPaso)).thenReturn(queryConteo);
        when(queryConteo.getSingleResult()).thenReturn(total);
    }
 
    // ---- getEntityManager() ----
 
    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }
 
    // ---- findByConsultaProcedimiento() ----
 
    @Test
    public void findByConsultaProcedimiento_armaQueryConIdDelPadreYDevuelveResultados() {
        UUID idPadre = UUID.randomUUID();
        List<ConsultaProcedimientoPaso> esperado = Arrays.asList(
                new ConsultaProcedimientoPaso(UUID.randomUUID()),
                new ConsultaProcedimientoPaso(UUID.randomUUID()));
        when(em.createQuery(JPQL_POR_CONSULTA_PROCEDIMIENTO, ConsultaProcedimientoPaso.class)).thenReturn(query);
        when(query.setParameter("id", idPadre)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);
 
        List<ConsultaProcedimientoPaso> resultado = dao.findByConsultaProcedimiento(idPadre);
 
        assertEquals(esperado, resultado);
        verify(em).createQuery(JPQL_POR_CONSULTA_PROCEDIMIENTO, ConsultaProcedimientoPaso.class);
        verify(query).setParameter("id", idPadre);
    }
 
    @Test
    public void findByConsultaProcedimiento_sinPasos_devuelveListaVacia() {
        UUID idPadre = UUID.randomUUID();
        when(em.createQuery(JPQL_POR_CONSULTA_PROCEDIMIENTO, ConsultaProcedimientoPaso.class)).thenReturn(query);
        when(query.setParameter("id", idPadre)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
 
        assertTrue(dao.findByConsultaProcedimiento(idPadre).isEmpty());
    }
 
    @Test
    public void retroceder_conMotivo_agregaElMotivoAlPasoAnterior() {
        UUID idActual = UUID.randomUUID();
        UUID idDefinicionActual = UUID.randomUUID();
        UUID idDefinicionAnterior = UUID.randomUUID();
        UUID idConsultaProcedimiento = UUID.randomUUID();
        UUID idPasoAnterior = UUID.randomUUID();
        ConsultaProcedimiento consultaProcedimiento = new ConsultaProcedimiento(idConsultaProcedimiento);
        ProcedimientoPaso definicionActual = new ProcedimientoPaso(idDefinicionActual);
        ProcedimientoPaso definicionAnterior = new ProcedimientoPaso(idDefinicionAnterior);
        ConsultaProcedimientoPaso actual = new ConsultaProcedimientoPaso(idActual);
        actual.setIdConsultaProcedimiento(consultaProcedimiento);
        actual.setIdProcedimientoPaso(definicionActual);
        actual.setEstado("EN_PROCESO");
        actual.setValor("Nota del paso actual");
        ConsultaProcedimientoPaso anterior = new ConsultaProcedimientoPaso(idPasoAnterior);
        anterior.setIdConsultaProcedimiento(consultaProcedimiento);
        anterior.setIdProcedimientoPaso(definicionAnterior);
        anterior.setValor("Nota anterior");
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia();
        secuencia.setTipoSecuencia("ANTERIOR");
        secuencia.setIdProcedimientoPasoReferencia(idDefinicionAnterior);
        String motivo = "Devolución desde Paso B: Falta información";

        when(em.find(ConsultaProcedimientoPaso.class, idActual)).thenReturn(actual);
        when(em.find(ConsultaProcedimientoPaso.class, idPasoAnterior)).thenReturn(anterior);
        when(em.createQuery(JPQL_ANTERIORES, ProcedimientoPasoSecuencia.class)).thenReturn(querySecuencias);
        when(querySecuencias.setParameter("id", idDefinicionActual)).thenReturn(querySecuencias);
        when(querySecuencias.getResultList()).thenReturn(Collections.singletonList(secuencia));
        when(em.find(ProcedimientoPaso.class, idDefinicionAnterior)).thenReturn(definicionAnterior);
        when(em.createQuery(JPQL_PASO_DE_CONSULTA, ConsultaProcedimientoPaso.class)).thenReturn(queryPasoConsulta);
        when(queryPasoConsulta.setParameter("idConsulta", idConsultaProcedimiento)).thenReturn(queryPasoConsulta);
        when(queryPasoConsulta.setParameter("idPaso", idDefinicionAnterior)).thenReturn(queryPasoConsulta);
        when(queryPasoConsulta.getResultList()).thenReturn(Collections.singletonList(anterior));
        when(em.merge(any(ConsultaProcedimientoPaso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaProcedimientoPaso resultado = dao.retroceder(idActual, null, motivo);

        assertSame(anterior, resultado);
        assertEquals("PENDIENTE", actual.getEstado());
        assertNull(actual.getFechaFin());
        assertEquals("EN_PROCESO", resultado.getEstado());
        assertEquals("Nota anterior\n" + motivo, resultado.getValor());
    }

    // ---- eliminar() ----
 
    @Test
    public void eliminar_idNulo_lanzaIllegalArgumentSinConsultarLaBaseDeDatos() {
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(null));
 
        verifyNoInteractions(em);
    }
 
    @Test
    public void eliminar_conOrdenesDeExamen_lanzaIllegalStateConMensajeDeNegocioYNoBorra() {
        UUID idPaso = UUID.randomUUID();
        ordenesAsociadas(idPaso, 2L);
 
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.eliminar(idPaso));
 
        assertTrue(ex.getMessage().contains("órdenes de examen"));
        verify(em, never()).find(any(), any());
        verify(em, never()).remove(any());
    }
 
    @Test
    public void eliminar_sinOrdenesDeExamen_borraElPaso() {
        UUID idPaso = UUID.randomUUID();
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(idPaso);
        ordenesAsociadas(idPaso, 0L);
        when(em.find(ConsultaProcedimientoPaso.class, idPaso)).thenReturn(paso);
 
        dao.eliminar(idPaso);
 
        verify(em).remove(paso);
    }
 
    @Test
    public void eliminar_conteoNulo_seTrataComoSinOrdenesYBorra() {
        UUID idPaso = UUID.randomUUID();
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(idPaso);
        ordenesAsociadas(idPaso, null);
        when(em.find(ConsultaProcedimientoPaso.class, idPaso)).thenReturn(paso);
 
        dao.eliminar(idPaso);
 
        verify(em).remove(paso);
    }
 
    @Test
    public void eliminar_sinOrdenesPeroPasoInexistente_lanzaIllegalArgument() {
        UUID idPaso = UUID.randomUUID();
        ordenesAsociadas(idPaso, 0L);
        when(em.find(ConsultaProcedimientoPaso.class, idPaso)).thenReturn(null);
 
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(idPaso));
 
        verify(em, never()).remove(any());
    }
}
