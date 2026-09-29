package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 *
 * @author antonio
 **
 * Prueba de ProcedimientoPasoExamenModel (detalle de ProcedimientoPaso). El
 * comportamiento heredado de AbstracdetallecrudModel ya se prueba en
 * ProcedimientoPasoModelTest; aquí solo lo propio: registro nuevo con fecha y
 * activo, etiqueta del autocomplete, selección desde el autocomplete,
 * abrirGestionExamen() y el catálogo cacheado de exámenes.
 */
@ExtendWith(MockitoExtension.class)
public class ProcedimientoPasoExamenModelTest {
 
    @Mock
    private ProcedimientoPasoExamenDAO ppeDAO;
    @Mock
    private ExamenDAO eDAO;
    @Mock
    private FacesContext fc;
 
    @InjectMocks
    private ProcedimientoPasoExamenModel bean;
 
    private ProcedimientoPaso pasoPadre;
 
    @BeforeEach
    public void setUp() {
        pasoPadre = new ProcedimientoPaso(UUID.randomUUID());
    }
 
    private Examen nuevoExamen(String nombre) {
        Examen e = new Examen(UUID.randomUUID());
        e.setNombre(nombre);
        return e;
    }
 
    private ProcedimientoPasoExamen nuevoPPE(Examen examen, Boolean activo) {
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        r.setIdExamen(examen);
        r.setActivo(activo);
        return r;
    }
 
    @SuppressWarnings("unchecked")
    private SelectEvent<ProcedimientoPasoExamen> eventoCon(ProcedimientoPasoExamen r) {
        SelectEvent<ProcedimientoPasoExamen> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(r);
        return event;
    }
 
