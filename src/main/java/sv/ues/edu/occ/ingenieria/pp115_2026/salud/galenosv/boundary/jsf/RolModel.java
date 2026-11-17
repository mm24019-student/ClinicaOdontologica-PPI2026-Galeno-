package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Managed Bean para el CRUD de Rol. Expuesto en las vistas como "rolModel".
 *
 * @author oscar
 */
@Named
@ViewScoped
public class RolModel extends AbstracCrudModel<Rol> {

    @Inject
    private RolService rolService;
    
    @Inject
    private SesionBean sesionBean;

    @Override
    protected InterfaceService<Rol> getService() {
        return rolService;
    }

    // Genera el UUID a mano y deja el registro activo por defecto.
    @Override
    protected Rol crearRegistroNuevo() {
        Rol r = new Rol(UUID.randomUUID());
        r.setActivo(Boolean.TRUE);
        return r;
    }

    @Override
    protected UUID obtenerId(Rol registro) {
        return registro.getIdRol();
    }
    
    // Al guardar o eliminar un rol, se refresca el selector de sesión de arriba
    // para que sus roles/clínicas activos aparezcan o desaparezcan al instante.
    @Override
    protected void recargarLista() {
        super.recargarLista();
        sesionBean.refrescar();
    }
    
}