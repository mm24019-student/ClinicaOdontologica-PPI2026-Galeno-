package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Prueba de ConsultaProcedimientoPasoModel (detalle de ConsultaProcedimiento).
 * Aquí solo se prueba lo propio: registro nuevo en estado PENDIENTE con fecha
 * de inicio, la lista de estados del combo (que debe coincidir con la regex de
 * la entidad) y el autocomplete de Persona/Rol.
 *
 * @author antonio
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoPasoModelTest {

    @Mock
    private ConsultaProcedimientoPasoDAO pasoDAO;
    @Mock
    private PersonaRolDAO personaRolDAO;
    @Mock
    private ProcedimientoPasoDAO procedimientoPasoDAO;
    @Mock
    private SesionBean sesionBean;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private ConsultaProcedimientoPasoModel bean;

    private ConsultaProcedimiento padre;

    @BeforeEach
    public void setUp() {
        padre = new ConsultaProcedimiento(UUID.randomUUID());
    }

    // Simula que hay una sesión iniciada. Se llama solo en los tests que
    // pasan por permitirAccion() (estricto: un stub sin usar hace fallar).
    private void iniciarSesion() {
        when(sesionBean.isAutenticado()).thenReturn(true);
    }

    private PersonaRol nuevaPersonaRol(String nombres, String apellidos, String nombreRol) {
        Persona persona = new Persona(UUID.randomUUID());
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdPersona(persona);
        if (nombreRol != null) {
            Rol rol = new Rol(UUID.randomUUID());
            rol.setNombre(nombreRol);
            pr.setIdRol(rol);
        }
        return pr;
    }

    // ---- getDAO() / obtenerId() / los 3 métodos del detalle ----

    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(pasoDAO, bean.getDAO());
    }

    @Test
    public void obtenerId_devuelveElIdDelPaso() {
        UUID id = UUID.randomUUID();

        assertEquals(id, bean.obtenerId(new ConsultaProcedimientoPaso(id)));
    }

    @Test
    public void obtenerIdPadre_devuelveElIdDelConsultaProcedimiento() {
        assertEquals(padre.getIdConsultaProcedimiento(), bean.obtenerIdPadre(padre));
    }

    @Test
    public void asignarPadre_ligaElPasoAlConsultaProcedimiento() {
        ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());

        bean.asignarPadre(paso, padre);

        assertSame(padre, paso.getIdConsultaProcedimiento());
    }

    @Test
    public void buscarPorPadre_delegaEnFindByConsultaProcedimiento() {
        List<ConsultaProcedimientoPaso> esperado = Arrays.asList(new ConsultaProcedimientoPaso(UUID.randomUUID()));
        when(pasoDAO.findByConsultaProcedimiento(padre.getIdConsultaProcedimiento())).thenReturn(esperado);

        assertEquals(esperado, bean.buscarPorPadre(padre.getIdConsultaProcedimiento()));
    }

    // ---- inicializar() / cargarDe() ----

    @Test
    public void inicializar_arrancaVacioSinConsultarLaBaseDeDatos() {
        bean.inicializar();

        assertTrue(bean.getregistros().isEmpty());
        verifyNoInteractions(pasoDAO);
    }

    @Test
    public void cargarDe_padreConId_cargaSoloSusPasos() {
        List<ConsultaProcedimientoPaso> esperado = Arrays.asList(new ConsultaProcedimientoPaso(UUID.randomUUID()));
        when(pasoDAO.findByConsultaProcedimiento(padre.getIdConsultaProcedimiento())).thenReturn(esperado);

        bean.cargarDe(padre);

        assertEquals(esperado, bean.getregistros());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    @Test
    public void cargarDe_padreNulo_dejaListaVaciaSinConsultar() {
        bean.cargarDe(null);

        assertTrue(bean.getregistros().isEmpty());
        verify(pasoDAO, never()).findByConsultaProcedimiento(any());
    }

    // ---- btnNuevoHandler() / crearRegistroNuevo() ----

    @Test
    public void btnNuevoHandler_creaPasoPendienteConFechaYLigadoAlPadreActual() {
        iniciarSesion();
        when(pasoDAO.findByConsultaProcedimiento(padre.getIdConsultaProcedimiento()))
                .thenReturn(Collections.emptyList());
        bean.cargarDe(padre);

        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(bean.getRegistro().getIdConsultaProcedimientoPaso());
        assertEquals("PENDIENTE", bean.getRegistro().getEstado());
        assertNotNull(bean.getRegistro().getFechaInicio());
        assertSame(padre, bean.getRegistro().getIdConsultaProcedimiento());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    // ---- btnCrearhandler() + recargarLista() ----

    @Test
    public void btnCrearhandler_exito_creaYRecargaSoloLosPasosDelPadre() {
        iniciarSesion();
        PersonaRol usuario = nuevaPersonaRol("Ana", "Perez", "Odontologo");
        when(sesionBean.getPersonaRolActual()).thenReturn(usuario);
        UUID idPadre = padre.getIdConsultaProcedimiento();
        when(pasoDAO.findByConsultaProcedimiento(idPadre)).thenReturn(Collections.emptyList());
        bean.cargarDe(padre);
        bean.btnNuevoHandler(mock(ActionEvent.class));
        ConsultaProcedimientoPaso nuevo = bean.getRegistro();
        nuevo.setIdProcedimientoPaso(new ProcedimientoPaso(UUID.randomUUID()));

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(pasoDAO).crear(nuevo);
        assertSame(usuario, nuevo.getIdPersonaRol());
        verify(pasoDAO, times(2)).findByConsultaProcedimiento(idPadre);
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    // ---- getEstados() ----

    @Test
    public void getEstados_devuelveLosCuatroEstadosEnOrden() {
        assertEquals(Arrays.asList("PENDIENTE", "EN_PROCESO", "COMPLETADO", "CANCELADO"), bean.getEstados());
    }

    @Test
    public void getEstados_todosCumplenLaRegexDeLaEntidad() {
        for (String estado : bean.getEstados()) {
            assertTrue(estado.matches(ConsultaProcedimientoPaso.ESTADOS_VALIDOS_REGEX),
                    "El estado " + estado + " no cumple ESTADOS_VALIDOS_REGEX");
        }
    }

    // ---- getPersonasRol() / completarPersonasRol() ----

    @Test
    public void getPersonasRol_cacheaElCatalogoEntreLlamadas() {
        when(personaRolDAO.findRange(0, 100)).thenReturn(Arrays.asList(nuevaPersonaRol("Ana", "Perez", "Odontologo")));

        bean.getPersonasRol();
        bean.getPersonasRol();

        verify(personaRolDAO, times(1)).findRange(0, 100);
    }

    @Test
    public void completarPersonasRol_filtraPorLaEtiquetaSinImportarMayusculas() {
        PersonaRol ana = nuevaPersonaRol("Ana", "Perez", "Odontologo");
        PersonaRol luis = nuevaPersonaRol("Luis", "Gomez", "Recepcionista");
        when(personaRolDAO.findRange(0, 100)).thenReturn(Arrays.asList(ana, luis));
        when(sesionBean.perteneceAClinicaActual(any())).thenReturn(true);

        List<PersonaRol> resultado = bean.completarPersonasRol("ODONTO");

        assertEquals(1, resultado.size());
        assertSame(ana, resultado.get(0));
    }

    @Test
    public void completarPersonasRol_consultaNula_devuelveTodas() {
        when(personaRolDAO.findRange(0, 100)).thenReturn(Arrays.asList(
                nuevaPersonaRol("Ana", "Perez", "Odontologo"),
                nuevaPersonaRol("Luis", "Gomez", "Recepcionista")));
        when(sesionBean.perteneceAClinicaActual(any())).thenReturn(true);

        assertEquals(2, bean.completarPersonasRol(null).size());
    }

    // ---- etiquetaPersonaRol() ----

    @Test
    public void etiquetaPersonaRol_nula_devuelveCadenaVacia() {
        assertEquals("", bean.etiquetaPersonaRol(null));
    }

    @Test
    public void etiquetaPersonaRol_sinPersona_devuelveCadenaVacia() {
        assertEquals("", bean.etiquetaPersonaRol(new PersonaRol(UUID.randomUUID())));
    }

    @Test
    public void etiquetaPersonaRol_conRol_devuelveNombreApellidoYRol() {
        assertEquals("Ana Perez - Odontologo",
                bean.etiquetaPersonaRol(nuevaPersonaRol("Ana", "Perez", "Odontologo")));
    }

    @Test
    public void etiquetaPersonaRol_sinRol_devuelveSoloElNombreCompleto() {
        assertEquals("Ana Perez", bean.etiquetaPersonaRol(nuevaPersonaRol("Ana", "Perez", null)));
    }

    @Test
    public void etiquetaPersonaRol_apellidosNulos_noDejaTextoNullNiEspaciosSobrantes() {
        assertEquals("Ana - Odontologo",
                bean.etiquetaPersonaRol(nuevaPersonaRol("Ana", null, "Odontologo")));
    }
}