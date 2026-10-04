package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;

@Named
@ViewScoped
public class ClinicaModel extends AbstracCrudModel<Clinica> {

    @Inject
    private ClinicaDAO clinicaDAO;
    
    @Inject
    private SesionBean sesionBean;

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
    
    // Al guardar o eliminar un clínica, se refresca el selector de sesión de arriba
    // para que sus roles/clínicas activos aparezcan o desaparezcan al instante.
    @Override
    protected void recargarLista() {
        super.recargarLista();
        sesionBean.refrescar();
    }
}