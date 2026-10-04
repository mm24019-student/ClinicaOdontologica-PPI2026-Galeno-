package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
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
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import org.primefaces.PrimeFaces;
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
        assertTrue(bean.mostrarDialogo);
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

    // =====================================================================
    // Pruebas agregadas: panel "Gestionar Examen" (agregar, eliminar, abrir,
    // lista del panel) y validación de exámenes inactivos.
    // =====================================================================

    // Mensaje que el bean mandó a la pantalla (cada test espera exactamente uno).
    private FacesMessage ultimoMensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        return captor.getValue();
    }

    // Ejecuta agregarExamen() con un examen activo. PrimeFaces.current() necesita un
    // FacesContext real (para ocultar el diálogo), así que aquí se simula.
    private ProcedimientoPasoExamen agregarYCapturar(String observaciones, PrimeFaces simulado) {
        try (MockedStatic<PrimeFaces> pf = mockStatic(PrimeFaces.class)) {
            pf.when(PrimeFaces::current).thenReturn(simulado);
            bean.setExamenPorAgregar(nuevoExamen("Radiografia"));
            bean.setObservacionesPorAgregar(observaciones);

            bean.agregarExamen(pasoPadre);
        }
        ArgumentCaptor<ProcedimientoPasoExamen> captor = ArgumentCaptor.forClass(ProcedimientoPasoExamen.class);
        verify(ppeDAO).crear(captor.capture());
        return captor.getValue();
    }

    // ---- agregarExamen() ----

    @Test
    public void agregarExamen_pasoNulo_avisaYNoTocaLaBase() {
        bean.agregarExamen(null);

        assertEquals(FacesMessage.SEVERITY_WARN, ultimoMensaje().getSeverity());
        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void agregarExamen_pasoSinGuardar_avisaYNoTocaLaBase() {
        bean.agregarExamen(new ProcedimientoPaso());   // sin id

        assertEquals(FacesMessage.SEVERITY_WARN, ultimoMensaje().getSeverity());
        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void agregarExamen_sinExamenElegido_muestraErrorYNoCrea() {
        bean.setExamenPorAgregar(null);

        bean.agregarExamen(pasoPadre);

        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void agregarExamen_examenInactivo_muestraErrorYNoCrea() {
        Examen inactivo = nuevoExamen("Radiografia");
        inactivo.setActivo(false);
        bean.setExamenPorAgregar(inactivo);

        bean.agregarExamen(pasoPadre);

        FacesMessage m = ultimoMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, m.getSeverity());
        assertEquals("Examen inactivo", m.getSummary());
        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void agregarExamen_examenYaAsignadoAlPaso_avisaYNoCreaDuplicado() {
        Examen rx = nuevoExamen("Radiografia");
        bean.setExamenPorAgregar(rx);
        when(ppeDAO.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso()))
                .thenReturn(Arrays.asList(nuevoPPE(rx, true)));

        bean.agregarExamen(pasoPadre);

        FacesMessage m = ultimoMensaje();
        assertEquals(FacesMessage.SEVERITY_WARN, m.getSeverity());
        assertEquals("Examen repetido", m.getSummary());
        verify(ppeDAO, never()).crear(any());
    }

    @Test
    public void agregarExamen_examenNuevo_creaElRegistroLigadoAlPasoYOcultaElDialogo() {
        PrimeFaces simulado = mock(PrimeFaces.class);

        ProcedimientoPasoExamen creado = agregarYCapturar("Urgente", simulado);

        assertNotNull(creado.getIdProcedimientoPasoExamen());
        assertSame(pasoPadre, creado.getIdProcedimientoPaso());
        assertEquals("Radiografia", creado.getIdExamen().getNombre());
        assertEquals(Boolean.TRUE, creado.getActivo());
        assertNotNull(creado.getFechaCreacion());
        assertEquals("Urgente", creado.getObservaciones());
        verify(simulado).executeScript("PF('dlgAgregarExamen').hide()");
        assertEquals(FacesMessage.SEVERITY_INFO, ultimoMensaje().getSeverity());
    }

    @Test
    public void agregarExamen_alTerminar_limpiaLoQueSeEscribioEnElDialogo() {
        agregarYCapturar("Urgente", mock(PrimeFaces.class));

        assertNull(bean.getExamenPorAgregar());
        assertNull(bean.getObservacionesPorAgregar());
    }

    @Test
    public void agregarExamen_observacionesConEspacios_seGuardanSinEllos() {
        assertEquals("Urgente", agregarYCapturar("   Urgente  ", mock(PrimeFaces.class)).getObservaciones());
    }

    @Test
    public void agregarExamen_sinObservaciones_usaElTextoPorDefectoPorqueLaEntidadNoAceptaBlancos() {
        assertEquals("Examen asignado al paso", agregarYCapturar(null, mock(PrimeFaces.class)).getObservaciones());
    }

    @Test
    public void agregarExamen_observacionesEnBlanco_usaElTextoPorDefecto() {
        assertEquals("Examen asignado al paso", agregarYCapturar("   ", mock(PrimeFaces.class)).getObservaciones());
    }

    @Test
    public void agregarExamen_siLaBaseFalla_muestraElErrorYConservaLoEscrito() {
        Examen rx = nuevoExamen("Radiografia");
        bean.setExamenPorAgregar(rx);
        doThrow(new IllegalStateException("fallo bd")).when(ppeDAO).crear(any());

        bean.agregarExamen(pasoPadre);

        FacesMessage m = ultimoMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, m.getSeverity());
        assertEquals("fallo bd", m.getDetail());
        assertSame(rx, bean.getExamenPorAgregar());
    }

    // ---- eliminarSeleccionado() ----

    @Test
    public void eliminarSeleccionado_sinSeleccion_avisaYNoTocaLaBase() {
        bean.setIdSeleccionado(null);

        bean.eliminarSeleccionado(pasoPadre);

        assertEquals(FacesMessage.SEVERITY_WARN, ultimoMensaje().getSeverity());
        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void eliminarSeleccionado_seleccionEnBlanco_avisaYNoTocaLaBase() {
        bean.setIdSeleccionado("   ");

        bean.eliminarSeleccionado(pasoPadre);

        assertEquals(FacesMessage.SEVERITY_WARN, ultimoMensaje().getSeverity());
        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void eliminarSeleccionado_elimina_recargaLaListaDelPasoYLimpiaLaSeleccion() {
        UUID id = UUID.randomUUID();
        bean.setIdSeleccionado(id.toString());

        bean.eliminarSeleccionado(pasoPadre);

        verify(ppeDAO).eliminar(id);
        verify(ppeDAO).findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso());
        assertNull(bean.getIdSeleccionado());
        assertEquals(FacesMessage.SEVERITY_INFO, ultimoMensaje().getSeverity());
    }

    @Test
    public void eliminarSeleccionado_siLaBaseFalla_muestraErrorYConservaLaSeleccion() {
        UUID id = UUID.randomUUID();
        bean.setIdSeleccionado(id.toString());
        doThrow(new IllegalStateException("fk")).when(ppeDAO).eliminar(id);

        bean.eliminarSeleccionado(pasoPadre);

        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
        assertEquals(id.toString(), bean.getIdSeleccionado());
    }

    @Test
    public void eliminarSeleccionado_idQueNoEsUuid_muestraErrorSinTocarLaBase() {
        bean.setIdSeleccionado("no-es-un-uuid");

        bean.eliminarSeleccionado(pasoPadre);

        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
        verify(ppeDAO, never()).eliminar(any());
    }

    // ---- examenesDe() (lista del panel) ----

    @Test
    public void examenesDe_pasoNulo_devuelveListaVaciaSinConsultar() {
        assertTrue(bean.examenesDe(null).isEmpty());

        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void examenesDe_pasoSinGuardar_devuelveListaVaciaSinConsultar() {
        assertTrue(bean.examenesDe(new ProcedimientoPaso()).isEmpty());

        verifyNoInteractions(ppeDAO);
    }

    @Test
    public void examenesDe_primeraVez_cargaLosExamenesDelPaso() {
        List<ProcedimientoPasoExamen> esperado = Arrays.asList(nuevoPPE(nuevoExamen("Radiografia"), true));
        when(ppeDAO.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso())).thenReturn(esperado);

        assertEquals(esperado, bean.examenesDe(pasoPadre));
    }

    @Test
    public void examenesDe_mismoPasoOtraVez_noVuelveALaBase() {
        bean.examenesDe(pasoPadre);
        bean.examenesDe(pasoPadre);

        verify(ppeDAO, times(1)).findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso());
    }

    @Test
    public void examenesDe_otroPaso_recargaLaLista() {
        ProcedimientoPaso otro = new ProcedimientoPaso(UUID.randomUUID());

        bean.examenesDe(pasoPadre);
        bean.examenesDe(otro);

        verify(ppeDAO, times(1)).findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso());
        verify(ppeDAO, times(1)).findByProcedimientoPaso(otro.getIdProcedimientoPaso());
    }

    @Test
    public void prepararAgregar_dejaElDialogoVacio() {
        bean.setExamenPorAgregar(nuevoExamen("Radiografia"));
        bean.setObservacionesPorAgregar("Urgente");

        bean.prepararAgregar();

        assertNull(bean.getExamenPorAgregar());
        assertNull(bean.getObservacionesPorAgregar());
    }

    // ---- etiquetaLista() ----

    @Test
    public void etiquetaLista_registroNulo_devuelveCadenaVacia() {
        assertEquals("", bean.etiquetaLista(null));
    }

    @Test
    public void etiquetaLista_sinExamen_devuelveCadenaVacia() {
        assertEquals("", bean.etiquetaLista(nuevoPPE(null, true)));
    }

    @Test
    public void etiquetaLista_sinObservaciones_muestraSoloElNombreDelExamen() {
        ProcedimientoPasoExamen r = nuevoPPE(nuevoExamen("Radiografia"), true);
        r.setObservaciones(null);

        assertEquals("Radiografia", bean.etiquetaLista(r));
    }

    @Test
    public void etiquetaLista_observacionesEnBlanco_muestraSoloElNombreDelExamen() {
        ProcedimientoPasoExamen r = nuevoPPE(nuevoExamen("Radiografia"), true);
        r.setObservaciones("   ");

        assertEquals("Radiografia", bean.etiquetaLista(r));
    }

    @Test
    public void etiquetaLista_conElTextoPorDefecto_noLoMuestra() {
        ProcedimientoPasoExamen r = nuevoPPE(nuevoExamen("Radiografia"), true);
        r.setObservaciones("Examen asignado al paso");

        assertEquals("Radiografia", bean.etiquetaLista(r));
    }

    @Test
    public void etiquetaLista_conObservacionPropia_laMuestraDespuesDelNombre() {
        ProcedimientoPasoExamen r = nuevoPPE(nuevoExamen("Radiografia"), true);
        r.setObservaciones("Urgente");

        assertEquals("Radiografia — Urgente", bean.etiquetaLista(r));
    }

    // ---- recargarLista() ----

    @Test
    public void recargarLista_refrescaLaListaDelPasoYCierraElDialogo() {
        bean.cargarDe(pasoPadre);
        bean.btnAbrirDialogo();

        bean.recargarLista();

        assertFalse(bean.mostrarDialogo);
        verify(ppeDAO, times(2)).findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso());
    }

    // ---- validarAntesDeGuardar() (no asignar exámenes inactivos) ----

    @Test
    public void validarAntesDeGuardar_sinExamen_muestraErrorYNoDejaGuardar() {
        bean.setRegistro(nuevoPPE(null, true));

        assertFalse(bean.validarAntesDeGuardar());
        assertEquals(FacesMessage.SEVERITY_ERROR, ultimoMensaje().getSeverity());
    }

    @Test
    public void validarAntesDeGuardar_examenInactivo_muestraErrorYNoDejaGuardar() {
        Examen inactivo = nuevoExamen("Radiografia");
        inactivo.setActivo(false);
        bean.setRegistro(nuevoPPE(inactivo, true));

        assertFalse(bean.validarAntesDeGuardar());
        assertEquals("Examen inactivo", ultimoMensaje().getSummary());
    }

    @Test
    public void validarAntesDeGuardar_examenActivo_dejaGuardarSinMensajes() {
        Examen activo = nuevoExamen("Radiografia");
        activo.setActivo(true);
        bean.setRegistro(nuevoPPE(activo, true));

        assertTrue(bean.validarAntesDeGuardar());
        verifyNoInteractions(fc);
    }

    // ---- getters / setters del diálogo ----

    @Test
    public void camposDelDialogo_guardanLoQueSeLesAsigna() {
        Examen rx = nuevoExamen("Radiografia");

        bean.setExamenPorAgregar(rx);
        bean.setObservacionesPorAgregar("Urgente");
        bean.setIdSeleccionado("abc");

        assertSame(rx, bean.getExamenPorAgregar());
        assertEquals("Urgente", bean.getObservacionesPorAgregar());
        assertEquals("abc", bean.getIdSeleccionado());
    }
}