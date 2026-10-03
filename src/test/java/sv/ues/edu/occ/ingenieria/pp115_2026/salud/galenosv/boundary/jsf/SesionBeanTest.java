package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.PrimeFaces;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Prueba de SesionBean (el selector de usuario y clínica de la cabecera). Es
 * quien decide qué usuarios se ven, qué clínica está fijada y cuándo se cierra
 * la sesión sola. Los DAO se simulan; lo que depende del FacesContext real
 * (refrescar y cerrarSesion) se prueba con un FacesContext simulado.
 */
@ExtendWith(MockitoExtension.class)
public class SesionBeanTest {

    @Mock
    private PersonaRolDAO personaRolDAO;
    @Mock
    private ClinicaDAO clinicaDAO;

    @InjectMocks
    private SesionBean bean;

    // ------------------------------------------------------------------
    // Fábricas de datos
    // ------------------------------------------------------------------
    private Clinica clinica(String nombre, Boolean activo) {
        Clinica c = new Clinica(UUID.randomUUID());
        c.setNombre(nombre);
        c.setActivo(activo);
        return c;
    }

    private Rol rol(String nombre, Boolean activo) {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombre);
        r.setActivo(activo);
        return r;
    }

    private PersonaRol usuario(String nombres, String apellidos, Rol rol, Clinica clinica) {
        Persona persona = new Persona(UUID.randomUUID());
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdPersona(persona);
        pr.setIdRol(rol);
        pr.setIdClinica(clinica);
        return pr;
    }

    // Deja a "pr" con la sesión iniciada, tal como lo hace el selector de la cabecera.
    private void iniciarSesionCon(PersonaRol pr) {
        when(personaRolDAO.listarConDetalle()).thenReturn(Arrays.asList(pr));
        bean.setIdSeleccionado(pr.getIdPersonaRol().toString());
        bean.cambiar();
    }

    // ---- getOpciones() ----

    @Test
    public void getOpciones_excluyeUsuariosConRolInactivoOClinicaInactiva() {
        PersonaRol bueno = usuario("Ana", "Perez", rol("Odontologo", true), clinica("A", true));
        PersonaRol rolInactivo = usuario("Luis", "Gomez", rol("Asistente", false), clinica("A", true));
        PersonaRol clinicaInactiva = usuario("Eva", "Diaz", rol("Odontologo", true), clinica("B", false));
        when(personaRolDAO.listarConDetalle()).thenReturn(Arrays.asList(bueno, rolInactivo, clinicaInactiva));

        List<PersonaRol> opciones = bean.getOpciones();

        assertEquals(1, opciones.size());
        assertSame(bueno, opciones.get(0));
    }

    @Test
    public void getOpciones_conservaLosQueNoTienenRolOClinicaOTienenEstadoSinDefinir() {
        PersonaRol sinRolNiClinica = usuario("Ana", "Perez", null, null);
        PersonaRol estadoSinDefinir = usuario("Luis", "Gomez", rol("Asistente", null), clinica("A", null));
        when(personaRolDAO.listarConDetalle()).thenReturn(Arrays.asList(sinRolNiClinica, estadoSinDefinir));

        assertEquals(2, bean.getOpciones().size());
    }

    @Test
    public void getOpciones_conClinicaDeTrabajo_soloDevuelveLosDeEsaClinica() {
        Clinica a = clinica("A", true);
        Clinica b = clinica("B", true);
        PersonaRol deA = usuario("Ana", "Perez", rol("Odontologo", true), a);
        PersonaRol deB = usuario("Luis", "Gomez", rol("Odontologo", true), b);
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(a, b));
        when(personaRolDAO.listarConDetalle()).thenReturn(Arrays.asList(deA, deB));
        bean.setIdClinicaSeleccionada(a.getIdClinica().toString());
        bean.cambiarClinica();

        List<PersonaRol> opciones = bean.getOpciones();

        assertEquals(1, opciones.size());
        assertSame(deA, opciones.get(0));
    }

    // ---- getClinicasTrabajo() ----

    @Test
    public void getClinicasTrabajo_excluyeLasInactivasPeroDejaLasQueNoTienenEstado() {
        Clinica activa = clinica("A", true);
        Clinica inactiva = clinica("B", false);
        Clinica sinEstado = clinica("C", null);
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(activa, inactiva, sinEstado));

        List<Clinica> resultado = bean.getClinicasTrabajo();

        assertEquals(Arrays.asList(activa, sinEstado), resultado);
    }

    // ---- cambiarClinica() ----

    @Test
    public void cambiarClinica_idNulo_quedaSinClinicaDeTrabajo() {
        bean.setIdClinicaSeleccionada(null);

        bean.cambiarClinica();

        assertNull(bean.getClinicaActual());
        assertNull(bean.getIdClinicaSeleccionada());
    }

    @Test
    public void cambiarClinica_idEnBlanco_quedaSinClinicaDeTrabajo() {
        bean.setIdClinicaSeleccionada("   ");

        bean.cambiarClinica();

        assertNull(bean.getClinicaActual());
        assertNull(bean.getIdClinicaSeleccionada());
    }

    @Test
    public void cambiarClinica_idDeUnaClinicaActiva_laFijaComoClinicaDeTrabajo() {
        Clinica a = clinica("A", true);
        Clinica b = clinica("B", true);
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(a, b));
        bean.setIdClinicaSeleccionada(b.getIdClinica().toString());

        bean.cambiarClinica();

        assertSame(b, bean.getClinicaActual());
        assertEquals(b.getIdClinica().toString(), bean.getIdClinicaSeleccionada());
    }

    @Test
    public void cambiarClinica_idQueNoEstaEntreLasActivas_quedaSinClinica() {
        when(clinicaDAO.findRange(0, 100)).thenReturn(Collections.singletonList(clinica("A", true)));
        bean.setIdClinicaSeleccionada(UUID.randomUUID().toString());

        bean.cambiarClinica();

        assertNull(bean.getClinicaActual());
        assertNull(bean.getIdClinicaSeleccionada());
    }

    @Test
    public void cambiarClinica_usuarioConSesionEsDeEsaClinica_conservaLaSesion() {
        Clinica a = clinica("A", true);
        PersonaRol ana = usuario("Ana", "Perez", rol("Odontologo", true), a);
        iniciarSesionCon(ana);
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(a));
        bean.setIdClinicaSeleccionada(a.getIdClinica().toString());

        bean.cambiarClinica();

        assertTrue(bean.isAutenticado());
        assertSame(ana, bean.getPersonaRolActual());
    }

    @Test
    public void cambiarClinica_usuarioConSesionEsDeOtraClinica_cierraLaSesion() {
        Clinica a = clinica("A", true);
        Clinica b = clinica("B", true);
        iniciarSesionCon(usuario("Ana", "Perez", rol("Odontologo", true), a));
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(a, b));
        bean.setIdClinicaSeleccionada(b.getIdClinica().toString());

        bean.cambiarClinica();

        assertFalse(bean.isAutenticado());
        assertNull(bean.getIdSeleccionado());
        assertSame(b, bean.getClinicaActual());
    }

    @Test
    public void cambiarClinica_alQuitarLaClinica_cierraLaSesionDelUsuario() {
        iniciarSesionCon(usuario("Ana", "Perez", rol("Odontologo", true), clinica("A", true)));
        bean.setIdClinicaSeleccionada(null);

        bean.cambiarClinica();

        assertFalse(bean.isAutenticado());
        assertNull(bean.getClinicaActual());
        assertNull(bean.getIdClinicaSeleccionada());
    }

    // ---- perteneceAClinicaActual() ----

    @Test
    public void perteneceAClinicaActual_sinClinicaFijada_aceptaATodos() {
        assertTrue(bean.perteneceAClinicaActual(usuario("Ana", "Perez", null, clinica("A", true))));
        assertTrue(bean.perteneceAClinicaActual(null));
    }

    @Test
    public void perteneceAClinicaActual_conClinicaFijada_soloAceptaALosDeEsaClinica() {
        Clinica a = clinica("A", true);
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(a));
        bean.setIdClinicaSeleccionada(a.getIdClinica().toString());
        bean.cambiarClinica();

        assertTrue(bean.perteneceAClinicaActual(usuario("Ana", "Perez", null, a)));
        assertFalse(bean.perteneceAClinicaActual(usuario("Luis", "Gomez", null, clinica("B", true))));
        assertFalse(bean.perteneceAClinicaActual(usuario("Eva", "Diaz", null, null)));
        assertFalse(bean.perteneceAClinicaActual(null));
    }

    // ---- cambiar() ----

    @Test
    public void cambiar_idNuloOEnBlanco_cierraLaSesion() {
        iniciarSesionCon(usuario("Ana", "Perez", rol("Odontologo", true), null));

        bean.setIdSeleccionado("  ");
        bean.cambiar();

        assertFalse(bean.isAutenticado());
    }

    @Test
    public void cambiar_elegirUsuario_iniciaSesionYFijaLaClinicaDeEseUsuario() {
        Clinica a = clinica("A", true);
        PersonaRol ana = usuario("Ana", "Perez", rol("Odontologo", true), a);

        iniciarSesionCon(ana);

        assertTrue(bean.isAutenticado());
        assertSame(ana, bean.getPersonaRolActual());
        assertSame(a, bean.getClinicaActual());
        assertEquals(a.getIdClinica().toString(), bean.getIdClinicaSeleccionada());
    }

    @Test
    public void cambiar_usuarioSinClinica_iniciaSesionSinTocarLaClinicaDeTrabajo() {
        PersonaRol ana = usuario("Ana", "Perez", rol("Odontologo", true), null);

        iniciarSesionCon(ana);

        assertSame(ana, bean.getPersonaRolActual());
        assertNull(bean.getClinicaActual());
    }

    @Test
    public void cambiar_idQueNoEstaEntreLasOpciones_quedaSinSesion() {
        when(personaRolDAO.listarConDetalle()).thenReturn(
                Arrays.asList(usuario("Ana", "Perez", rol("Odontologo", true), null)));
        bean.setIdSeleccionado(UUID.randomUUID().toString());

        bean.cambiar();

        assertFalse(bean.isAutenticado());
    }

    // Ejecuta refrescar() sin FacesContext (fuera de una petición JSF getCurrentInstance()
    // devuelve null). Se simula porque jakartaee-api no trae la implementación de Faces.
    private void refrescarSinPeticion() {
        try (MockedStatic<FacesContext> contexto = mockStatic(FacesContext.class)) {
            contexto.when(FacesContext::getCurrentInstance).thenReturn(null);
            bean.refrescar();
        }
    }

    // ---- refrescar() ----

    @Test
    public void refrescar_siLaClinicaDeTrabajoYaNoEstaActiva_laQuita() {
        Clinica a = clinica("A", true);
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(a));
        bean.setIdClinicaSeleccionada(a.getIdClinica().toString());
        bean.cambiarClinica();
        a.setActivo(false);

        refrescarSinPeticion();

        assertNull(bean.getClinicaActual());
        assertNull(bean.getIdClinicaSeleccionada());
    }

    @Test
    public void refrescar_siElRolDelUsuarioConSesionYaNoEstaActivo_cierraLaSesion() {
        Rol odontologo = rol("Odontologo", true);
        iniciarSesionCon(usuario("Ana", "Perez", odontologo, null));
        odontologo.setActivo(false);

        refrescarSinPeticion();

        assertFalse(bean.isAutenticado());
        assertNull(bean.getIdSeleccionado());
    }

    @Test
    public void refrescar_siTodoSigueActivo_nadaCambia() {
        Clinica a = clinica("A", true);
        PersonaRol ana = usuario("Ana", "Perez", rol("Odontologo", true), a);
        iniciarSesionCon(ana);
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(a));

        refrescarSinPeticion();

        assertSame(ana, bean.getPersonaRolActual());
        assertSame(a, bean.getClinicaActual());
    }

    @Test
    public void refrescar_enPeticionAjax_vuelveADibujarElSelectorDeSesion() {
        FacesContext fc = mock(FacesContext.class, RETURNS_DEEP_STUBS);
        when(fc.getPartialViewContext().isAjaxRequest()).thenReturn(true);
        PrimeFaces primeFaces = mock(PrimeFaces.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<FacesContext> contexto = mockStatic(FacesContext.class);
                MockedStatic<PrimeFaces> pf = mockStatic(PrimeFaces.class)) {
            contexto.when(FacesContext::getCurrentInstance).thenReturn(fc);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            bean.refrescar();
        }

        verify(primeFaces.ajax()).update("frmSesion");
    }

    @Test
    public void refrescar_enPeticionQueNoEsAjax_noActualizaNada() {
        FacesContext fc = mock(FacesContext.class, RETURNS_DEEP_STUBS);
        when(fc.getPartialViewContext().isAjaxRequest()).thenReturn(false);
        PrimeFaces primeFaces = mock(PrimeFaces.class);
        try (MockedStatic<FacesContext> contexto = mockStatic(FacesContext.class);
                MockedStatic<PrimeFaces> pf = mockStatic(PrimeFaces.class)) {
            contexto.when(FacesContext::getCurrentInstance).thenReturn(fc);
            pf.when(PrimeFaces::current).thenReturn(primeFaces);

            bean.refrescar();
        }

        verifyNoInteractions(primeFaces);
    }

    // ---- cerrarSesion() ----

    @Test
    public void cerrarSesion_cierraLaSesionConservaLaClinicaYRecargaLaMismaPagina() throws IOException {
        Clinica a = clinica("A", true);
        iniciarSesionCon(usuario("Ana", "Perez", rol("Odontologo", true), a));
        FacesContext fc = mock(FacesContext.class, RETURNS_DEEP_STUBS);
        when(fc.getExternalContext().getRequestContextPath()).thenReturn("/galeno");
        when(fc.getViewRoot().getViewId()).thenReturn("/paginas/Consulta.xhtml");
        try (MockedStatic<FacesContext> contexto = mockStatic(FacesContext.class)) {
            contexto.when(FacesContext::getCurrentInstance).thenReturn(fc);

            bean.cerrarSesion();
        }

        assertFalse(bean.isAutenticado());
        assertNull(bean.getIdSeleccionado());
        assertSame(a, bean.getClinicaActual());
        verify(fc.getExternalContext()).redirect("/galeno/paginas/Consulta.xhtml");
    }

    // ---- isAutenticado() / etiqueta() ----

    @Test
    public void isAutenticado_sinSesion_esFalso() {
        assertFalse(bean.isAutenticado());
        assertNull(bean.getPersonaRolActual());
    }

    @Test
    public void etiqueta_nula_devuelveCadenaVacia() {
        assertEquals("", bean.etiqueta(null));
    }

    @Test
    public void etiqueta_conClinica_muestraNombreRolYClinica() {
        PersonaRol ana = usuario("Ana", "Perez", rol("Odontologo", true), clinica("Clinica A", true));

        assertEquals("Ana Perez - Odontologo (Clinica A)", bean.etiqueta(ana));
    }

    @Test
    public void etiqueta_sinClinica_muestraSoloNombreYRol() {
        PersonaRol ana = usuario("Ana", "Perez", rol("Odontologo", true), null);

        assertEquals("Ana Perez - Odontologo", bean.etiqueta(ana));
    }
}