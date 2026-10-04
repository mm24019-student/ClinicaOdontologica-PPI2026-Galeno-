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
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 ***
 * Prueba de ProcedimientoPasoSecuenciaModel (detalle de ProcedimientoPaso).
 * El comportamiento heredado de AbstracdetallecrudModel ya se prueba en
 * ProcedimientoPasoModelTest; aquí solo lo propio: paso de referencia
 * (UUID suelto en la entidad, objeto en el autocomplete), etiqueta del
 * autocomplete de búsqueda, nombrePaso() con caché + búsqueda puntual,
 * y la limpieza de selecciones al cambiar de paso o cerrar el diálogo.
 */
@ExtendWith(MockitoExtension.class)
public class ProcedimientoPasoSecuenciaModelTest {
 
    @Mock
    private ProcedimientoPasoSecuenciaDAO dao;
    @Mock
    private ProcedimientoPasoDAO pasoDAO;
 
    @InjectMocks
    private ProcedimientoPasoSecuenciaModel bean;
 
    private ProcedimientoPaso pasoPadre;
 
    @BeforeEach
    public void setUp() {
        pasoPadre = new ProcedimientoPaso(UUID.randomUUID());
    }
 
    private ProcedimientoPaso nuevoPaso(String nombre) {
        ProcedimientoPaso p = new ProcedimientoPaso(UUID.randomUUID());
        p.setNombre(nombre);
        return p;
    }
 
    private ProcedimientoPasoSecuencia nuevaSecuencia(String tipo, UUID idReferencia) {
        ProcedimientoPasoSecuencia s = new ProcedimientoPasoSecuencia(UUID.randomUUID());
        s.setTipoSecuencia(tipo);
        s.setIdProcedimientoPasoReferencia(idReferencia);
        return s;
    }
 
    @SuppressWarnings("unchecked")
    private SelectEvent<ProcedimientoPasoSecuencia> eventoCon(ProcedimientoPasoSecuencia s) {
        SelectEvent<ProcedimientoPasoSecuencia> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(s);
        return event;
    }
 
