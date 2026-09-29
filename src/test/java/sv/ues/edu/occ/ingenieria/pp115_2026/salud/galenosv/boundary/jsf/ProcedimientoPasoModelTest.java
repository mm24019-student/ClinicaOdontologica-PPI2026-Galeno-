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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author antonio
**
 * Prueba de ProcedimientoPasoModel (detalle de Procedimiento). Como es el
 * "modelo tipo" de AbstracdetallecrudModel, aquí también se prueba el
 * comportamiento heredado del detalle: arranque vacío, cargarDe(padre),
 * padre asignado a cada hijo nuevo y recarga filtrada por padre tras guardar.
 */
@ExtendWith(MockitoExtension.class)
public class ProcedimientoPasoModelTest {
 
    @Mock
    private ProcedimientoPasoDAO pdDAO;
    @Mock
    private RolDAO rolDAO;
    @Mock
    private FacesContext fc;
 
    @InjectMocks
    private ProcedimientoPasoModel bean;
 
    private Procedimiento procedimiento;
 
    @BeforeEach
    public void setUp() {
        procedimiento = new Procedimiento(UUID.randomUUID());
    }
 
    private Rol nuevoRol(String nombre) {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombre);
        return r;
    }
 
    // ---- getDAO() / obtenerId() / los 3 métodos del detalle ----
 
    @Test
    public void getDAO_devuelveElDaoInyectado() {
        assertSame(pdDAO, bean.getDAO());
    }
 
    @Test
    public void obtenerId_devuelveElIdDelPaso() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, bean.obtenerId(new ProcedimientoPaso(id)));
    }
 
    @Test
    public void obtenerIdPadre_devuelveElIdDelProcedimiento() {
        assertEquals(procedimiento.getIdProcedimiento(), bean.obtenerIdPadre(procedimiento));
    }
 
    @Test
    public void asignarPadre_ligaElPasoAlProcedimiento() {
        ProcedimientoPaso paso = new ProcedimientoPaso(UUID.randomUUID());
 
        bean.asignarPadre(paso, procedimiento);
 
        assertSame(procedimiento, paso.getIdProcedimiento());
    }
 
    @Test
    public void buscarPorPadre_delegaEnFindByProcedimiento() {
        List<ProcedimientoPaso> esperado = Arrays.asList(new ProcedimientoPaso(UUID.randomUUID()));
        when(pdDAO.findByProcedimiento(procedimiento.getIdProcedimiento())).thenReturn(esperado);
 
        assertEquals(esperado, bean.buscarPorPadre(procedimiento.getIdProcedimiento()));
    }
 
    // ---- inicializar() ----
 
    @Test
    public void inicializar_arrancaVacioSinConsultarLaBaseDeDatos() {
        bean.inicializar();
 
        assertTrue(bean.getregistros().isEmpty());
        verifyNoInteractions(pdDAO);
    }
 
    // ---- cargarDe() ----
 
    @Test
    public void cargarDe_procedimientoConId_cargaSoloSusPasosYReseteaElEstado() {
        List<ProcedimientoPaso> esperado = Arrays.asList(new ProcedimientoPaso(UUID.randomUUID()));
        when(pdDAO.findByProcedimiento(procedimiento.getIdProcedimiento())).thenReturn(esperado);
        bean.setRegistro(new ProcedimientoPaso(UUID.randomUUID()));
        bean.setEstado(Estado_Crud.MODIFICAR);
 
        bean.cargarDe(procedimiento);
 
        assertEquals(esperado, bean.getregistros());
        assertNull(bean.getRegistro());
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
    }
 
    @Test
    public void cargarDe_procedimientoNulo_dejaListaVaciaSinConsultar() {
        bean.cargarDe(null);
 
        assertTrue(bean.getregistros().isEmpty());
        verify(pdDAO, never()).findByProcedimiento(any());
    }
 
    @Test
    public void cargarDe_procedimientoSinId_dejaListaVaciaSinConsultar() {
        bean.cargarDe(new Procedimiento());
 
        assertTrue(bean.getregistros().isEmpty());
        verify(pdDAO, never()).findByProcedimiento(any());
    }
 
    // ---- btnNuevoHandler() / crearRegistroNuevo() ----
 
    @Test
    public void btnNuevoHandler_creaPasoConIndicaFinYLigadoAlProcedimientoActual() {
        when(pdDAO.findByProcedimiento(procedimiento.getIdProcedimiento()))
                .thenReturn(Collections.emptyList());
        bean.cargarDe(procedimiento);
 
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertNotNull(bean.getRegistro().getIdProcedimientoPaso());
        assertEquals(Boolean.TRUE, bean.getRegistro().getIndicaFin());
        assertSame(procedimiento, bean.getRegistro().getIdProcedimiento());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }
 
    @Test
    public void btnNuevoHandler_sinProcedimientoPadre_dejaElPasoSinProcedimiento() {
        bean.btnNuevoHandler(mock(ActionEvent.class));
 
        assertNull(bean.getRegistro().getIdProcedimiento());
    }
 
    // ---- btnCrearhandler() + recargarLista() ----
 
    @Test
    public void btnCrearhandler_exito_creaYRecargaSoloLosPasosDelProcedimiento() {
        UUID idPadre = procedimiento.getIdProcedimiento();
        when(pdDAO.findByProcedimiento(idPadre)).thenReturn(Collections.emptyList());
        bean.cargarDe(procedimiento);
        bean.btnNuevoHandler(mock(ActionEvent.class));
        ProcedimientoPaso nuevo = bean.getRegistro();
 
        bean.btnCrearhandler(mock(ActionEvent.class));
 
        verify(pdDAO).crear(nuevo);
        // Una consulta al cargar el padre y otra al recargar tras guardar.
        verify(pdDAO, times(2)).findByProcedimiento(idPadre);
        verify(pdDAO, never()).findRange(anyInt(), anyInt());
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getValue().getSeverity());
    }
 
    // ---- completarRoles() ----
 
    @Test
    public void completarRoles_filtraPorNombreSinImportarMayusculas() {
        Rol odontologo = nuevoRol("Odontologo");
        Rol recepcion = nuevoRol("Recepcionista");
        when(rolDAO.findRange(0, 100)).thenReturn(Arrays.asList(odontologo, recepcion));
 
        List<Rol> resultado = bean.completarRoles("ODONTO");
 
        assertEquals(1, resultado.size());
        assertSame(odontologo, resultado.get(0));
    }
 
    @Test
    public void completarRoles_consultaVacia_devuelveTodosLosRoles() {
        when(rolDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(nuevoRol("Odontologo"), nuevoRol("Recepcionista")));
 
        assertEquals(2, bean.completarRoles("").size());
    }
 
    @Test
    public void completarRoles_consultaNula_devuelveTodosLosRoles() {
        when(rolDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(nuevoRol("Odontologo"), nuevoRol("Recepcionista")));
 
        assertEquals(2, bean.completarRoles(null).size());
    }
 
    @Test
    public void completarRoles_cacheaElCatalogoEntreLlamadas() {
        when(rolDAO.findRange(0, 100)).thenReturn(Arrays.asList(nuevoRol("Odontologo")));
 
        bean.completarRoles("");
        bean.completarRoles("odo");
 
        verify(rolDAO, times(1)).findRange(0, 100);
    }
}