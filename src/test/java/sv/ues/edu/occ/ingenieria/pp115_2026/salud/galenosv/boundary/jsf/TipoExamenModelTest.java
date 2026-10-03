package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoExamenModelTest {

    @Mock
    private TipoExamenDAO tipoExamenDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private TipoExamenModel bean;

    @Test
    public void btnNuevoHandler_creaRegistroActivoConEstadoCrear() {
        bean.btnNuevoHandler(null);

        assertNotNull(bean.getRegistro());
        assertNotNull(bean.getRegistro().getIdTipoExamen());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void getRowKey_devuelveElIdTipoExamenComoTexto() {
        UUID id = UUID.randomUUID();

        assertEquals(id.toString(), bean.getRowKey(new TipoExamen(id)));
    }

    @Test
    public void btnCrearhandler_exito_creaYRecargaLaLista() {
        TipoExamen nuevo = new TipoExamen(UUID.randomUUID());
        bean.setRegistro(nuevo);
        List<TipoExamen> recargados = Arrays.asList(nuevo);
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(null);

        verify(tipoExamenDAO).crear(nuevo);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertNull(bean.getRegistro());
        assertEquals(recargados, bean.getregistros());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    @Test
    public void btnEliminarHandler_registroSeleccionado_eliminaPorId() {
        TipoExamen existente = new TipoExamen(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(existente));
        bean.setRegistro(existente);

        bean.btnEliminarHandler();

        verify(tipoExamenDAO).eliminar(existente.getIdTipoExamen());
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }
}