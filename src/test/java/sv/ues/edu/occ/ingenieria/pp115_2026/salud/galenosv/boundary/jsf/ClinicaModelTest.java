package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClinicaModelTest {

    @Mock
    private ClinicaDAO clinicaDAO;
    @Mock
    private SesionBean sesionBean;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private ClinicaModel bean;

    @Test
    public void btnNuevoHandler_creaRegistroActivoConEstadoCrear() {
        bean.btnNuevoHandler(null);

        assertNotNull(bean.getRegistro().getIdClinica());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void inicializar_cargaLaListaYRefrescaLaSesion() {
        List<Clinica> clinicas = Arrays.asList(new Clinica(UUID.randomUUID()));
        when(clinicaDAO.findRange(0, 100)).thenReturn(clinicas);

        bean.inicializar();

        assertEquals(clinicas, bean.getRegistros());
        verify(sesionBean).refrescar();
    }

    @Test
    public void btnCrearhandler_exito_creaYRefrescaLaSesion() {
        Clinica nueva = new Clinica(UUID.randomUUID());
        bean.setRegistro(nueva);

        bean.btnCrearhandler(null);

        verify(clinicaDAO).crear(nueva);
        verify(sesionBean).refrescar();
    }

    @Test
    public void btnCrearhandler_daoLanzaExcepcion_noRefrescaLaSesion() {
        Clinica nueva = new Clinica(UUID.randomUUID());
        bean.setRegistro(nueva);
        doThrow(new RuntimeException("fallo bd")).when(clinicaDAO).crear(nueva);

        assertDoesNotThrow(() -> bean.btnCrearhandler(null));

        verify(sesionBean, never()).refrescar();
    }

    @Test
    public void btnEliminarHandler_exito_eliminaYRefrescaLaSesion() {
        Clinica existente = new Clinica(UUID.randomUUID());
        bean.setRegistros(Arrays.asList(existente));
        bean.setRegistro(existente);

        bean.btnEliminarHandler();

        verify(clinicaDAO).eliminar(existente.getIdClinica());
        verify(sesionBean).refrescar();
    }
}