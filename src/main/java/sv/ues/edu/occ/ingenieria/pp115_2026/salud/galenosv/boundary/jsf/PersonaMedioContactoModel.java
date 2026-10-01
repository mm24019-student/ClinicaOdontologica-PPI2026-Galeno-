package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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

    public List<TipoMedioContacto> getTiposMedioContacto() {
        if (tiposMedioContacto == null) {
            tiposMedioContacto = tipoMedioContactoDAO.findRange(0, 100);
        }
        return tiposMedioContacto;
    }

    public List<TipoMedioContacto> completarTiposMedioContacto(String query) {
        return filtrarActivos(getTiposMedioContacto(), query, TipoMedioContacto::getNombre,
                TipoMedioContacto::getActivo);
    }

    @Override
    protected boolean validarAntesDeGuardar() {
        // La persona no se valida: en este detalle el padre ya viene fijo.
        if (!requerir(registro.getIdTipoMedioContacto(),
                "Seleccione un tipo de medio de contacto",
                "El tipo es obligatorio")) {
            return false;
        }

        if (!requerirActivo(registro.getIdTipoMedioContacto().getActivo(),
                "Tipo inactivo",
                "No se puede usar un tipo de medio de contacto inactivo")) {
            return false;
        }

        return validarFormatoDelTipo() && validarSinDuplicados();
    }

    // Valida el valor contra la expresion regular del TipoMedioContacto, leida de
    // la base de datos en este momento (no de la lista cacheada en el bean).
    private boolean validarFormatoDelTipo() {
        TipoMedioContacto tipo = tipoMedioContactoDAO
                .buscar(registro.getIdTipoMedioContacto().getIdTipoMedioContacto());
        if (tipo == null) {
            tipo = registro.getIdTipoMedioContacto();
        }
        return validarFormatoConRegex(registro.getValor(), tipo.getExpresionRegular(),
                tipo.getNombre(), tipo.getIndicaciones());
    }

    // Evita datos repetidos: la persona no puede tener dos veces el mismo medio
    // de contacto (mismo tipo y mismo valor).
    private boolean validarSinDuplicados() {
        UUID idPersona = padreActual != null ? obtenerIdPadre(padreActual) : null;
        if (idPersona != null && medioContactoDAO.existeMedioParaPersona(
                idPersona,
                registro.getIdTipoMedioContacto().getIdTipoMedioContacto(),
                registro.getValor(),
                registro.getIdMedioContacto())) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Medio de contacto duplicado",
                    "Esta persona ya tiene registrado " + registro.getIdTipoMedioContacto().getNombre()
                            + " con el valor " + registro.getValor());
            return false;
        }
        return true;
    }
}