    // ---- getDAO() / obtenerId() / los 3 métodos del detalle ----
 
    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(ppeDAO, bean.getDAO());
    }
 
    @Test
    public void obtenerId_devuelveElIdDelRegistro() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, bean.obtenerId(new ProcedimientoPasoExamen(id)));
    }
 
    @Test
    public void obtenerIdPadre_devuelveElIdDelPaso() {
        assertEquals(pasoPadre.getIdProcedimientoPaso(), bean.obtenerIdPadre(pasoPadre));
    }
 
    @Test
    public void asignarPadre_ligaElExamenAlPaso() {
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
 
        bean.asignarPadre(r, pasoPadre);
 
        assertSame(pasoPadre, r.getIdProcedimientoPaso());
    }
 
    @Test
    public void buscarPorPadre_delegaEnFindByProcedimientoPaso() {
        List<ProcedimientoPasoExamen> esperado = Arrays.asList(nuevoPPE(null, true));
        when(ppeDAO.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso())).thenReturn(esperado);
 
        assertEquals(esperado, bean.buscarPorPadre(pasoPadre.getIdProcedimientoPaso()));
    }
 
    // ---- btnNuevoHandler() / crearRegistroNuevo() ----
 
    @Test
    public void btnNuevoHandler_creaRegistroActivoConFechaYLigadoAlPasoActual() {
        when(ppeDAO.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso()))
                .thenReturn(Collections.emptyList());
        bean.cargarDe(pasoPadre);
 
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertNotNull(bean.getRegistro().getIdProcedimientoPasoExamen());
        assertNotNull(bean.getRegistro().getFechaCreacion());
        assertEquals(Boolean.TRUE, bean.getRegistro().getActivo());
        assertSame(pasoPadre, bean.getRegistro().getIdProcedimientoPaso());
        assertNull(bean.getRegistro().getIdExamen());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }
 
    // ---- etiqueta() ----
 
    @Test
    public void etiqueta_registroNulo_devuelveCadenaVacia() {
        assertEquals("", bean.etiqueta(null));
    }
 
    @Test
    public void etiqueta_examenActivo_muestraNombreYActivo() {
        assertEquals("Radiografia — ACTIVO", bean.etiqueta(nuevoPPE(nuevoExamen("Radiografia"), true)));
    }
 
    @Test
    public void etiqueta_examenInactivo_muestraNombreEInactivo() {
        assertEquals("Radiografia — INACTIVO", bean.etiqueta(nuevoPPE(nuevoExamen("Radiografia"), false)));
    }
 
    @Test
    public void etiqueta_activoNulo_seTrataComoInactivo() {
        assertEquals("Radiografia — INACTIVO", bean.etiqueta(nuevoPPE(nuevoExamen("Radiografia"), null)));
    }
 
    @Test
    public void etiqueta_sinExamen_muestraSinExamen() {
        assertEquals("(sin examen) — ACTIVO", bean.etiqueta(nuevoPPE(null, true)));
    }
 
    @Test
    public void etiqueta_examenSinNombre_muestraSinExamen() {
        assertEquals("(sin examen) — ACTIVO", bean.etiqueta(nuevoPPE(nuevoExamen(null), true)));
    }
 
    // ---- completarExamenes() ----
 
    @Test
    public void completarExamenes_filtraSoloLosCargadosPorEtiquetaSinImportarMayusculas() {
        ProcedimientoPasoExamen rx = nuevoPPE(nuevoExamen("Radiografia"), true);
        ProcedimientoPasoExamen sangre = nuevoPPE(nuevoExamen("Hemograma"), true);
        bean.setWrappedData(Arrays.asList(rx, sangre));
 
        List<ProcedimientoPasoExamen> resultado = bean.completarExamenes("RADIO");
 
        assertEquals(1, resultado.size());
        assertSame(rx, resultado.get(0));
    }
 
    @Test
    public void completarExamenes_tambienBuscaPorElTextoDelEstado() {
        ProcedimientoPasoExamen activo = nuevoPPE(nuevoExamen("Radiografia"), true);
        ProcedimientoPasoExamen inactivo = nuevoPPE(nuevoExamen("Hemograma"), false);
        bean.setWrappedData(Arrays.asList(activo, inactivo));
 
        List<ProcedimientoPasoExamen> resultado = bean.completarExamenes("inactivo");
 
        assertEquals(1, resultado.size());
        assertSame(inactivo, resultado.get(0));
    }
 
    @Test
    public void completarExamenes_sinRegistrosCargados_devuelveListaVacia() {
        assertTrue(bean.completarExamenes("x").isEmpty());
    }
 
    // ---- onItemSelect() / btnSeleccionarHandler() ----
 
    @Test
    public void onItemSelect_seleccionaElRegistroElegidoDelAutocomplete() {
        ProcedimientoPasoExamen r = nuevoPPE(nuevoExamen("Radiografia"), true);
        bean.setWrappedData(Arrays.asList(r));
 
        bean.onItemSelect(eventoCon(r));
 
        assertSame(r, bean.getSeleccionAutocomplete());
        assertSame(r, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
    }
 
    @Test
    public void onItemSelect_objetoNulo_noSeleccionaNingunRegistro() {
        bean.onItemSelect(eventoCon(null));
 
        assertNull(bean.getSeleccionAutocomplete());
        assertNull(bean.getRegistro());
    }
 
    @Test
    public void btnSeleccionarHandler_conSeleccionAutocomplete_cargaElRegistro() {
        ProcedimientoPasoExamen r = nuevoPPE(nuevoExamen("Radiografia"), true);
        bean.setWrappedData(Arrays.asList(r));
        bean.setSeleccionAutocomplete(r);
 
        bean.btnSeleccionarHandler();
 
        assertSame(r, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
    }
 
    @Test
    public void btnSeleccionarHandler_sinSeleccionAutocomplete_noHaceNada() {
        bean.btnSeleccionarHandler();
 
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }
 
    // ---- abrirGestionExamen() / cargarDe() ----
 
    @Test
    public void abrirGestionExamen_recargaLosExamenesDelPasoYAbreElDialogo() {
        List<ProcedimientoPasoExamen> esperado = Arrays.asList(nuevoPPE(nuevoExamen("Radiografia"), true));
        when(ppeDAO.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso())).thenReturn(esperado);
 
        bean.abrirGestionExamen(pasoPadre);
 
        assertEquals(esperado, bean.getregistros());
        assertTrue(bean.isMostrarDialogo());
    }
 
    @Test
    public void cargarDe_limpiaLaSeleccionDelAutocomplete() {
        when(ppeDAO.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso()))
                .thenReturn(Collections.emptyList());
        bean.setSeleccionAutocomplete(nuevoPPE(nuevoExamen("Radiografia"), true));
 
        bean.cargarDe(pasoPadre);
 
        assertNull(bean.getSeleccionAutocomplete());
    }
 
    // ---- getExamenes() / completarExamenesFormulario() ----
 
    @Test
    public void getExamenes_cacheaElCatalogoEntreLlamadas() {
        when(eDAO.findRange(0, 100)).thenReturn(Arrays.asList(nuevoExamen("Radiografia")));
 
        bean.getExamenes();
        bean.getExamenes();
 
        verify(eDAO, times(1)).findRange(0, 100);
    }
 
    @Test
    public void completarExamenesFormulario_filtraPorNombreSinImportarMayusculas() {
        Examen rx = nuevoExamen("Radiografia");
        Examen sangre = nuevoExamen("Hemograma");
        when(eDAO.findRange(0, 100)).thenReturn(Arrays.asList(rx, sangre));
 
        List<Examen> resultado = bean.completarExamenesFormulario("hemo");
 
        assertEquals(1, resultado.size());
        assertSame(sangre, resultado.get(0));
    }
}
 
