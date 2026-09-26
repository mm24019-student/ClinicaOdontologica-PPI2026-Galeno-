package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de PersonaRolDetalleModel: la única clase del dominio con lógica
 * propia que no se repite en ningún otro bean (autocompletes de Rol/Clinica,
 * validaciones de agregarRol/eliminarRol, detección de asignación duplicada).
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class PersonaRolDetalleModelTest {

    @Mock
    private PersonaRolDAO personaRolDAO;
    @Mock
    private RolDAO rolDAO;
    @Mock
    private ClinicaDAO clinicaDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private PersonaRolDetalleModel bean;

    private Persona persona;
    private Rol rol;
    private Clinica clinica;

    @BeforeEach
    public void setUp() {
        persona = new Persona(UUID.randomUUID());
        rol = new Rol(UUID.randomUUID());
        rol.setNombre("Odontologo");
        clinica = new Clinica(UUID.randomUUID());
        clinica.setNombre("Clinica Central");
    }

    // ---- completarRoles() ----

    @Test
    public void completarRoles_filtraPorNombreSinImportarMayusculas() {
        Rol otro = new Rol(UUID.randomUUID());
        otro.setNombre("Recepcionista");
        when(rolDAO.findRange(0, 100)).thenReturn(Arrays.asList(rol, otro));

        List<Rol> resultado = bean.completarRoles("odonto");

        assertEquals(1, resultado.size());
        assertSame(rol, resultado.get(0));
    }

    @Test
    public void completarRoles_cacheaElCatalogoEntreLlamadas() {
        when(rolDAO.findRange(0, 100)).thenReturn(Arrays.asList(rol));

        bean.completarRoles("");
        bean.completarRoles("odo");

        verify(rolDAO, times(1)).findRange(0, 100);
    }

    // ---- completarClinicas() ----

    @Test
    public void completarClinicas_filtraPorNombreSinImportarMayusculas() {
        Clinica otra = new Clinica(UUID.randomUUID());
        otra.setNombre("Clinica Norte");
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(clinica, otra));

        List<Clinica> resultado = bean.completarClinicas("central");

        assertEquals(1, resultado.size());
        assertSame(clinica, resultado.get(0));
    }

    // ---- onRolAutocompleteSelect() / onClinicaAutocompleteSelect() ----

    @Test
    @SuppressWarnings("unchecked")
    public void onRolAutocompleteSelect_fijaElRolSeleccionado() {
        SelectEvent<Rol> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(rol);

        bean.onRolAutocompleteSelect(event);

        assertSame(rol, bean.getNuevoRol());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onClinicaAutocompleteSelect_fijaLaClinicaSeleccionada() {
        SelectEvent<Clinica> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(clinica);

        bean.onClinicaAutocompleteSelect(event);

        assertSame(clinica, bean.getNuevaClinica());
    }

    // ---- limpiarRolSeleccionado() / limpiarClinicaSeleccionada() ----

    @Test
    public void limpiarRolSeleccionado_dejaNuevoRolEnNull() {
        bean.setNuevoRol(rol);

        bean.limpiarRolSeleccionado();

        assertNull(bean.getNuevoRol());
    }

    @Test
    public void limpiarClinicaSeleccionada_dejaNuevaClinicaEnNull() {
        bean.setNuevaClinica(clinica);

        bean.limpiarClinicaSeleccionada();

        assertNull(bean.getNuevaClinica());
    }

    // ---- agregarRol() ----

    @Test
    public void agregarRol_sinPersonaSeleccionada_agregaMensajeDeErrorYNoCrea() {
        // padreActual nunca se fijó (bean recién construido).
        bean.setNuevoRol(rol);
        bean.setNuevaClinica(clinica);

        bean.agregarRol();

        verify(personaRolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void agregarRol_sinRolSeleccionado_agregaMensajeDeErrorYNoCrea() {
        bean.padreActual = persona;
        bean.setNuevaClinica(clinica);
        // nuevoRol se queda null.

        bean.agregarRol();

        verify(personaRolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void agregarRol_sinClinicaSeleccionada_agregaMensajeDeErrorYNoCrea() {
        bean.padreActual = persona;
        bean.setNuevoRol(rol);
        // nuevaClinica se queda null.

        bean.agregarRol();

        verify(personaRolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void agregarRol_yaAsignado_agregaMensajeDeAdvertenciaYNoCrea() {
        PersonaRol existente = new PersonaRol(UUID.randomUUID());
        existente.setIdRol(rol);
        existente.setIdClinica(clinica);
        bean.padreActual = persona;
        bean.setWrappedData(Collections.singletonList(existente));
        bean.setNuevoRol(rol);
        bean.setNuevaClinica(clinica);

        bean.agregarRol();

        verify(personaRolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_WARN, captor.getValue().getSeverity());
    }

    @Test
    public void agregarRol_exito_creaYRecarga() {
        bean.padreActual = persona;
        bean.setWrappedData(Collections.emptyList());
        bean.setNuevoRol(rol);
        bean.setNuevaClinica(clinica);
        PersonaRol recargado = new PersonaRol(UUID.randomUUID());
        when(personaRolDAO.findByPersona(persona.getIdPersona()))
                .thenReturn(Collections.singletonList(recargado));

        bean.agregarRol();

        verify(personaRolDAO).crear(any(PersonaRol.class));
        // Se limpia la selección y se recarga la lista desde el DAO.
        assertNull(bean.getNuevoRol());
        assertNull(bean.getNuevaClinica());
        assertEquals(1, bean.getRolesDePersona().size());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    public void agregarRol_elDaoLanzaExcepcion_agregaMensajeDeErrorSinReventar() {
        bean.padreActual = persona;
        bean.setWrappedData(Collections.emptyList());
        bean.setNuevoRol(rol);
        bean.setNuevaClinica(clinica);
        doThrow(new RuntimeException("fallo bd")).when(personaRolDAO).crear(any());

        assertDoesNotThrow(() -> bean.agregarRol());

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    // ---- eliminarRol() ----

    @Test
    public void eliminarRol_nulo_agregaMensajeDeErrorYNoLlamaAlDao() {
        bean.eliminarRol(null);

        verify(personaRolDAO, never()).eliminar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void eliminarRol_sinId_agregaMensajeDeErrorYNoLlamaAlDao() {
        PersonaRol sinId = new PersonaRol(); // idPersonaRol queda null

        bean.eliminarRol(sinId);

        verify(personaRolDAO, never()).eliminar(any());
    }

    @Test
    public void eliminarRol_exito_eliminaYRecarga() {
        UUID idPersonaRol = UUID.randomUUID();
        PersonaRol pr = new PersonaRol(idPersonaRol);
        bean.padreActual = persona;
        when(personaRolDAO.findByPersona(persona.getIdPersona()))
                .thenReturn(Collections.emptyList());

        bean.eliminarRol(pr);

        verify(personaRolDAO).eliminar(idPersonaRol);
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    public void eliminarRol_elDaoLanzaExcepcion_agregaMensajeDeErrorSinReventar() {
        UUID idPersonaRol = UUID.randomUUID();
        PersonaRol pr = new PersonaRol(idPersonaRol);
        bean.padreActual = persona;
        doThrow(new RuntimeException("fallo bd")).when(personaRolDAO).eliminar(idPersonaRol);

        assertDoesNotThrow(() -> bean.eliminarRol(pr));

        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    // ---- cargarDe() ----

    @Test
    public void cargarDe_limpiaLaSeleccionDeRolYClinicaPrevia() {
        bean.setNuevoRol(rol);
        bean.setNuevaClinica(clinica);
        when(personaRolDAO.findByPersona(persona.getIdPersona()))
                .thenReturn(Collections.emptyList());

        bean.cargarDe(persona);

        assertNull(bean.getNuevoRol());
        assertNull(bean.getNuevaClinica());
    }
}