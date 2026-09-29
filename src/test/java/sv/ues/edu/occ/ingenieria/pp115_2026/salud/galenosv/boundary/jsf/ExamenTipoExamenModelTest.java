package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.ArrayList;
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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenTipoExamen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba de ExamenTipoExamenModel (mini-CRUD de la pestaña "Tipos de Examen"
 * dentro de Examen.xhtml): carga filtrada por examen, examen padre en los
 * registros nuevos, validación de las dos relaciones y el autocomplete de
 * TipoExamen.
 */
@ExtendWith(MockitoExtension.class)
public class ExamenTipoExamenModelTest {

    @Mock
    private ExamenTipoExamenDAO examenTipoExamenDAO;
    @Mock
    private ExamenDAO examenDAO;
    @Mock
    private TipoExamenDAO tipoExamenDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private ExamenTipoExamenModel bean;

    private Examen examen;
    private TipoExamen tipo;

    @BeforeEach
    public void setUp() {
        examen = new Examen(UUID.randomUUID());
        examen.setNombre("Hemograma completo");
        tipo = new TipoExamen(UUID.randomUUID());
        tipo.setNombre("Hematologia");
        tipo.setActivo(Boolean.TRUE);
    }

    private TipoExamen nuevoTipo(String nombre, Boolean activo) {
        TipoExamen t = new TipoExamen(UUID.randomUUID());
        t.setNombre(nombre);
        t.setActivo(activo);
        return t;
    }
    
    

