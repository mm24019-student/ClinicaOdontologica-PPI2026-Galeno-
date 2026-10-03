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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoMedioContactoModelTest {

    @Mock
    private TipoMedioContactoDAO tipoMedioContactoDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private TipoMedioContactoModel bean;

    private void assertMensaje(FacesMessage.Severity esperada) {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(esperada, captor.getValue().getSeverity());
    }

    @Test
    public void btnNuevoHandler_creaRegistroActivoConEstadoCrear() {
        bean.btnNuevoHandler(null);

        assertNotNull(bean.getRegistro().getIdTipoMedioContacto());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void btnCrearhandler_regexValida_creaElRegistro() {
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        tipo.setExpresionRegular("^\\d{8}$");
        bean.setRegistro(tipo);

        bean.btnCrearhandler(null);

        verify(tipoMedioContactoDAO).crear(tipo);
        assertMensaje(FacesMessage.SEVERITY_INFO);
    }

    @Test
    public void btnCrearhandler_regexVacia_creaElRegistro() {
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        bean.setRegistro(tipo);

        bean.btnCrearhandler(null);

        verify(tipoMedioContactoDAO).crear(tipo);
    }

    @Test
    public void btnCrearhandler_regexConSintaxisInvalida_agregaErrorYNoCrea() {
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        tipo.setExpresionRegular("[abc");
        bean.setRegistro(tipo);

        bean.btnCrearhandler(null);

        verify(tipoMedioContactoDAO, never()).crear(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnModificarHandler_regexConSintaxisInvalida_agregaErrorYNoActualiza() {
        TipoMedioContacto tipo = new TipoMedioContacto(UUID.randomUUID());
        tipo.setExpresionRegular("(sin cerrar");
        bean.setRegistro(tipo);

        bean.btnModificarHandler();

        verify(tipoMedioContactoDAO, never()).actualizar(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }
}