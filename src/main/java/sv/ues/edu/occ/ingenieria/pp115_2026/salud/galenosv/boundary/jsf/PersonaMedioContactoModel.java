package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 * Bean de detalle para la pestaña "Medios de Contacto" dentro de la
 * pantalla de Persona. Mismo caso que PersonaDocumentoModel: un solo
 * combo (TipoMedioContacto) + un campo propio (valor), flujo estándar
 * Nuevo -> formulario -> Crear/Actualizar/Eliminar.
 *
 * No se llama "MedioContactoModel" porque ese nombre ya lo tiene el CRUD
 * standalone de MedioContacto (su propia pantalla en el menú).
 */
@Named
@ViewScoped
public class PersonaMedioContactoModel extends AbstracdetallecrudModel<MedioContacto, Persona> {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedioContactoDAO medioContactoDAO;
    @Inject
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    private List<TipoMedioContacto> tiposMedioContacto;

    @Override
    protected InterfaceDAO<MedioContacto> getDAO() {
        return medioContactoDAO;
    }

    @Override
    protected MedioContacto crearRegistroNuevo() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setFechaCreacion(new Date());
        return mc;
    }

    @Override
    protected UUID obtenerId(MedioContacto registro) {
        return registro.getIdMedioContacto();
    }

    // ---- Contrato con AbstracdetallecrudModel: Persona es el padre ----
    @Override
    protected List<MedioContacto> buscarPorPadre(UUID idPadre) {
        return medioContactoDAO.findByPersona(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(Persona padre) {
        return padre.getIdPersona();
    }

    @Override
    protected void asignarPadre(MedioContacto hijo, Persona padre) {
        hijo.setIdPersona(padre);
    }

    // Misma validación que ya tenía el MedioContactoModel standalone (menos
    // la de Persona, porque acá el padre ya viene fijo).
    @Override
    public void btnCrearhandler(ActionEvent ae) {
        if (relacionesCompletas()) {
            super.btnCrearhandler(ae);
        }
    }

    @Override
    public void btnModificarHandler() {
        if (relacionesCompletas()) {
            super.btnModificarHandler();
        }
    }

    private boolean relacionesCompletas() {
        if (registro == null) {
            return true;
        }
        if (registro.getIdTipoMedioContacto() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione un tipo de medio de contacto", "El tipo es obligatorio"));
            return false;
        }
        return true;
    }

    public List<TipoMedioContacto> getTiposMedioContacto() {
        if (tiposMedioContacto == null) {
            tiposMedioContacto = tipoMedioContactoDAO.findRange(0, 100);
        }
        return tiposMedioContacto;
    }

    // completeMethod del autocomplete "Tipo de Medio de Contacto" (mismo
    // patrón que completarRoles/completarClinicas en PersonaRolDetalleModel).
    public List<TipoMedioContacto> completarTiposMedioContacto(String query) {
        String texto = query == null ? "" : query.trim().toLowerCase();
        return getTiposMedioContacto().stream()
                .filter(t -> t.getNombre() != null && t.getNombre().toLowerCase().contains(texto))
                .collect(Collectors.toList());
    }
}