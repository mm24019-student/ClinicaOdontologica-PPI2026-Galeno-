package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;

@Named
@ViewScoped
public class ClinicaModel extends AbstracCrudModel<Clinica> {

    @Inject
    private ClinicaDAO clinicaDAO;

    @Override
    protected InterfaceDAO<Clinica> getDAO() {
        return clinicaDAO;
    }

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

    public List<Clinica> getRegistros() {
        return getregistros();
    }
}