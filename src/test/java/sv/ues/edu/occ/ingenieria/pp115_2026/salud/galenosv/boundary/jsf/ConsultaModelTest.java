package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Prueba de ConsultaModel (CRUD padre con pestañas: Consulta ->
 * Procedimientos de la Consulta -> Pasos del Procedimiento). El flujo
 * estándar de AbstracCrudModel ya está cubierto por AbstracCrudModelTest;
 * aquí solo lo propio: exigir sesión para crear, registro nuevo con fecha de
 * inicio y persona/rol de la sesión, autocomplete de Persona/Rol, reseteo de
 * los dos hijos y el único listener onTabChange.
 *
 * @author antonio
 */
@ExtendWith(MockitoExtension.class)
public class ConsultaModelTest {

    // Debe coincidir con el p:tab de Consulta.xhtml (una sola pestaña con
    // procedimientos y pasos).
    private static final String TAB_PROCEDIMIENTOS = "Procedimientos";

    @Mock
    private ConsultaDAO cDAO;
    @Mock
    private PersonaRolDAO personaRolDAO;
    @Mock
    private SesionBean sesionBean;
    @Mock
    private ConsultaProcedimientoModel consultaProcedimientoModel;
    @Mock
    private ConsultaProcedimientoPasoModel consultaProcedimientoPasoModel;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private ConsultaModel bean;

    private PersonaRol personaRol;

