package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;

/**
 * Managed Bean para el CRUD de Clinica. Expuesto en las vistas como
 * "clinicaModel".
 *
 * @author oscar
 */
@Named
@ViewScoped
public class ClinicaModel extends AbstracCrudModel<Clinica> {

    @Inject
    private ClinicaDAO clinicaDAO;

    @Override
    protected InterfaceDAO<Clinica> getDAO() {
        return clinicaDAO;
    }

    // Genera el UUID a mano y deja el registro activo por defecto.
    @Override
    protected Clinica crearRegistroNuevo() {
        Clinica c = new Clinica(UUID.randomUUID());
        c.setActivo(Boolean.TRUE);
        return c;
    }

    @Override
    protected UUID obtenerId(Clinica registro) {
        return registro.getIdClinica();
    }
}