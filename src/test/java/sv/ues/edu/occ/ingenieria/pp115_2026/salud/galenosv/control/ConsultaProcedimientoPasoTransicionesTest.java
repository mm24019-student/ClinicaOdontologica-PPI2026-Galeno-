package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoPasoTransicionesTest {

    private static final String JPQL_SIGUIENTES = "SELECT s FROM ProcedimientoPasoSecuencia s "
            + "WHERE s.idProcedimientoPasoReferencia = :id";
    private static final String JPQL_ANTERIORES = "SELECT s FROM ProcedimientoPasoSecuencia s "
            + "WHERE s.idProcedimientoPaso.idProcedimientoPaso = :id "
            + "AND s.idProcedimientoPasoReferencia IS NOT NULL";
    private static final String JPQL_PASO_DE_CONSULTA = "SELECT p FROM ConsultaProcedimientoPaso p "
            + "WHERE p.idConsultaProcedimiento.idConsultaProcedimiento = :idConsulta "
            + "AND p.idProcedimientoPaso.idProcedimientoPaso = :idPaso";

    @Mock
    private EntityManager em;

    @Spy
    @InjectMocks
    private ConsultaProcedimientoPasoService dao;

    private ConsultaProcedimientoPaso pasoActual(ProcedimientoPaso definicion) {
        ConsultaProcedimientoPaso actual = new ConsultaProcedimientoPaso(UUID.randomUUID());
        actual.setEstado("EN_PROCESO");
        actual.setIdProcedimientoPaso(definicion);
        actual.setIdConsultaProcedimiento(new ConsultaProcedimiento(UUID.randomUUID()));
        return actual;
    }

    private void sinPasoRepetido(ProcedimientoPaso definicion, ConsultaProcedimiento cp,
            TypedQuery<ConsultaProcedimientoPaso> query) {
        when(em.createQuery(JPQL_PASO_DE_CONSULTA, ConsultaProcedimientoPaso.class)).thenReturn(query);
        when(query.setParameter("idConsulta", cp.getIdConsultaProcedimiento())).thenReturn(query);
        when(query.setParameter("idPaso", definicion.getIdProcedimientoPaso())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
    }

    @Test
    public void avanzar_conSiguiente_completaElActualYCreaElNuevoEnProceso() {
        UUID id = UUID.randomUUID();
        ProcedimientoPaso definicion = new ProcedimientoPaso(UUID.randomUUID());
        ProcedimientoPaso siguiente = new ProcedimientoPaso(UUID.randomUUID());
        ConsultaProcedimientoPaso actual = pasoActual(definicion);
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        secuencia.setTipoSecuencia("SIGUIENTE");
        secuencia.setIdProcedimientoPaso(siguiente);
        secuencia.setIdProcedimientoPasoReferencia(definicion.getIdProcedimientoPaso());
        TypedQuery<ProcedimientoPasoSecuencia> querySecuencias = org.mockito.Mockito.mock(TypedQuery.class);
        TypedQuery<ConsultaProcedimientoPaso> queryPasos = org.mockito.Mockito.mock(TypedQuery.class);
        doReturn(actual).when(dao).buscar(id);
        doNothing().when(dao).crear(any());
        when(em.createQuery(JPQL_SIGUIENTES, ProcedimientoPasoSecuencia.class)).thenReturn(querySecuencias);
        when(querySecuencias.setParameter("id", definicion.getIdProcedimientoPaso())).thenReturn(querySecuencias);
        when(querySecuencias.getResultList()).thenReturn(List.of(secuencia));
        when(em.merge(actual)).thenReturn(actual);
        sinPasoRepetido(siguiente, actual.getIdConsultaProcedimiento(), queryPasos);

        ConsultaProcedimientoPaso resultado = dao.avanzar(id);

        assertEquals("COMPLETADO", actual.getEstado());
        assertNotNull(actual.getFechaFin());
        ArgumentCaptor<ConsultaProcedimientoPaso> captor = ArgumentCaptor.forClass(ConsultaProcedimientoPaso.class);
        verify(dao).crear(captor.capture());
        assertSame(resultado, captor.getValue());
        assertEquals("EN_PROCESO", resultado.getEstado());
        assertSame(siguiente, resultado.getIdProcedimientoPaso());
        assertSame(actual.getIdConsultaProcedimiento(), resultado.getIdConsultaProcedimiento());
    }

    @Test
    public void avanzar_conPasoFinal_lanzaIllegalStateSinBuscarSecuencias() {
        UUID id = UUID.randomUUID();
        ProcedimientoPaso definicion = new ProcedimientoPaso(UUID.randomUUID());
        definicion.setIndicaFin(true);
        ConsultaProcedimientoPaso actual = pasoActual(definicion);
        doReturn(actual).when(dao).buscar(id);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.avanzar(id));

        assertTrue(ex.getMessage().contains("siguiente"));
        verify(em, never()).createQuery(anyString(), any());
    }

    @Test
    public void retroceder_conAnterior_ponePendienteElActualYActivaElAnterior() {
        UUID id = UUID.randomUUID();
        ProcedimientoPaso definicion = new ProcedimientoPaso(UUID.randomUUID());
        ProcedimientoPaso anterior = new ProcedimientoPaso(UUID.randomUUID());
        ConsultaProcedimientoPaso actual = pasoActual(definicion);
        actual.setFechaFin(new java.util.Date());
        ProcedimientoPasoSecuencia secuencia = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        secuencia.setTipoSecuencia("ANTERIOR");
        secuencia.setIdProcedimientoPaso(definicion);
        secuencia.setIdProcedimientoPasoReferencia(anterior.getIdProcedimientoPaso());
        TypedQuery<ProcedimientoPasoSecuencia> querySecuencias = org.mockito.Mockito.mock(TypedQuery.class);
        TypedQuery<ConsultaProcedimientoPaso> queryPasos = org.mockito.Mockito.mock(TypedQuery.class);
        doReturn(actual).when(dao).buscar(id);
        doNothing().when(dao).crear(any());
        when(em.createQuery(JPQL_ANTERIORES, ProcedimientoPasoSecuencia.class)).thenReturn(querySecuencias);
        when(querySecuencias.setParameter("id", definicion.getIdProcedimientoPaso())).thenReturn(querySecuencias);
        when(querySecuencias.getResultList()).thenReturn(List.of(secuencia));
        when(em.find(ProcedimientoPaso.class, anterior.getIdProcedimientoPaso())).thenReturn(anterior);
        when(em.merge(actual)).thenReturn(actual);
        sinPasoRepetido(anterior, actual.getIdConsultaProcedimiento(), queryPasos);

        ConsultaProcedimientoPaso resultado = dao.retroceder(id);

        assertEquals("PENDIENTE", actual.getEstado());
        assertNull(actual.getFechaFin());
        ArgumentCaptor<ConsultaProcedimientoPaso> captor = ArgumentCaptor.forClass(ConsultaProcedimientoPaso.class);
        verify(dao).crear(captor.capture());
        assertSame(resultado, captor.getValue());
        assertEquals("EN_PROCESO", resultado.getEstado());
        assertSame(anterior, resultado.getIdProcedimientoPaso());
    }

    @Test
    public void retroceder_sinSecuenciaHaciaAtras_lanzaIllegalState() {
        UUID id = UUID.randomUUID();
        ProcedimientoPaso definicion = new ProcedimientoPaso(UUID.randomUUID());
        ConsultaProcedimientoPaso actual = pasoActual(definicion);
        TypedQuery<ProcedimientoPasoSecuencia> querySecuencias = org.mockito.Mockito.mock(TypedQuery.class);
        doReturn(actual).when(dao).buscar(id);
        when(em.createQuery(JPQL_ANTERIORES, ProcedimientoPasoSecuencia.class)).thenReturn(querySecuencias);
        when(querySecuencias.setParameter("id", definicion.getIdProcedimientoPaso())).thenReturn(querySecuencias);
        when(querySecuencias.getResultList()).thenReturn(Collections.emptyList());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.retroceder(id));

        assertTrue(ex.getMessage().contains("anterior"));
        assertEquals("EN_PROCESO", actual.getEstado());
    }
}
