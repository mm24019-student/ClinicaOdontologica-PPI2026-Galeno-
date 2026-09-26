package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de PersonaDocumentoModel. Representa también a
 * PersonaMedioContactoModel (mismo patrón, cambiando el nombre del campo).
 * Cubre dos cosas: el contrato con AbstracdetallecrudModel propio de esta
 * clase (buscarPorPadre/asignarPadre, vía cargarDe/btnNuevoHandler) y la
 * validación relacionesCompletas() que envuelve btnCrearhandler/
 * btnModificarHandler.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class PersonaDocumentoModelTest {

    @Mock
    private DocumentoDAO documentoDAO;
    @Mock
    private TipoDocumentoDAO tipoDocumentoDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private PersonaDocumentoModel bean;

    // ---- buscarPorPadre() / cargarDe() ----

    @Test
    public void cargarDe_conPersona_delegaEnDocumentoDaoFindByPersona() {
        Persona persona = new Persona(UUID.randomUUID());
        List<Documento> esperado = Arrays.asList(new Documento(UUID.randomUUID()));
        when(documentoDAO.findByPersona(persona.getIdPersona())).thenReturn(esperado);

        bean.cargarDe(persona);

        assertEquals(esperado, bean.getregistros());
    }

    @Test
    public void cargarDe_conPadreNulo_dejaLaListaVacia() {
        bean.cargarDe(null);

        assertTrue(bean.getregistros().isEmpty());
        verifyNoInteractions(documentoDAO);
    }

    // ---- asignarPadre() (vía btnNuevoHandler, heredado de AbstracCrudModel) ----

    @Test
    public void btnNuevoHandler_asignaLaPersonaActualAlDocumentoNuevo() {
        Persona persona = new Persona(UUID.randomUUID());
        when(documentoDAO.findByPersona(persona.getIdPersona())).thenReturn(Collections.emptyList());
        bean.cargarDe(persona); // fija padreActual

        bean.btnNuevoHandler(null);

        assertNotNull(bean.getRegistro());
        assertSame(persona, bean.getRegistro().getIdPersona());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    // ---- btnCrearhandler() / relacionesCompletas() ----

    @Test
    public void btnCrearhandler_sinTipoDocumento_agregaMensajeDeErrorYNoCrea() {
        Documento doc = new Documento(UUID.randomUUID()); // idTipoDocumento queda null
        bean.setRegistro(doc);

        bean.btnCrearhandler(null);

        verify(documentoDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_conTipoDocumento_creaYRecargaFiltradoPorPersona() {
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
        Documento doc = new Documento(UUID.randomUUID());
        doc.setIdTipoDocumento(tipo);
        doc.setIdPersona(persona);
        bean.padreActual = persona;
        bean.setRegistro(doc);
        List<Documento> recargados = Arrays.asList(doc);
        when(documentoDAO.findByPersona(persona.getIdPersona())).thenReturn(recargados);

        bean.btnCrearhandler(null);

        verify(documentoDAO).crear(doc);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertNull(bean.getRegistro());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- btnModificarHandler() / relacionesCompletas() ----

    @Test
    public void btnModificarHandler_sinTipoDocumento_agregaMensajeDeErrorYNoActualiza() {
        Documento doc = new Documento(UUID.randomUUID());
        bean.setRegistro(doc);

        bean.btnModificarHandler();

        verify(documentoDAO, never()).actualizar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_conTipoDocumento_actualizaYRecargaFiltradoPorPersona() {
        Persona persona = new Persona(UUID.randomUUID());
        TipoDocumento tipo = new TipoDocumento(UUID.randomUUID());
        Documento doc = new Documento(UUID.randomUUID());
        doc.setIdTipoDocumento(tipo);
        doc.setIdPersona(persona);
        bean.padreActual = persona;
        bean.setRegistro(doc);
        when(documentoDAO.actualizar(doc)).thenReturn(doc);
        List<Documento> recargados = Arrays.asList(doc);
        when(documentoDAO.findByPersona(persona.getIdPersona())).thenReturn(recargados);

        bean.btnModificarHandler();

        verify(documentoDAO).actualizar(doc);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertNull(bean.getRegistro());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- getTiposDocumento() ----

    @Test
    public void getTiposDocumento_cacheaEntreLlamadas() {
        when(tipoDocumentoDAO.findRange(0, 100)).thenReturn(Arrays.asList(new TipoDocumento(UUID.randomUUID())));

        bean.getTiposDocumento();
        bean.getTiposDocumento();

        verify(tipoDocumentoDAO, times(1)).findRange(0, 100);
    }

    // ---- completarTiposDocumento() ----

    @Test
    public void completarTiposDocumento_filtraPorNombreSinImportarMayusculas() {
        TipoDocumento dui = new TipoDocumento(UUID.randomUUID());
        dui.setNombre("DUI");
        TipoDocumento pasaporte = new TipoDocumento(UUID.randomUUID());
        pasaporte.setNombre("Pasaporte");
        when(tipoDocumentoDAO.findRange(0, 100)).thenReturn(Arrays.asList(dui, pasaporte));

        List<TipoDocumento> resultado = bean.completarTiposDocumento("dui");

        assertEquals(1, resultado.size());
        assertSame(dui, resultado.get(0));
    }
}