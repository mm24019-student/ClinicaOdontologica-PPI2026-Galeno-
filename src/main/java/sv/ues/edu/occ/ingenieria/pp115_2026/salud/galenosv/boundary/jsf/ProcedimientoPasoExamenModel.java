package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

@Named
@ViewScoped
public class ProcedimientoPasoExamenModel extends AbstracdetallecrudModel<ProcedimientoPasoExamen, ProcedimientoPaso> {

    @Inject
    private ProcedimientoPasoExamenDAO ppeDAO;

    @Inject
    private ExamenDAO eDAO;

    private List<Examen> examenes;

    @Override
    protected InterfaceDAO<ProcedimientoPasoExamen> getDAO() {
        return ppeDAO;
    }

    @Override
    protected ProcedimientoPasoExamen crearRegistroNuevo() {
        ProcedimientoPasoExamen r = new ProcedimientoPasoExamen(UUID.randomUUID());
        r.setFechaCreacion(new Date());
        r.setActivo(Boolean.TRUE);
        return r;
    }

    @Override
    protected UUID obtenerId(ProcedimientoPasoExamen registro) {
        return registro.getIdProcedimientoPasoExamen();
    }

    // ---- Los 3 métodos que pide AbstracdetallecrudModel ----
    @Override
    protected List<ProcedimientoPasoExamen> buscarPorPadre(UUID idPadre) {
        return ppeDAO.findByProcedimientoPaso(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(ProcedimientoPaso padre) {
        return padre.getIdProcedimientoPaso();
    }

    @Override
    protected void asignarPadre(ProcedimientoPasoExamen hijo, ProcedimientoPaso padre) {
        hijo.setIdProcedimientoPaso(padre);
    }

    // Valida el examen antes de guardar
    @Override
    public void btnCrearhandler(ActionEvent ae) {
        if (examenSeleccionado()) {
            super.btnCrearhandler(ae);
        }
    }

    @Override
    public void btnModificarHandler() {
        if (examenSeleccionado()) {
            super.btnModificarHandler();
        }
    }

    private boolean examenSeleccionado() {
        if (registro != null && registro.getIdExamen() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione un examen", "El examen es obligatorio"));
            return false;
        }
        return true;
    }

    // Opciones del selector de examen (se cargan una vez por vista)
    public List<Examen> getExamenes() {
        if (examenes == null) {
            examenes = eDAO.findRange(0, 100);
        }
        return examenes;
    }

    // PENDIENTE: cuando exista ProcedimientoPasoDAO (persona 2), agregar aqui
    // la lista de procedimientos paso para el segundo selector.
}