    @BeforeEach
    public void setUp() {
        personaRol = nuevaPersonaRol("Ana", "Perez", "Odontologo");
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

    // Simula que hay una sesión iniciada. Se llama solo en los tests que
    // pasan por permitirAccion(): con Mockito estricto, un stub sin usar
    // en el resto de los tests haría fallar la prueba.
    private void iniciarSesion() {
        when(sesionBean.isAutenticado()).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    private TabChangeEvent<Object> eventoDePestana(String titulo) {
        TabChangeEvent<Object> event = mock(TabChangeEvent.class);
        Tab tab = mock(Tab.class);
        when(event.getTab()).thenReturn(tab);
        when(tab.getTitle()).thenReturn(titulo);
        return event;
    }

    // ---- getDAO() / obtenerId() ----

    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(cDAO, bean.getDAO());
    }

    @Test
    public void obtenerId_devuelveElIdDeLaConsulta() {
        UUID id = UUID.randomUUID();

        assertEquals(id, bean.obtenerId(new Consulta(id)));
    }

    // ---- inicializar() ----

    @Test
    public void inicializar_cargaLasPrimeras100ConsultasDelDiaDeHoy() {
        // El filtro de fechas arranca con "hoy" (fechaDesde/fechaHasta), así que
        // ya no se usa findRange sino buscarConFiltro. Sin sesión no hay clínica.
        List<Consulta> esperado = Arrays.asList(new Consulta(UUID.randomUUID()));
        when(cDAO.buscarConFiltro(any(Date.class), any(Date.class), isNull(), eq(100)))
                .thenReturn(esperado);

        bean.inicializar();

        assertEquals(esperado, bean.getregistros());
    }

    // ---- permitirAccion() ----

    @Test
    public void btnNuevoHandler_sinSesion_noCreaRegistroYAvisa() {
        when(sesionBean.isAutenticado()).thenReturn(false);

        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        verify(fc).addMessage(isNull(), any(FacesMessage.class));
        verifyNoInteractions(consultaProcedimientoModel, consultaProcedimientoPasoModel);
    }

    // ---- btnNuevoHandler() / crearRegistroNuevoBase() / configurarNuevoRegistro() ----

    @Test
    public void btnNuevoHandler_creaConsultaConIdYFechaDeInicioYPasaAEstadoCrear() {
        iniciarSesion();

        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(bean.getRegistro().getIdConsulta());
        assertNotNull(bean.getRegistro().getFechaInicio());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void btnNuevoHandler_creaLaConsultaSinPacienteHastaQueSeElija() {
        // El paciente se busca con el diálogo (seleccionarPaciente); la sesión
        // ya no se asigna a la consulta al pulsar "Nuevo".
        iniciarSesion();

        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNull(bean.getPersonaRolSeleccionado());
        assertNull(bean.getRegistro().getIdPersonaRol());
    }

    @Test
    public void btnNuevoHandler_noArrastraLaPersonaRolSeleccionadaAntes() {
        iniciarSesion();
        bean.setPersonaRolSeleccionado(personaRol);

        bean.btnNuevoHandler(mock(ActionEvent.class));

        // Una consulta nueva siempre empieza sin paciente.
        assertNull(bean.getPersonaRolSeleccionado());
    }

    @Test
    public void btnNuevoHandler_limpiaLosDosHijos() {
        iniciarSesion();

        bean.btnNuevoHandler(mock(ActionEvent.class));

        verify(consultaProcedimientoModel).cargarDe(null);
        verify(consultaProcedimientoModel).btnCancelar();
        verify(consultaProcedimientoPasoModel).btnCancelar();
    }

    // ---- btnCancelar() / resetearHijos() ----

    @Test
    public void btnCancelar_limpiaSeleccionYReseteaLosProcedimientosDeLaConsulta() {
        bean.setRegistro(new Consulta(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.btnCancelar();

        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        verify(consultaProcedimientoModel).cargarDe(null);
    }

    // ---- setPersonaRolSeleccionado() ----

    @Test
    public void setPersonaRolSeleccionado_conRegistro_loAsignaALaConsulta() {
        Consulta consulta = new Consulta(UUID.randomUUID());
        bean.setRegistro(consulta);

        bean.setPersonaRolSeleccionado(personaRol);

        assertSame(personaRol, bean.getPersonaRolSeleccionado());
        assertSame(personaRol, consulta.getIdPersonaRol());
    }

    @Test
    public void setPersonaRolSeleccionado_sinRegistro_noLanzaExcepcion() {
        assertDoesNotThrow(() -> bean.setPersonaRolSeleccionado(personaRol));

        assertSame(personaRol, bean.getPersonaRolSeleccionado());
    }

    // ---- onRowSelect() ----

    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_seleccionaLaConsultaMuestraSuPersonaRolYLimpiaLosHijos() {
        Consulta elegida = new Consulta(UUID.randomUUID());
        elegida.setIdPersonaRol(personaRol);
        SelectEvent<Consulta> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(elegida);

        bean.onRowSelect(event);

        assertSame(elegida, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
        assertSame(personaRol, bean.getPersonaRolSeleccionado());
        verify(consultaProcedimientoModel).btnCancelar();
        verify(consultaProcedimientoPasoModel).btnCancelar();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_consultaSinPersonaRol_dejaLaSeleccionEnNull() {
        bean.setPersonaRolSeleccionado(personaRol);
        SelectEvent<Consulta> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(new Consulta(UUID.randomUUID()));

        bean.onRowSelect(event);

        assertNull(bean.getPersonaRolSeleccionado());
    }

    // ---- getPersonasRol() / completarPersonasRol() ----

    @Test
    public void getPersonasRol_cacheaElCatalogoEntreLlamadas() {
        when(personaRolDAO.findRange(0, 100)).thenReturn(Arrays.asList(personaRol));

        bean.getPersonasRol();
        bean.getPersonasRol();

        verify(personaRolDAO, times(1)).findRange(0, 100);
    }

    @Test
    public void completarPersonasRol_filtraPorLaEtiquetaSinImportarMayusculas() {
        PersonaRol otra = nuevaPersonaRol("Luis", "Gomez", "Recepcionista");
        when(personaRolDAO.findRange(0, 100)).thenReturn(Arrays.asList(personaRol, otra));
        when(sesionBean.perteneceAClinicaActual(any())).thenReturn(true);

        List<PersonaRol> resultado = bean.completarPersonasRol("ANA PEREZ");

        assertEquals(1, resultado.size());
        assertSame(personaRol, resultado.get(0));
    }

    @Test
    public void completarPersonasRol_tambienBuscaPorElNombreDelRol() {
        PersonaRol otra = nuevaPersonaRol("Luis", "Gomez", "Recepcionista");
        when(personaRolDAO.findRange(0, 100)).thenReturn(Arrays.asList(personaRol, otra));
        when(sesionBean.perteneceAClinicaActual(any())).thenReturn(true);

        List<PersonaRol> resultado = bean.completarPersonasRol("recepcion");

        assertEquals(1, resultado.size());
        assertSame(otra, resultado.get(0));
    }

    @Test
    public void completarPersonasRol_consultaNula_devuelveTodas() {
        when(personaRolDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(personaRol, nuevaPersonaRol("Luis", "Gomez", "Recepcionista")));
        when(sesionBean.perteneceAClinicaActual(any())).thenReturn(true);

        assertEquals(2, bean.completarPersonasRol(null).size());
    }

    @Test
    public void completarPersonasRol_excluyeLasDeOtraClinica() {
        PersonaRol otra = nuevaPersonaRol("Luis", "Gomez", "Recepcionista");
        when(personaRolDAO.findRange(0, 100)).thenReturn(Arrays.asList(personaRol, otra));
        when(sesionBean.perteneceAClinicaActual(personaRol)).thenReturn(true);
        when(sesionBean.perteneceAClinicaActual(otra)).thenReturn(false);

        List<PersonaRol> resultado = bean.completarPersonasRol("");

        assertEquals(1, resultado.size());
        assertSame(personaRol, resultado.get(0));
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
        assertEquals("Ana Perez - Odontologo", bean.etiquetaPersonaRol(personaRol));
    }

    @Test
    public void etiquetaPersonaRol_sinRol_devuelveSoloElNombreCompleto() {
        assertEquals("Ana Perez", bean.etiquetaPersonaRol(nuevaPersonaRol("Ana", "Perez", null)));
    }

    @Test
    public void etiquetaPersonaRol_apellidosNulos_noDejaTextoNullNiEspaciosSobrantes() {
        assertEquals("Ana - Odontologo", bean.etiquetaPersonaRol(nuevaPersonaRol("Ana", null, "Odontologo")));
    }

    // ---- onTabChange() ----

    @Test
    public void onTabChange_pestanaProcedimientos_cargaLosProcedimientosYLimpiaLosPasos() {
        Consulta actual = new Consulta(UUID.randomUUID());
        bean.setRegistro(actual);

        bean.onTabChange(eventoDePestana(TAB_PROCEDIMIENTOS));

        verify(consultaProcedimientoModel).cargarDe(actual);
        // Aún no hay un procedimiento elegido, así que los pasos quedan vacíos.
        verify(consultaProcedimientoPasoModel).cargarDe(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onProcedimientoRowSelect_seleccionaElProcedimientoYCargaSusPasos() {
        ConsultaProcedimiento elegido = new ConsultaProcedimiento(UUID.randomUUID());
        SelectEvent<ConsultaProcedimiento> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(elegido);

        bean.onProcedimientoRowSelect(event);

        verify(consultaProcedimientoModel).onRowSelect(event);
        verify(consultaProcedimientoPasoModel).cargarDe(elegido);
    }

    @Test
    public void onTabChange_otraPestana_noRecargaNingunHijo() {
        bean.onTabChange(eventoDePestana("Datos de la Consulta"));

        verifyNoInteractions(consultaProcedimientoModel, consultaProcedimientoPasoModel);
    }

    @Test
    public void onTabChange_pestanaSinTitulo_noRecargaNingunHijo() {
        bean.onTabChange(eventoDePestana(null));

        verifyNoInteractions(consultaProcedimientoModel, consultaProcedimientoPasoModel);
    }
}