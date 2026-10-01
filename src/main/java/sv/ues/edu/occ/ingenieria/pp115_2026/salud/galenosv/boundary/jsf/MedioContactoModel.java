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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named
@ViewScoped
public class MedioContactoModel extends AbstracCrudModel<MedioContacto> {

    @Inject
    private MedioContactoDAO medioContactoDAO;

    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    private List<Persona> personas;
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

    public List<Persona> getPersonas() {
        if (personas == null) {
            personas = personaDAO.findRange(0, 100);
        }
        return personas;
    }

    public List<TipoMedioContacto> getTiposMedioContacto() {
        if (tiposMedioContacto == null) {
            tiposMedioContacto = tipoMedioContactoDAO.findRange(0, 100);
        }
        return tiposMedioContacto;
    }
    
        @Override
    protected boolean validarAntesDeGuardar() {
        return requerir(registro.getIdPersona(), "Seleccione una persona", "La persona es obligatoria")
                && requerir(registro.getIdTipoMedioContacto(), "Seleccione un tipo de medio de contacto", "El tipo es obligatorio")
                && validarFormatoDelTipo()
                && validarSinDuplicados();
    }

    // Valida el valor contra la expresion regular del TipoMedioContacto, leida de
    // la base de datos en este momento (no de la lista cacheada en el bean).
    private boolean validarFormatoDelTipo() {
        TipoMedioContacto tipo = tipoMedioContactoDAO.buscar(registro.getIdTipoMedioContacto().getIdTipoMedioContacto());
        if (tipo == null) {
            tipo = registro.getIdTipoMedioContacto();
        }
        return validarFormatoConRegex(registro.getValor(), tipo.getExpresionRegular(),
                tipo.getNombre(), tipo.getIndicaciones());
    }

    // Evita datos repetidos: la persona no puede tener dos veces el mismo medio
    // de contacto (mismo tipo y mismo valor).
    private boolean validarSinDuplicados() {
        if (medioContactoDAO.existeMedioParaPersona(
                registro.getIdPersona().getIdPersona(),
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