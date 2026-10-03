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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonaMedioContactoModelTest {

    @Mock
    private MedioContactoDAO medioContactoDAO;
    @Mock
    private TipoMedioContactoDAO tipoMedioContactoDAO;
    @Mock
    private FacesContext fc;

    @InjectMocks
    private PersonaMedioContactoModel bean;

    private void assertMensaje(FacesMessage.Severity esperada) {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(fc).addMessage(isNull(), captor.capture());
        assertEquals(esperada, captor.getValue().getSeverity());
    }

    private TipoMedioContacto tipo(String nombre, String regex, Boolean activo) {
        TipoMedioContacto t = new TipoMedioContacto(UUID.randomUUID());
        t.setNombre(nombre);
        t.setExpresionRegular(regex);
        t.setActivo(activo);
        return t;
    }

    private MedioContacto medio(TipoMedioContacto tipo, String valor) {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setIdTipoMedioContacto(tipo);
        mc.setValor(valor);
        return mc;
    }

    // ---- cargarDe() / buscarPorPadre() ----

    @Test
    public void cargarDe_conPersona_delegaEnFindByPersona() {
        Persona persona = new Persona(UUID.randomUUID());
        List<MedioContacto> esperado = Arrays.asList(new MedioContacto(UUID.randomUUID()));
        when(medioContactoDAO.findByPersona(persona.getIdPersona())).thenReturn(esperado);

        bean.cargarDe(persona);

        assertEquals(esperado, bean.getregistros());
    }

    @Test
    public void cargarDe_conPadreNulo_dejaLaListaVacia() {
        bean.cargarDe(null);

        assertTrue(bean.getregistros().isEmpty());
        verifyNoInteractions(medioContactoDAO);
    }

    // ---- btnNuevoHandler() ----

    @Test
    public void btnNuevoHandler_asignaPersonaYFechaDeCreacion() {
        Persona persona = new Persona(UUID.randomUUID());
        bean.cargarDe(persona);

        bean.btnNuevoHandler(null);

        assertSame(persona, bean.getRegistro().getIdPersona());
        assertNotNull(bean.getRegistro().getIdMedioContacto());
        assertNotNull(bean.getRegistro().getFechaCreacion());
        assertEquals(Estado_Crud.CREAR, bean.getEstado());
    }

    // ---- validarAntesDeGuardar() ----

    @Test
    public void btnCrearhandler_sinTipo_agregaErrorYNoCrea() {
        bean.setRegistro(new MedioContacto(UUID.randomUUID()));

        bean.btnCrearhandler(null);

        verify(medioContactoDAO, never()).crear(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnCrearhandler_tipoInactivo_agregaErrorYNoCrea() {
        bean.setRegistro(medio(tipo("Teléfono", null, Boolean.FALSE), "22345678"));

        bean.btnCrearhandler(null);

        verify(medioContactoDAO, never()).crear(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnCrearhandler_valorNoCumpleElFormato_agregaErrorYNoCrea() {
        TipoMedioContacto t = tipo("Teléfono", "^\\d{8}$", Boolean.TRUE);
        when(tipoMedioContactoDAO.buscar(t.getIdTipoMedioContacto())).thenReturn(t);
        bean.setRegistro(medio(t, "abc"));

        bean.btnCrearhandler(null);

        verify(medioContactoDAO, never()).crear(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnCrearhandler_valorCumpleElFormatoYSinPadre_creaElRegistro() {
        TipoMedioContacto t = tipo("Teléfono", "^\\d{8}$", Boolean.TRUE);
        when(tipoMedioContactoDAO.buscar(t.getIdTipoMedioContacto())).thenReturn(t);
        MedioContacto mc = medio(t, "22345678");
        bean.setRegistro(mc);

        bean.btnCrearhandler(null);

        verify(medioContactoDAO).crear(mc);
        assertMensaje(FacesMessage.SEVERITY_INFO);
    }

    @Test
    public void btnCrearhandler_medioDuplicadoParaLaPersona_agregaErrorYNoCrea() {
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto t = tipo("Correo", null, Boolean.TRUE);
        MedioContacto mc = medio(t, "a@b.com");
        bean.padreActual = persona;
        bean.setRegistro(mc);
        when(medioContactoDAO.existeMedioParaPersona(persona.getIdPersona(),
                t.getIdTipoMedioContacto(), "a@b.com", mc.getIdMedioContacto())).thenReturn(true);

        bean.btnCrearhandler(null);

        verify(medioContactoDAO, never()).crear(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    public void btnCrearhandler_exitoConPadre_creaYRecargaFiltradoPorPersona() {
        Persona persona = new Persona(UUID.randomUUID());
        TipoMedioContacto t = tipo("Correo", null, Boolean.TRUE);
        MedioContacto mc = medio(t, "a@b.com");
        bean.padreActual = persona;
        bean.setRegistro(mc);
        List<MedioContacto> recargados = Arrays.asList(mc);
        when(medioContactoDAO.findByPersona(persona.getIdPersona())).thenReturn(recargados);

        bean.btnCrearhandler(null);

        verify(medioContactoDAO).crear(mc);
        assertEquals(Estado_Crud.NINGUNO, bean.getEstado());
        assertNull(bean.getRegistro());
        assertEquals(recargados, bean.getregistros());
    }

    @Test
    public void btnModificarHandler_sinTipo_agregaErrorYNoActualiza() {
        bean.setRegistro(new MedioContacto(UUID.randomUUID()));

        bean.btnModificarHandler();

        verify(medioContactoDAO, never()).actualizar(any());
        assertMensaje(FacesMessage.SEVERITY_ERROR);
    }

    // ---- getTiposMedioContacto() / completarTiposMedioContacto() ----

    @Test
    public void getTiposMedioContacto_cacheaEntreLlamadas() {
        when(tipoMedioContactoDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(new TipoMedioContacto(UUID.randomUUID())));

        bean.getTiposMedioContacto();
        bean.getTiposMedioContacto();

        verify(tipoMedioContactoDAO, times(1)).findRange(0, 100);
    }

    @Test
    public void completarTiposMedioContacto_filtraPorNombreYExcluyeInactivos() {
        TipoMedioContacto telefono = tipo("Teléfono", null, Boolean.TRUE);
        TipoMedioContacto telefonoViejo = tipo("Teléfono fijo", null, Boolean.FALSE);
        TipoMedioContacto correo = tipo("Correo", null, Boolean.TRUE);
        when(tipoMedioContactoDAO.findRange(0, 100))
                .thenReturn(Arrays.asList(telefono, telefonoViejo, correo));

        List<TipoMedioContacto> resultado = bean.completarTiposMedioContacto("TEL");

        assertEquals(1, resultado.size());
        assertSame(telefono, resultado.get(0));
    }
}