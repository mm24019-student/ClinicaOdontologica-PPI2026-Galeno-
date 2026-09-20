package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenResultado;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Named
@ViewScoped
public class ExamenResultadoModel extends AbstracCrudModel<ExamenResultado> {

    @Inject
    private ExamenResultadoDAO erDAO;

    @Inject
    private OrdenExamenDAO oeDAO;

    private List<OrdenExamen> ordenes;

    @Override
    protected InterfaceDAO<ExamenResultado> getDAO() {
        return erDAO;
    }

    @Override
    protected ExamenResultado crearRegistroNuevo() {
        ExamenResultado r = new ExamenResultado(UUID.randomUUID());
        r.setFechaCreacion(new Date());
        return r;
    }

    @Override
    protected UUID obtenerId(ExamenResultado registro) {
        return registro.getIdExamenResultado();
    }

    // Opciones del selector de orden de examen (se cargan una vez por vista)
    public List<OrdenExamen> getOrdenes() {
        if (ordenes == null) {
            ordenes = oeDAO.findRange(0, 100);
        }
        return ordenes;
    }
}