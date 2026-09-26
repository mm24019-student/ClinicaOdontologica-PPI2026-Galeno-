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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de DocumentoModel (CRUD standalone). El flujo estándar heredado de
 * AbstracCrudModel ya está cubierto por RolModelTest; aquí solo se prueba lo
 * propio de esta clase: la validación relacionesCompletas() (persona +
 * tipoDocumento, incluyendo la rama de persona que PersonaDocumentoModelTest
 * no cubre porque ahí la persona ya viene fija) y los getters de combo
 * getPersonas()/getTiposDocumento(). Representa también a MedioContactoModel
 * (mismo patrón, cambiando el nombre del campo).
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class DocumentoModelTest {

    @Mock
    private DocumentoDAO documentoDAO;
    @Mock
    private PersonaDAO personaDAO;
    @Mock
    private TipoDocumentoDAO tipoDocumentoDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private DocumentoModel bean;

    // ---- btnCrearhandler() / relacionesCompletas() ----

    @Test
    public void btnCrearhandler_sinPersona_agregaMensajeDeErrorYNoCrea() {
        Documento doc = new Documento(UUID.randomUUID());
        doc.setIdTipoDocumento(new TipoDocumento(UUID.randomUUID())); // solo falta persona
        bean.setRegistro(doc);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(documentoDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_sinTipoDocumento_agregaMensajeDeErrorYNoCrea() {
        Documento doc = new Documento(UUID.randomUUID());
        doc.setIdPersona(new Persona(UUID.randomUUID())); // solo falta tipoDocumento
        bean.setRegistro(doc);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(documentoDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_relacionesCompletas_creaYRecargaLaLista() {
        Documento doc = new Documento(UUID.randomUUID());
        doc.setIdPersona(new Persona(UUID.randomUUID()));
        doc.setIdTipoDocumento(new TipoDocumento(UUID.randomUUID()));
        bean.setRegistro(doc);
        List<Documento> recargados = Arrays.asList(doc);
        when(documentoDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(documentoDAO).crear(doc);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- btnModificarHandler() / relacionesCompletas() ----

    @Test
    public void btnModificarHandler_sinPersona_agregaMensajeDeErrorYNoActualiza() {
        Documento doc = new Documento(UUID.randomUUID());
        doc.setIdTipoDocumento(new TipoDocumento(UUID.randomUUID()));
        bean.setRegistro(doc);

        bean.btnModificarHandler();

        verify(documentoDAO, never()).actualizar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_relacionesCompletas_actualizaYRecargaLaLista() {
        Documento doc = new Documento(UUID.randomUUID());
        doc.setIdPersona(new Persona(UUID.randomUUID()));
        doc.setIdTipoDocumento(new TipoDocumento(UUID.randomUUID()));
        bean.setRegistro(doc);
        when(documentoDAO.actualizar(doc)).thenReturn(doc);
        List<Documento> recargados = Arrays.asList(doc);
        when(documentoDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnModificarHandler();

        verify(documentoDAO).actualizar(doc);
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

    // ---- getTiposDocumento() ----

    @Test
    public void getTiposDocumento_cacheaEntreLlamadas() {
        when(tipoDocumentoDAO.findRange(0, 100)).thenReturn(Arrays.asList(new TipoDocumento(UUID.randomUUID())));

        bean.getTiposDocumento();
        bean.getTiposDocumento();

        verify(tipoDocumentoDAO, times(1)).findRange(0, 100);
    }
}