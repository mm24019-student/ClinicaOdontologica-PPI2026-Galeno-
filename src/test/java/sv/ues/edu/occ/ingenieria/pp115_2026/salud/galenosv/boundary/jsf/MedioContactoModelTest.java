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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de MedioContactoModel (CRUD standalone). Mismo caso que
 * DocumentoModelTest: el flujo estándar de AbstracCrudModel ya está cubierto
 * por RolModelTest; aquí solo se prueba relacionesCompletas() (persona +
 * tipoMedioContacto, incluida la rama de persona que PersonaMedioContactoModel
 * no cubre) y los getters getPersonas()/getTiposMedioContacto().
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class MedioContactoModelTest {

    @Mock
    private MedioContactoDAO medioContactoDAO;
    @Mock
    private PersonaDAO personaDAO;
    @Mock
    private TipoMedioContactoDAO tipoMedioContactoDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private MedioContactoModel bean;

    // ---- btnCrearhandler() / relacionesCompletas() ----

    @Test
    public void btnCrearhandler_sinPersona_agregaMensajeDeErrorYNoCrea() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setIdTipoMedioContacto(new TipoMedioContacto(UUID.randomUUID()));
        bean.setRegistro(mc);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(medioContactoDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_sinTipoMedioContacto_agregaMensajeDeErrorYNoCrea() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setIdPersona(new Persona(UUID.randomUUID()));
        bean.setRegistro(mc);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(medioContactoDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_relacionesCompletas_creaYRecargaLaLista() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setIdPersona(new Persona(UUID.randomUUID()));
        mc.setIdTipoMedioContacto(new TipoMedioContacto(UUID.randomUUID()));
        bean.setRegistro(mc);
        List<MedioContacto> recargados = Arrays.asList(mc);
        when(medioContactoDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(medioContactoDAO).crear(mc);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- btnModificarHandler() / relacionesCompletas() ----

    @Test
    public void btnModificarHandler_sinPersona_agregaMensajeDeErrorYNoActualiza() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setIdTipoMedioContacto(new TipoMedioContacto(UUID.randomUUID()));
        bean.setRegistro(mc);

        bean.btnModificarHandler();

        verify(medioContactoDAO, never()).actualizar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_relacionesCompletas_actualizaYRecargaLaLista() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setIdPersona(new Persona(UUID.randomUUID()));
        mc.setIdTipoMedioContacto(new TipoMedioContacto(UUID.randomUUID()));
        bean.setRegistro(mc);
        when(medioContactoDAO.actualizar(mc)).thenReturn(mc);
        List<MedioContacto> recargados = Arrays.asList(mc);
        when(medioContactoDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnModificarHandler();

        verify(medioContactoDAO).actualizar(mc);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- getPersonas() ----

    @Test
    public void getPersonas_cacheaEntreLlamadas() {
        when(personaDAO.findRange(0, 100)).thenReturn(Arrays.asList(new Persona(UUID.randomUUID())));

        bean.getPersonas();
        bean.getPersonas();

        verify(personaDAO, times(1)).findRange(0, 100);
    }

    // ---- getTiposMedioContacto() ----

    @Test
    public void getTiposMedioContacto_cacheaEntreLlamadas() {
        when(tipoMedioContactoDAO.findRange(0, 100)).thenReturn(Arrays.asList(new TipoMedioContacto(UUID.randomUUID())));

        bean.getTiposMedioContacto();
        bean.getTiposMedioContacto();

        verify(tipoMedioContactoDAO, times(1)).findRange(0, 100);
    }
}