    // ---- getDAO() / obtenerId() / los 3 métodos del detalle ----
 
    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(dao, bean.getDAO());
    }
 
    @Test
    public void obtenerId_devuelveElIdDeLaSecuencia() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, bean.obtenerId(new ProcedimientoPasoSecuencia(id)));
    }
 
    @Test
    public void obtenerIdPadre_devuelveElIdDelPaso() {
        assertEquals(pasoPadre.getIdProcedimientoPaso(), bean.obtenerIdPadre(pasoPadre));
    }
 
    @Test
    public void asignarPadre_ligaLaSecuenciaAlPaso() {
        ProcedimientoPasoSecuencia s = new ProcedimientoPasoSecuencia(UUID.randomUUID());
 
        bean.asignarPadre(s, pasoPadre);
 
        assertSame(pasoPadre, s.getIdProcedimientoPaso());
    }
 
    @Test
    public void buscarPorPadre_delegaEnFindByProcedimientoPaso() {
        List<ProcedimientoPasoSecuencia> esperado = Arrays.asList(nuevaSecuencia("SIGUIENTE", null));
        when(dao.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso())).thenReturn(esperado);
 
        assertEquals(esperado, bean.buscarPorPadre(pasoPadre.getIdProcedimientoPaso()));
    }
 
    // ---- btnNuevoHandler() / crearRegistroNuevo() ----
 
    @Test
    public void btnNuevoHandler_creaSecuenciaLigadaAlPasoActualYSinPasoReferencia() {
        when(dao.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso()))
                .thenReturn(Collections.emptyList());
        bean.cargarDe(pasoPadre);
        bean.setPasoReferenciaSeleccionado(nuevoPaso("Anestesia local"));
 
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertNotNull(bean.getRegistro().getIdProcedimientoPasoSecuencia());
        assertSame(pasoPadre, bean.getRegistro().getIdProcedimientoPaso());
        assertNull(bean.getRegistro().getTipoSecuencia());
        assertNull(bean.getPasoReferenciaSeleccionado());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }
 
    // ---- setPasoReferenciaSeleccionado() ----
 
    @Test
    public void setPasoReferenciaSeleccionado_conRegistro_guardaElUuidDelPasoEnLaSecuencia() {
        ProcedimientoPaso referencia = nuevoPaso("Anestesia local");
        bean.setRegistro(new ProcedimientoPasoSecuencia(UUID.randomUUID()));
 
        bean.setPasoReferenciaSeleccionado(referencia);
 
        assertSame(referencia, bean.getPasoReferenciaSeleccionado());
        assertEquals(referencia.getIdProcedimientoPaso(),
                bean.getRegistro().getIdProcedimientoPasoReferencia());
    }
 
    @Test
    public void setPasoReferenciaSeleccionado_nulo_limpiaElUuidDeReferencia() {
        bean.setRegistro(nuevaSecuencia("SIGUIENTE", UUID.randomUUID()));
 
        bean.setPasoReferenciaSeleccionado(null);
 
        assertNull(bean.getPasoReferenciaSeleccionado());
        assertNull(bean.getRegistro().getIdProcedimientoPasoReferencia());
    }
 
    @Test
    public void setPasoReferenciaSeleccionado_sinRegistro_noLanzaExcepcion() {
        ProcedimientoPaso referencia = nuevoPaso("Anestesia local");
 
        assertDoesNotThrow(() -> bean.setPasoReferenciaSeleccionado(referencia));
 
        assertSame(referencia, bean.getPasoReferenciaSeleccionado());
    }
 
    // ---- btnSeleccionarRegistro() / sincronizarPasoReferencia() ----
 
    @Test
    public void btnSeleccionarRegistro_referenciaEnElCatalogo_fijaElPasoSinBuscarPorId() {
        ProcedimientoPaso referencia = nuevoPaso("Anestesia local");
        ProcedimientoPasoSecuencia s = nuevaSecuencia("SIGUIENTE", referencia.getIdProcedimientoPaso());
        when(pasoDAO.findRange(0, 100)).thenReturn(Arrays.asList(referencia));
        bean.setWrappedData(Arrays.asList(s));
 
        bean.btnSeleccionarRegistro(s.getIdProcedimientoPasoSecuencia());
 
        assertSame(s, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
        assertSame(referencia, bean.getPasoReferenciaSeleccionado());
        verify(pasoDAO, never()).buscar(any());
    }
 
    @Test
    public void btnSeleccionarRegistro_referenciaFueraDelCatalogo_laBuscaPorId() {
        ProcedimientoPaso referencia = nuevoPaso("Paso lejano");
        ProcedimientoPasoSecuencia s = nuevaSecuencia("SIGUIENTE", referencia.getIdProcedimientoPaso());
        when(pasoDAO.findRange(0, 100)).thenReturn(Collections.emptyList());
        when(pasoDAO.buscar(referencia.getIdProcedimientoPaso())).thenReturn(referencia);
        bean.setWrappedData(Arrays.asList(s));
 
        bean.btnSeleccionarRegistro(s.getIdProcedimientoPasoSecuencia());
 
        assertSame(referencia, bean.getPasoReferenciaSeleccionado());
    }
 
    @Test
    public void btnSeleccionarRegistro_referenciaInexistente_dejaPasoReferenciaEnNull() {
        UUID idReferencia = UUID.randomUUID();
        ProcedimientoPasoSecuencia s = nuevaSecuencia("SIGUIENTE", idReferencia);
        when(pasoDAO.findRange(0, 100)).thenReturn(Collections.emptyList());
        when(pasoDAO.buscar(idReferencia)).thenReturn(null);
        bean.setWrappedData(Arrays.asList(s));
 
        bean.btnSeleccionarRegistro(s.getIdProcedimientoPasoSecuencia());
 
        assertNull(bean.getPasoReferenciaSeleccionado());
    }
 
    @Test
    public void btnSeleccionarRegistro_sinReferencia_dejaPasoReferenciaEnNullSinConsultar() {
        ProcedimientoPasoSecuencia s = nuevaSecuencia("FIN", null);
        bean.setWrappedData(Arrays.asList(s));
        bean.setPasoReferenciaSeleccionado(nuevoPaso("Paso anterior"));
 
        bean.btnSeleccionarRegistro(s.getIdProcedimientoPasoSecuencia());
 
        assertNull(bean.getPasoReferenciaSeleccionado());
        verifyNoInteractions(pasoDAO);
    }
 
    @Test
    public void btnSeleccionarRegistro_idNulo_noSeleccionaNada() {
        bean.btnSeleccionarRegistro(null);
 
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }
 
    // ---- onItemSelect() / btnSeleccionarHandler() ----
 
    @Test
    public void onItemSelect_seleccionaLaSecuenciaElegidaDelAutocomplete() {
        ProcedimientoPasoSecuencia s = nuevaSecuencia("FIN", null);
        bean.setWrappedData(Arrays.asList(s));
 
        bean.onItemSelect(eventoCon(s));
 
        assertSame(s, bean.getSeleccionAutocomplete());
        assertSame(s, bean.getRegistro());
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
        ProcedimientoPasoSecuencia s = nuevaSecuencia("FIN", null);
        bean.setWrappedData(Arrays.asList(s));
        bean.setSeleccionAutocomplete(s);
 
        bean.btnSeleccionarHandler();
 
        assertSame(s, bean.getRegistro());
        assertEquals(Estado_Crud.MODIFICAR, bean.getEstado());
    }
 
    @Test
    public void btnSeleccionarHandler_sinSeleccionAutocomplete_noHaceNada() {
        bean.btnSeleccionarHandler();
 
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }
 
    // ---- etiqueta() ----
 
    @Test
    public void etiqueta_secuenciaNula_devuelveCadenaVacia() {
        assertEquals("", bean.etiqueta(null));
    }
 
    @Test
    public void etiqueta_tipoNulo_muestraSinTipo() {
        assertEquals("(sin tipo)", bean.etiqueta(nuevaSecuencia(null, null)));
    }
 
    @Test
    public void etiqueta_tipoEnBlanco_muestraSinTipo() {
        assertEquals("(sin tipo)", bean.etiqueta(nuevaSecuencia("   ", null)));
    }
 
    @Test
    public void etiqueta_tipoSinReferencia_devuelveSoloElTipo() {
        assertEquals("FIN", bean.etiqueta(nuevaSecuencia("FIN", null)));
    }
 
    @Test
    public void etiqueta_tipoConReferencia_devuelveTipoYNombreDelPaso() {
        ProcedimientoPaso referencia = nuevoPaso("Anestesia local");
        when(pasoDAO.findRange(0, 100)).thenReturn(Arrays.asList(referencia));
 
        String resultado = bean.etiqueta(nuevaSecuencia("SIGUIENTE", referencia.getIdProcedimientoPaso()));
 
        assertEquals("SIGUIENTE — Anestesia local", resultado);
    }
 
    // ---- completarSecuencias() ----
 
    @Test
    public void completarSecuencias_filtraSoloLasCargadasPorLaEtiquetaSinImportarMayusculas() {
        ProcedimientoPasoSecuencia siguiente = nuevaSecuencia("SIGUIENTE", null);
        ProcedimientoPasoSecuencia alternativa = nuevaSecuencia("ALTERNATIVA", null);
        bean.setWrappedData(Arrays.asList(siguiente, alternativa));
 
        List<ProcedimientoPasoSecuencia> resultado = bean.completarSecuencias("sig");
 
        assertEquals(1, resultado.size());
        assertSame(siguiente, resultado.get(0));
    }
 
    @Test
    public void completarSecuencias_consultaNula_devuelveTodasLasCargadas() {
        bean.setWrappedData(Arrays.asList(nuevaSecuencia("SIGUIENTE", null), nuevaSecuencia("FIN", null)));
 
        assertEquals(2, bean.completarSecuencias(null).size());
    }
 
    @Test
    public void completarSecuencias_sinRegistrosCargados_devuelveListaVacia() {
        assertTrue(bean.completarSecuencias("x").isEmpty());
    }
 
    // ---- abrirGestionSecuencia() ----
 
    @Test
    public void abrirGestionSecuencia_recargaLasSecuenciasDelPasoYAbreElDialogo() {
        List<ProcedimientoPasoSecuencia> esperado = Arrays.asList(nuevaSecuencia("FIN", null));
        when(dao.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso())).thenReturn(esperado);
 
        bean.abrirGestionSecuencia(pasoPadre);
 
        assertEquals(esperado, bean.getregistros());
        assertTrue(bean.mostrarDialogo);
    }
 
    // ---- cargarDe() / btnCerrarDialogo() ----
 
    @Test
    public void cargarDe_limpiaElAutocompleteYElPasoDeReferencia() {
        when(dao.findByProcedimientoPaso(pasoPadre.getIdProcedimientoPaso()))
                .thenReturn(Collections.emptyList());
        bean.setSeleccionAutocomplete(nuevaSecuencia("FIN", null));
        bean.setPasoReferenciaSeleccionado(nuevoPaso("Anestesia local"));
 
        bean.cargarDe(pasoPadre);
 
        assertNull(bean.getSeleccionAutocomplete());
        assertNull(bean.getPasoReferenciaSeleccionado());
    }
 
    @Test
    public void btnCerrarDialogo_cierraYLimpiaSeleccionesYRegistro() {
        bean.btnAbrirDialogo();
        bean.setRegistro(nuevaSecuencia("FIN", null));
        bean.setEstado(Estado_Crud.MODIFICAR);
        bean.setSeleccionAutocomplete(nuevaSecuencia("FIN", null));
        bean.setPasoReferenciaSeleccionado(nuevoPaso("Anestesia local"));
 
        bean.btnCerrarDialogo();
 
        assertFalse(bean.mostrarDialogo);
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertNull(bean.getSeleccionAutocomplete());
        assertNull(bean.getPasoReferenciaSeleccionado());
    }
 
    // ---- getPasos() / completarPasos() ----
 
    @Test
    public void getPasos_cacheaElCatalogoEntreLlamadas() {
        when(pasoDAO.findRange(0, 100)).thenReturn(Arrays.asList(nuevoPaso("Anestesia local")));
 
        bean.getPasos();
        bean.getPasos();
 
        verify(pasoDAO, times(1)).findRange(0, 100);
    }
 
    @Test
    public void completarPasos_filtraPorNombreSinImportarMayusculas() {
        ProcedimientoPaso anestesia = nuevoPaso("Anestesia local");
        ProcedimientoPaso limpieza = nuevoPaso("Limpieza dental");
        when(pasoDAO.findRange(0, 100)).thenReturn(Arrays.asList(anestesia, limpieza));
 
        List<ProcedimientoPaso> resultado = bean.completarPasos("ANEST");
 
        assertEquals(1, resultado.size());
        assertSame(anestesia, resultado.get(0));
    }
 
    // ---- nombrePaso() ----
 
    @Test
    public void nombrePaso_idNulo_devuelveCadenaVaciaSinConsultar() {
        assertEquals("", bean.nombrePaso(null));
        verifyNoInteractions(pasoDAO);
    }
 
    @Test
    public void nombrePaso_pasoEnElCatalogo_devuelveSuNombreSinBuscarPorId() {
        ProcedimientoPaso paso = nuevoPaso("Anestesia local");
        when(pasoDAO.findRange(0, 100)).thenReturn(Arrays.asList(paso));
 
        assertEquals("Anestesia local", bean.nombrePaso(paso.getIdProcedimientoPaso()));
        verify(pasoDAO, never()).buscar(any());
    }
 
    @Test
    public void nombrePaso_pasoFueraDelCatalogo_loBuscaPorId() {
        ProcedimientoPaso lejano = nuevoPaso("Paso lejano");
        when(pasoDAO.findRange(0, 100)).thenReturn(Collections.emptyList());
        when(pasoDAO.buscar(lejano.getIdProcedimientoPaso())).thenReturn(lejano);
 
        assertEquals("Paso lejano", bean.nombrePaso(lejano.getIdProcedimientoPaso()));
    }
 
    @Test
    public void nombrePaso_pasoInexistente_devuelveElUuidEnTexto() {
        UUID id = UUID.randomUUID();
        when(pasoDAO.findRange(0, 100)).thenReturn(Collections.emptyList());
        when(pasoDAO.buscar(id)).thenReturn(null);
 
        assertEquals(id.toString(), bean.nombrePaso(id));
    }
}
 