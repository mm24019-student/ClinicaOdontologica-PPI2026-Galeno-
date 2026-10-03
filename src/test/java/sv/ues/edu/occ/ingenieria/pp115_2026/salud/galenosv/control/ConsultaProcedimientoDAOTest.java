package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Prueba de ConsultaProcedimientoDAO. Lo genérico (persist, find, merge...) ya
 * queda cubierto por DefaultDAOTest; aquí solo lo propio: findByConsulta, la
 * generación automática de pasos al crear (qué pasos se crean, quién los
 * atiende y cuándo se cancela todo) y el borrado en cascada manual.
 *
 * Los queries se identifican por un trozo de su JPQL (contains) y no por el
 * texto completo, para que el test no se rompa si se cambia el ORDER BY.
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoDAOTest {

    private static final String JPQL_POR_CONSULTA
            = "SELECT e FROM ConsultaProcedimiento e WHERE e.idConsulta.idConsulta = :id";

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ConsultaProcedimiento> query;
    @Mock
    private TypedQuery<ProcedimientoPaso> qPasosDefinidos;
    @Mock
    private TypedQuery<UUID> qDependientes;
    @Mock
    private TypedQuery<PersonaRol> qResponsables;
    @Mock
    private TypedQuery<Long> qConteoOrdenes;
    @Mock
    private Query qBorrarPasos;

    @InjectMocks
    private ConsultaProcedimientoDAO dao;

    // ------------------------------------------------------------------
    // Fábricas de datos
    // ------------------------------------------------------------------
    private Clinica clinica(String nombre) {
        Clinica c = new Clinica(UUID.randomUUID());
        c.setNombre(nombre);
        return c;
    }

    private PersonaRol personaRol(Clinica clinica) {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdClinica(clinica);
        return pr;
    }

    private Rol rol(String nombre) {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombre);
        return r;
    }

    private ProcedimientoPaso pasoDefinido(String nombre, Rol rol) {
        ProcedimientoPaso p = new ProcedimientoPaso(UUID.randomUUID());
        p.setNombre(nombre);
        p.setIdRol(rol);
        return p;
    }

    // Procedimiento de consulta ligado a una consulta de la clínica indicada.
    private ConsultaProcedimiento procedimientoDeConsulta(Consulta consulta) {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        cp.setIdProcedimiento(UUID.randomUUID());
        cp.setFechaInicio(new Date(1_000_000L));
        cp.setFechaFin(new Date(2_000_000L));
        cp.setIdConsulta(consulta);
        return cp;
    }

    private Consulta consultaDeClinica(Clinica clinica) {
        Consulta c = new Consulta(UUID.randomUUID());
        c.setIdPersonaRol(personaRol(clinica));
        return c;
    }

    // ------------------------------------------------------------------
    // Stubs de los queries de generarPasos() (se usan solo donde hacen falta:
    // con Mockito estricto, un stub sin usar hace fallar la prueba).
    // ------------------------------------------------------------------
    private void stubPasosDefinidos(List<ProcedimientoPaso> definidos) {
        when(em.createQuery(contains("FROM ProcedimientoPaso pp"), eq(ProcedimientoPaso.class)))
                .thenReturn(qPasosDefinidos);
        when(qPasosDefinidos.setParameter(anyString(), any())).thenReturn(qPasosDefinidos);
        when(qPasosDefinidos.getResultList()).thenReturn(definidos);
    }

    private void stubDependientes(List<UUID> dependientes) {
        when(em.createQuery(contains("FROM ProcedimientoPasoSecuencia s"), eq(UUID.class)))
                .thenReturn(qDependientes);
        when(qDependientes.setParameter(anyString(), any())).thenReturn(qDependientes);
        when(qDependientes.getResultList()).thenReturn(dependientes);
    }

    private void stubResponsables(List<PersonaRol> candidatos) {
        when(em.createQuery(contains("FROM PersonaRol pr"), eq(PersonaRol.class)))
                .thenReturn(qResponsables);
        when(qResponsables.setParameter(anyString(), any())).thenReturn(qResponsables);
        when(qResponsables.setMaxResults(50)).thenReturn(qResponsables);
        when(qResponsables.getResultList()).thenReturn(candidatos);
    }

    // Persiste capturados: el primero es el procedimiento, el resto son pasos.
    private List<Object> persistidos(int veces) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(em, times(veces)).persist(captor.capture());
        return captor.getAllValues();
    }

    // ---- getEntityManager() / findByConsulta() ----
    @Test
    public void getEntityManager_devuelveElEntityManagerInyectado() {
        assertSame(em, dao.getEntityManager());
    }

    @Test
    public void findByConsulta_armaQueryConIdDeLaConsultaYDevuelveResultados() {
        UUID idConsulta = UUID.randomUUID();
        List<ConsultaProcedimiento> esperado = Arrays.asList(
                new ConsultaProcedimiento(UUID.randomUUID()),
                new ConsultaProcedimiento(UUID.randomUUID()));
        when(em.createQuery(JPQL_POR_CONSULTA, ConsultaProcedimiento.class)).thenReturn(query);
        when(query.setParameter("id", idConsulta)).thenReturn(query);
        when(query.getResultList()).thenReturn(esperado);

        List<ConsultaProcedimiento> resultado = dao.findByConsulta(idConsulta);

        assertEquals(esperado, resultado);
        verify(em).createQuery(JPQL_POR_CONSULTA, ConsultaProcedimiento.class);
        verify(query).setParameter("id", idConsulta);
    }

    @Test
    public void findByConsulta_sinProcedimientos_devuelveListaVacia() {
        UUID idConsulta = UUID.randomUUID();
        when(em.createQuery(JPQL_POR_CONSULTA, ConsultaProcedimiento.class)).thenReturn(query);
        when(query.setParameter("id", idConsulta)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertTrue(dao.findByConsulta(idConsulta).isEmpty());
    }

    // ---- crear(): casos que no generan pasos ----
    @Test
    public void crear_registroNulo_lanzaIllegalArgumentYNoTocaLaBase() {
        assertThrows(IllegalArgumentException.class, () -> dao.crear(null));

        verifyNoInteractions(em);
    }

    @Test
    public void crear_sinProcedimientoElegido_soloPersisteElRegistroYNoGeneraPasos() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());   // idProcedimiento == null

        dao.crear(cp);

        verify(em).persist(cp);
        verifyNoMoreInteractions(em);
    }

    @Test
    public void crear_elProcedimientoNoTienePasosDefinidos_soloPersisteElRegistro() {
        ConsultaProcedimiento cp = procedimientoDeConsulta(null);
        stubPasosDefinidos(Collections.emptyList());
        stubDependientes(Collections.emptyList());

        dao.crear(cp);

        assertEquals(1, persistidos(1).size());
    }

    // ---- crear(): generación de pasos ----
    @Test
    public void crear_generaUnPasoPorCadaPasoDefinidoExceptoLosQueDependenDeOtro() {
        Clinica clinicaA = clinica("Clinica A");
        Consulta consulta = consultaDeClinica(clinicaA);
        ConsultaProcedimiento cp = procedimientoDeConsulta(consulta);
        Rol odontologo = rol("Odontologo");
        ProcedimientoPaso inicial = pasoDefinido("A inicial", odontologo);
        ProcedimientoPaso dependiente = pasoDefinido("B dependiente", odontologo);
        ProcedimientoPaso otroInicial = pasoDefinido("C otro inicial", odontologo);
        PersonaRol responsable = personaRol(clinicaA);
        stubPasosDefinidos(Arrays.asList(inicial, dependiente, otroInicial));
        stubDependientes(Arrays.asList(dependiente.getIdProcedimientoPaso()));
        when(em.find(Consulta.class, consulta.getIdConsulta())).thenReturn(consulta);
        stubResponsables(Arrays.asList(responsable));

        dao.crear(cp);

        List<Object> guardados = persistidos(3);
        assertSame(cp, guardados.get(0));
        ConsultaProcedimientoPaso paso1 = (ConsultaProcedimientoPaso) guardados.get(1);
        ConsultaProcedimientoPaso paso2 = (ConsultaProcedimientoPaso) guardados.get(2);
        assertSame(inicial, paso1.getIdProcedimientoPaso());
        assertSame(otroInicial, paso2.getIdProcedimientoPaso());
    }

    @Test
    public void crear_cadaPasoNaceConEstadoPendienteFechasDelProcedimientoYSuResponsable() {
        Clinica clinicaA = clinica("Clinica A");
        Consulta consulta = consultaDeClinica(clinicaA);
        ConsultaProcedimiento cp = procedimientoDeConsulta(consulta);
        ProcedimientoPaso definido = pasoDefinido("Revision", rol("Odontologo"));
        PersonaRol responsable = personaRol(clinicaA);
        stubPasosDefinidos(Arrays.asList(definido));
        stubDependientes(Collections.emptyList());
        when(em.find(Consulta.class, consulta.getIdConsulta())).thenReturn(consulta);
        stubResponsables(Arrays.asList(responsable));

        dao.crear(cp);

        ConsultaProcedimientoPaso paso = (ConsultaProcedimientoPaso) persistidos(2).get(1);
        assertNotNull(paso.getIdConsultaProcedimientoPaso());
        assertEquals("PENDIENTE", paso.getEstado());
        assertSame(cp, paso.getIdConsultaProcedimiento());
        assertEquals(cp.getFechaInicio(), paso.getFechaInicio());
        assertEquals(cp.getFechaFin(), paso.getFechaFin());
        assertSame(responsable, paso.getIdPersonaRol());
    }

    @Test
    public void crear_conVariosCandidatos_prefiereAlDeLaMismaClinicaDeLaConsulta() {
        Clinica clinicaA = clinica("Clinica A");
        Clinica clinicaB = clinica("Clinica B");
        Consulta consulta = consultaDeClinica(clinicaA);
        ConsultaProcedimiento cp = procedimientoDeConsulta(consulta);
        PersonaRol sinClinica = personaRol(null);
        PersonaRol deOtraClinica = personaRol(clinicaB);
        PersonaRol deLaMismaClinica = personaRol(clinicaA);
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", rol("Odontologo"))));
        stubDependientes(Collections.emptyList());
        when(em.find(Consulta.class, consulta.getIdConsulta())).thenReturn(consulta);
        stubResponsables(Arrays.asList(sinClinica, deOtraClinica, deLaMismaClinica));

        dao.crear(cp);

        ConsultaProcedimientoPaso paso = (ConsultaProcedimientoPaso) persistidos(2).get(1);
        assertSame(deLaMismaClinica, paso.getIdPersonaRol());
    }

    @Test
    public void crear_siNadieEsDeLaMismaClinica_usaAlPrimerCandidatoConEseRol() {
        Consulta consulta = consultaDeClinica(clinica("Clinica A"));
        ConsultaProcedimiento cp = procedimientoDeConsulta(consulta);
        PersonaRol primero = personaRol(clinica("Clinica B"));
        PersonaRol segundo = personaRol(clinica("Clinica C"));
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", rol("Odontologo"))));
        stubDependientes(Collections.emptyList());
        when(em.find(Consulta.class, consulta.getIdConsulta())).thenReturn(consulta);
        stubResponsables(Arrays.asList(primero, segundo));

        dao.crear(cp);

        ConsultaProcedimientoPaso paso = (ConsultaProcedimientoPaso) persistidos(2).get(1);
        assertSame(primero, paso.getIdPersonaRol());
    }

    // ---- crear(): cuando no se puede determinar la clínica de la consulta ----
    @Test
    public void crear_procedimientoSinConsulta_noBuscaLaClinicaYUsaAlPrimerCandidato() {
        ConsultaProcedimiento cp = procedimientoDeConsulta(null);
        PersonaRol primero = personaRol(clinica("Clinica B"));
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", rol("Odontologo"))));
        stubDependientes(Collections.emptyList());
        stubResponsables(Arrays.asList(primero));

        dao.crear(cp);

        ConsultaProcedimientoPaso paso = (ConsultaProcedimientoPaso) persistidos(2).get(1);
        assertSame(primero, paso.getIdPersonaRol());
        verify(em, never()).find(any(Class.class), any());
    }

    @Test
    public void crear_consultaQueYaNoExisteEnLaBase_usaAlPrimerCandidato() {
        Consulta consulta = consultaDeClinica(clinica("Clinica A"));
        ConsultaProcedimiento cp = procedimientoDeConsulta(consulta);
        PersonaRol primero = personaRol(clinica("Clinica B"));
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", rol("Odontologo"))));
        stubDependientes(Collections.emptyList());
        when(em.find(Consulta.class, consulta.getIdConsulta())).thenReturn(null);
        stubResponsables(Arrays.asList(primero));

        dao.crear(cp);

        ConsultaProcedimientoPaso paso = (ConsultaProcedimientoPaso) persistidos(2).get(1);
        assertSame(primero, paso.getIdPersonaRol());
    }

    @Test
    public void crear_consultaSinPersonaRol_usaAlPrimerCandidato() {
        Consulta consultaSinPersonaRol = new Consulta(UUID.randomUUID());
        ConsultaProcedimiento cp = procedimientoDeConsulta(consultaSinPersonaRol);
        PersonaRol primero = personaRol(clinica("Clinica B"));
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", rol("Odontologo"))));
        stubDependientes(Collections.emptyList());
        when(em.find(Consulta.class, consultaSinPersonaRol.getIdConsulta())).thenReturn(consultaSinPersonaRol);
        stubResponsables(Arrays.asList(primero));

        dao.crear(cp);

        ConsultaProcedimientoPaso paso = (ConsultaProcedimientoPaso) persistidos(2).get(1);
        assertSame(primero, paso.getIdPersonaRol());
    }

    @Test
    public void crear_personaRolDeLaConsultaSinClinica_usaAlPrimerCandidato() {
        Consulta consulta = consultaDeClinica(null);
        ConsultaProcedimiento cp = procedimientoDeConsulta(consulta);
        PersonaRol primero = personaRol(clinica("Clinica B"));
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", rol("Odontologo"))));
        stubDependientes(Collections.emptyList());
        when(em.find(Consulta.class, consulta.getIdConsulta())).thenReturn(consulta);
        stubResponsables(Arrays.asList(primero));

        dao.crear(cp);

        ConsultaProcedimientoPaso paso = (ConsultaProcedimientoPaso) persistidos(2).get(1);
        assertSame(primero, paso.getIdPersonaRol());
    }

    // ---- crear(): errores que cancelan todo (la transacción se revierte) ----
    @Test
    public void crear_pasoSinRolAsignado_lanzaIllegalStateConElNombreDelPaso() {
        ConsultaProcedimiento cp = procedimientoDeConsulta(null);
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", null)));
        stubDependientes(Collections.emptyList());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.crear(cp));

        assertTrue(ex.getMessage().contains("Revision"));
        assertTrue(ex.getMessage().contains("no tiene rol asignado"));
        verify(em, times(1)).persist(any());   // solo el procedimiento, ningún paso
    }

    @Test
    public void crear_nadieTieneElRolDelPaso_lanzaIllegalStateConElRolYElPaso() {
        ConsultaProcedimiento cp = procedimientoDeConsulta(null);
        stubPasosDefinidos(Arrays.asList(pasoDefinido("Revision", rol("Odontologo"))));
        stubDependientes(Collections.emptyList());
        stubResponsables(Collections.emptyList());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.crear(cp));

        assertTrue(ex.getMessage().contains("Odontologo"));
        assertTrue(ex.getMessage().contains("Revision"));
        verify(em, times(1)).persist(any());
    }

    // ---- eliminar() ----
    @Test
    public void eliminar_idNulo_lanzaIllegalArgumentYNoTocaLaBase() {
        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(null));

        verifyNoInteractions(em);
    }

    private void stubConteoOrdenes(Long resultado) {
        when(em.createQuery(contains("COUNT(o)"), eq(Long.class))).thenReturn(qConteoOrdenes);
        when(qConteoOrdenes.setParameter(anyString(), any())).thenReturn(qConteoOrdenes);
        when(qConteoOrdenes.getSingleResult()).thenReturn(resultado);
    }

    @Test
    public void eliminar_conOrdenesDeExamenEnSusPasos_noBorraNadaYLanzaIllegalState() {
        UUID id = UUID.randomUUID();
        stubConteoOrdenes(2L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dao.eliminar(id));

        assertTrue(ex.getMessage().contains("rdenes de examen"));
        verify(em, never()).createQuery(anyString());   // no se borraron los pasos
        verify(em, never()).remove(any());
    }

    @Test
    public void eliminar_sinOrdenes_borraPrimeroLosPasosYDespuesElProcedimiento() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimiento managed = new ConsultaProcedimiento(id);
        stubConteoOrdenes(0L);
        when(em.createQuery(contains("DELETE FROM ConsultaProcedimientoPaso"))).thenReturn(qBorrarPasos);
        when(qBorrarPasos.setParameter(anyString(), any())).thenReturn(qBorrarPasos);
        when(qBorrarPasos.executeUpdate()).thenReturn(3);
        when(em.find(ConsultaProcedimiento.class, id)).thenReturn(managed);

        dao.eliminar(id);

        verify(qBorrarPasos).setParameter("id", id);
        // Si se borrara el procedimiento antes que sus pasos, la llave foránea lo impediría.
        InOrder orden = inOrder(qBorrarPasos, em);
        orden.verify(qBorrarPasos).executeUpdate();
        orden.verify(em).remove(managed);
    }

    @Test
    public void eliminar_elConteoDeOrdenesVieneNulo_seTrataComoSinOrdenes() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimiento managed = new ConsultaProcedimiento(id);
        stubConteoOrdenes(null);
        when(em.createQuery(contains("DELETE FROM ConsultaProcedimientoPaso"))).thenReturn(qBorrarPasos);
        when(qBorrarPasos.setParameter(anyString(), any())).thenReturn(qBorrarPasos);
        when(qBorrarPasos.executeUpdate()).thenReturn(0);
        when(em.find(ConsultaProcedimiento.class, id)).thenReturn(managed);

        dao.eliminar(id);

        verify(em).remove(managed);
    }

    @Test
    public void eliminar_elProcedimientoNoExiste_lanzaIllegalArgument() {
        UUID id = UUID.randomUUID();
        stubConteoOrdenes(0L);
        when(em.createQuery(contains("DELETE FROM ConsultaProcedimientoPaso"))).thenReturn(qBorrarPasos);
        when(qBorrarPasos.setParameter(anyString(), any())).thenReturn(qBorrarPasos);
        when(qBorrarPasos.executeUpdate()).thenReturn(0);
        when(em.find(ConsultaProcedimiento.class, id)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.eliminar(id));

        verify(em, never()).remove(any());
    }
}