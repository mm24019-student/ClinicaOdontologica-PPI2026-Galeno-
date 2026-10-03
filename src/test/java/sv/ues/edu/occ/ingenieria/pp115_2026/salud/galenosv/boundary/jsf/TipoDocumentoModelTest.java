package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoDocumentoModelTest {

    @Mock
    private TipoDocumentoDAO tipoDocumentoDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private TipoDocumentoModel bean;

    private void assertMensaje(FacesMessage.Severity esperada) {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(esperada, captor.getValue().getSeverity());
    }

    private TipoDocumento tipo(String nombre, String regex) {
        TipoDocumento t = new TipoDocumento(UUID.randomUUID());
        t.setNombre(nombre);
        t.setExpresionRegular(regex);
        return t;
    }

    @Test
    public void btnNuevoHandler_creaRegistroActivoConEstadoCrear() {
        bean.btnNuevoHandler(null);

        assertNotNull(bean.getRegistro().getIdTipoDocumento());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void btnCrearhandler_nombreUnicoYRegexValida_creaElRegistro() {
        TipoDocumento t = tipo("DUI", "^\\d{9}$");
        bean.setRegistro(t);

        bean.btnCrearhandler(null);

        verify(tipoDocumentoDAO).crear(t);
        assertMensaje(FacesMessage.SEVERITY_INFO);
    }

    @Test
    public void btnCrearhandler_nombreDuplicado_agregaErrorYNoCrea() {
        TipoDocumento t = tipo("  DUI ", null);
        bean.setRegistro(t);
        when(tipoDocumentoDAO.existeNombre("  DUI ", t.getIdTipoDocumento())).thenReturn(true);

        bean.btnCrearhandler(null);

        verify(tipoDocumentoDAO, never()).crear(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnCrearhandler_regexConSintaxisInvalida_agregaErrorYNoCrea() {
        TipoDocumento t = tipo("Pasaporte", "[abc");
        bean.setRegistro(t);

        bean.btnCrearhandler(null);

        verify(tipoDocumentoDAO, never()).crear(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnModificarHandler_nombreDuplicado_agregaErrorYNoActualiza() {
        TipoDocumento t = tipo("NIT", null);
        bean.setRegistro(t);
        when(tipoDocumentoDAO.existeNombre("NIT", t.getIdTipoDocumento())).thenReturn(true);

        bean.btnModificarHandler();

        verify(tipoDocumentoDAO, never()).actualizar(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnModificarHandler_nombreUnico_actualizaElRegistro() {
        TipoDocumento t = tipo("NIT", null);
        bean.setRegistro(t);
        when(tipoDocumentoDAO.actualizar(t)).thenReturn(t);

        bean.btnModificarHandler();

        verify(tipoDocumentoDAO).actualizar(t);
        assertMensaje(FacesMessage.SEVERITY_INFO);
    }
}