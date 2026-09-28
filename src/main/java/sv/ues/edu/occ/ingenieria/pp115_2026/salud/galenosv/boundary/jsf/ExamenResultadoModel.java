package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenResultado;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Named
@ViewScoped
public class ExamenResultadoModel extends AbstracdetallecrudModel<ExamenResultado, OrdenExamen> {

    @Inject
    private ExamenResultadoDAO erDAO;

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
    // Trae de la base solo los resultados de la orden indicada

    @Override
    protected List<ExamenResultado> buscarPorPadre(UUID idPadre) {
        return erDAO.findByOrdenExamen(idPadre);
    }

// UUID del padre (OrdenExamen)
    @Override
    protected UUID obtenerIdPadre(OrdenExamen padre) {
        return padre.getIdOrdenExamen();
    }

// Cómo asignar el padre a un resultado nuevo
    @Override
    protected void asignarPadre(ExamenResultado hijo, OrdenExamen padre) {
        hijo.setIdOrdenExamen(padre);
    }
}