    // ---- getDAO() / obtenerId() ----

    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(examenTipoExamenDAO, bean.getDAO());
    }

    @Test
    public void obtenerId_devuelveElIdDelRegistro() {
        UUID id = UUID.randomUUID();

        assertEquals(id, bean.obtenerId(new ExamenTipoExamen(id)));
    }

    // ---- cargarPorExamen() ----

    @Test
    public void cargarPorExamen_examenValido_cargaSoloSusTiposYReseteaElEstado() {
        List<ExamenTipoExamen> esperado = Arrays.asList(new ExamenTipoExamen(UUID.randomUUID()));
        when(examenTipoExamenDAO.findByExamen(examen.getIdExamen())).thenReturn(esperado);
        bean.setRegistro(new ExamenTipoExamen(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);

        bean.cargarPorExamen(examen);

        assertEquals(esperado, bean.getregistros());
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }

    @Test
    public void cargarPorExamen_examenNulo_dejaListaVaciaSinConsultar() {
        bean.cargarPorExamen(null);

        assertTrue(bean.getregistros().isEmpty());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        verify(examenTipoExamenDAO, never()).findByExamen(any());
    }

    @Test
    public void cargarPorExamen_examenSinId_dejaListaVaciaSinConsultar() {
        bean.cargarPorExamen(new Examen());

        assertTrue(bean.getregistros().isEmpty());
        verify(examenTipoExamenDAO, never()).findByExamen(any());
    }

    // ---- btnNuevoHandler() / crearRegistroNuevo() ----

    @Test
    public void btnNuevoHandler_sinExamenPadre_creaRegistroConFechaYSinExamen() {
        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertNotNull(bean.getRegistro().getIdExamenTipoExamen());
        assertNotNull(bean.getRegistro().getFechaCreacion());
        assertNull(bean.getRegistro().getIdExamen());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    @Test
    public void btnNuevoHandler_conExamenPadre_ligaElRegistroAlExamen() {
        when(examenTipoExamenDAO.findByExamen(examen.getIdExamen())).thenReturn(Collections.emptyList());
        bean.cargarPorExamen(examen);

        bean.btnNuevoHandler(mock(ActionEvent.class));

        assertSame(examen, bean.getRegistro().getIdExamen());
        assertNull(bean.getRegistro().getIdTipoExamen());
    }

    // ---- btnCrearhandler() / relacionesCompletas() ----

    @Test
    public void btnCrearhandler_registroNulo_agregaMensajeDeErrorYNoCrea() {
        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(examenTipoExamenDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_sinExamen_agregaMensajeDeErrorYNoCrea() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setIdTipoExamen(tipo);
        bean.setRegistro(r);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(examenTipoExamenDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_sinTipoExamen_agregaMensajeDeErrorYNoCrea() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setIdExamen(examen);
        bean.setRegistro(r);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(examenTipoExamenDAO, never()).crear(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnCrearhandler_relacionesCompletas_creaYRecargaLaLista() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setIdExamen(examen);
        r.setIdTipoExamen(tipo);
        bean.setRegistro(r);
        List<ExamenTipoExamen> recargados = Arrays.asList(r);
        when(examenTipoExamenDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnCrearhandler(mock(ActionEvent.class));

        verify(examenTipoExamenDAO).crear(r);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- btnModificarHandler() / relacionesCompletas() ----

    @Test
    public void btnModificarHandler_sinTipoExamen_agregaMensajeDeErrorYNoActualiza() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setIdExamen(examen);
        bean.setRegistro(r);

        bean.btnModificarHandler();

        verify(examenTipoExamenDAO, never()).actualizar(any());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    public void btnModificarHandler_relacionesCompletas_actualizaYRecargaLaLista() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setIdExamen(examen);
        r.setIdTipoExamen(tipo);
        bean.setRegistro(r);
        List<ExamenTipoExamen> recargados = Arrays.asList(r);
        when(examenTipoExamenDAO.actualizar(r)).thenReturn(r);
        when(examenTipoExamenDAO.findRange(0, 100)).thenReturn(recargados);

        bean.btnModificarHandler();

        verify(examenTipoExamenDAO).actualizar(r);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertEquals(recargados, bean.getregistros());
    }

    // ---- getExamenes() / getTiposExamen() ----

    @Test
    public void getExamenes_cacheaEntreLlamadas() {
        when(examenDAO.findRange(0, 100)).thenReturn(Arrays.asList(examen));

        bean.getExamenes();
        bean.getExamenes();

        verify(examenDAO, times(1)).findRange(0, 100);
    }

    @Test
    public void getTiposExamen_cacheaEntreLlamadas() {
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(tipo));

        bean.getTiposExamen();
        bean.getTiposExamen();

        verify(tipoExamenDAO, times(1)).findRange(0, 100);
    }

    // ---- completarTiposExamen() ----

    @Test
    public void completarTiposExamen_filtraPorNombreSinImportarMayusculas() {
        TipoExamen otro = nuevoTipo("Quimica sanguinea", true);
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(tipo, otro));

        List<TipoExamen> resultado = bean.completarTiposExamen("HEMA");

        assertEquals(1, resultado.size());
        assertSame(tipo, resultado.get(0));
    }

    @Test
    public void completarTiposExamen_consultaVacia_devuelveTodosLosActivos() {
        TipoExamen otro = nuevoTipo("Quimica sanguinea", true);
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(tipo, otro));

        assertEquals(2, bean.completarTiposExamen("").size());
    }

    @Test
    public void completarTiposExamen_consultaNula_seTrataComoVacia() {
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(tipo));

        assertEquals(1, bean.completarTiposExamen(null).size());
    }

    @Test
    public void completarTiposExamen_excluyeLosInactivos() {
        TipoExamen inactivo = nuevoTipo("Hematologia vieja", false);
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(tipo, inactivo));

        List<TipoExamen> resultado = bean.completarTiposExamen("hema");

        assertEquals(1, resultado.size());
        assertSame(tipo, resultado.get(0));
    }

    @Test
    public void completarTiposExamen_activoNulo_seIncluye() {
        TipoExamen sinEstado = nuevoTipo("Hematologia sin estado", null);
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(sinEstado));

        assertEquals(1, bean.completarTiposExamen("hema").size());
    }

    @Test
    public void completarTiposExamen_nombreNulo_soloCoincideConConsultaVacia() {
        TipoExamen sinNombre = nuevoTipo(null, true);
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(sinNombre));

        assertEquals(1, bean.completarTiposExamen("").size());
        assertEquals(0, bean.completarTiposExamen("hema").size());
    }

    @Test
    public void completarTiposExamen_limitaA20Resultados() {
        List<TipoExamen> muchos = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            muchos.add(nuevoTipo("Tipo " + i, true));
        }
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(muchos);

        assertEquals(20, bean.completarTiposExamen("tipo").size());
    }

    @Test
    public void completarTiposExamen_cacheaElCatalogoEntreLlamadas() {
        when(tipoExamenDAO.findRange(0, 100)).thenReturn(Arrays.asList(tipo));

        bean.completarTiposExamen("");
        bean.completarTiposExamen("hema");

        verify(tipoExamenDAO, times(1)).findRange(0, 100);
    }
}