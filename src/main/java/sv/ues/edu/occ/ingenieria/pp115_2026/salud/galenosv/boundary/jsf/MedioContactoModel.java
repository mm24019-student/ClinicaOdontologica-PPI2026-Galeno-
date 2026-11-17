package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.MedioContactoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named
@ViewScoped
public class MedioContactoModel extends AbstracCrudModel<MedioContacto> {

    @Inject
    private MedioContactoService medioContactoService;

    @Inject
    private TipoMedioContactoService tipoMedioContactoService;

    private List<TipoMedioContacto> tiposMedioContacto;

    @Override
    protected InterfaceService<MedioContacto> getService() {
        return medioContactoService;
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


    public List<TipoMedioContacto> getTiposMedioContacto() {
        if (tiposMedioContacto == null) {
            tiposMedioContacto = tipoMedioContactoService.findRange(0, 100);
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
        TipoMedioContacto tipo = tipoMedioContactoService.buscar(registro.getIdTipoMedioContacto().getIdTipoMedioContacto());
        if (tipo == null) {
            tipo = registro.getIdTipoMedioContacto();
        }
        return validarFormatoConRegex(registro.getValor(), tipo.getExpresionRegular(),
                tipo.getNombre(), tipo.getIndicaciones());
    }

    // Evita datos repetidos: la persona no puede tener dos veces el mismo medio
    // de contacto (mismo tipo y mismo valor).
    private boolean validarSinDuplicados() {
        if (medioContactoService.existeMedioParaPersona(
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