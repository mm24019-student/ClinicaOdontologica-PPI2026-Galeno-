package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
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
 * Prueba de PersonaRolModel (CRUD standalone). El flujo estándar de
 * AbstracCrudModel ya está cubierto por RolModelTest; aquí solo se prueba
 * relacionesCompletas() con sus 3 campos (persona + rol + clinica, incluida
 * la rama de persona que PersonaRolDetalleModelTest no cubre porque ahí la
 * persona ya viene fija) y los getters getPersonas()/getRoles()/getClinicas().
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class PersonaRolModelTest {

    @Mock
    private PersonaRolDAO personaRolDAO;
    @Mock
    private PersonaDAO personaDAO;
    @Mock
    private RolDAO rolDAO;
    @Mock
    private ClinicaDAO clinicaDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private PersonaRolModel bean;

    // ---- btnCrearhandler() / relacionesCompletas() ----

    @Test
    public void btnCrearhandler_sinPersona_agregaMensajeDeErrorYNoCrea() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdRol(new Rol(UUID.randomUUID()));
        pr.setIdClinica(new Clinica(UUID.randomUUID()));
        bean.setRegistro(pr);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(personaRolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_sinRol_agregaMensajeDeErrorYNoCrea() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdPersona(new Persona(UUID.randomUUID()));
        pr.setIdClinica(new Clinica(UUID.randomUUID()));
        bean.setRegistro(pr);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(personaRolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_sinClinica_agregaMensajeDeErrorYNoCrea() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdPersona(new Persona(UUID.randomUUID()));
        pr.setIdRol(new Rol(UUID.randomUUID()));
        bean.setRegistro(pr);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(personaRolDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_relacionesCompletas_creaYRecargaLaLista() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdPersona(new Persona(UUID.randomUUID()));
        pr.setIdRol(new Rol(UUID.randomUUID()));
        pr.setIdClinica(new Clinica(UUID.randomUUID()));
        bean.setRegistro(pr);
        List<PersonaRol> recargados = Arrays.asList(pr);
        when(personaRolDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(personaRolDAO).crear(pr);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- btnModificarHandler() / relacionesCompletas() ----

    @Test
    public void btnModificarHandler_sinPersona_agregaMensajeDeErrorYNoActualiza() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdRol(new Rol(UUID.randomUUID()));
        pr.setIdClinica(new Clinica(UUID.randomUUID()));
        bean.setRegistro(pr);

        bean.btnModificarHandler();

        verify(personaRolDAO, never()).actualizar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_relacionesCompletas_actualizaYRecargaLaLista() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setIdPersona(new Persona(UUID.randomUUID()));
        pr.setIdRol(new Rol(UUID.randomUUID()));
        pr.setIdClinica(new Clinica(UUID.randomUUID()));
        bean.setRegistro(pr);
        when(personaRolDAO.actualizar(pr)).thenReturn(pr);
        List<PersonaRol> recargados = Arrays.asList(pr);
        when(personaRolDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnModificarHandler();

        verify(personaRolDAO).actualizar(pr);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- getPersonas() / getRoles() / getClinicas() ----

    @Test
    public void getPersonas_cacheaEntreLlamadas() {
        when(personaDAO.findRange(0, 100)).thenReturn(Arrays.asList(new Persona(UUID.randomUUID())));

        bean.getPersonas();
        bean.getPersonas();

        verify(personaDAO, times(1)).findRange(0, 100);
    }

    @Test
    public void getRoles_cacheaEntreLlamadas() {
        when(rolDAO.findRange(0, 100)).thenReturn(Arrays.asList(new Rol(UUID.randomUUID())));

        bean.getRoles();
        bean.getRoles();

        verify(rolDAO, times(1)).findRange(0, 100);
    }

    @Test
    public void getClinicas_cacheaEntreLlamadas() {
        when(clinicaDAO.findRange(0, 100)).thenReturn(Arrays.asList(new Clinica(UUID.randomUUID())));

        bean.getClinicas();
        bean.getClinicas();

        verify(clinicaDAO, times(1)).findRange(0, 100);
    }
}