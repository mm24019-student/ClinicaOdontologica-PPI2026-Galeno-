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
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de ExamenModel. El flujo estándar de AbstracCrudModel ya está
 * cubierto por RolModelTest; aquí se prueba lo propio de ExamenModel: el
 * registro nuevo (activo por defecto), obtenerId/getRowKey, y
 * seleccionarExamen(), que es el que carga la pestaña "Tipos de Examen".
 */
@ExtendWith(MockitoExtension.class)
public class ExamenModelTest {

    @Mock
    private ExamenDAO examenDAO;
    @Mock
    private ExamenTipoExamenModel examenTipoExamenModel;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private ExamenModel bean;

    // ---- getDAO() / obtenerId() ----

    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(examenDAO, bean.getDAO());
    }

    @Test
    public void obtenerId_devuelveElIdDelExamen() {
        UUID id = UUID.randomUUID();

        assertEquals(id, bean.obtenerId(new Examen(id)));
    }

    // ---- btnNuevoHandler() / crearRegistroNuevo() ----

    @Test
    public void btnNuevoHandler_creaExamenActivoConEstadoCrear() {
        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(bean.getRegistro());
        assertNotNull(bean.getRegistro().getIdExamen());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    // ---- seleccionarExamen() ----

    @Test
    @SuppressWarnings("unchecked")
    public void seleccionarExamen_cargaLosTiposDelExamenSeleccionado() {
        Examen examen = new Examen(UUID.randomUUID());
        SelectEvent<Examen> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(examen);

        bean.seleccionarExamen(event);

        verify(examenTipoExamenModel).cargarPorExamen(examen);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_fijaElExamenYEstadoModificarSinTocarElHijo() {
        Examen examen = new Examen(UUID.randomUUID());
        SelectEvent<Examen> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(examen);

        bean.onRowSelect(event);

        assertSame(examen, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
        verifyNoInteractions(examenTipoExamenModel);
    }

    // ---- btnCrearhandler() ----

    @Test
    public void btnCrearhandler_exito_creaYRecargaLaLista() {
        Examen nuevo = new Examen(UUID.randomUUID());
        bean.setRegistro(nuevo);
        List<Examen> recargados = Arrays.asList(nuevo);
        when(examenDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(examenDAO).crear(nuevo);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }

    // ---- inicializar() / getRowKey() ----

    @Test
    public void inicializar_cargaLosPrimeros100Examenes() {
        List<Examen> esperado = Arrays.asList(new Examen(UUID.randomUUID()));
        when(examenDAO.findRange(0, 100)).thenReturn(esperado);

        bean.inicializar();

        assertEquals(esperado, bean.getregistros());
    }

    @Test
    public void getRowKey_devuelveElIdComoTexto() {
        Examen examen = new Examen(UUID.randomUUID());

        assertEquals(examen.getIdExamen().toString(), bean.getRowKey(examen));
    }
}