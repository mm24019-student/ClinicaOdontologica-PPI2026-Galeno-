package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Named
@ViewScoped
public class OrdenExamenModel extends AbstracCrudModel<OrdenExamen> {

    @Inject
    private OrdenExamenDAO oeDAO;

    @Inject
    private ConsultaProcedimientoPasoDAO cppDAO;

    private List<ConsultaProcedimientoPaso> pasosConsulta;

    @Override
    protected InterfaceDAO<OrdenExamen> getDAO() {
        return oeDAO;
    }

    @Override
    protected OrdenExamen crearRegistroNuevo() {
        OrdenExamen o = new OrdenExamen(UUID.randomUUID());
        o.setFechaCreacion(new Date());
        return o;
    }

    @Override
    protected UUID obtenerId(OrdenExamen registro) {
        return registro.getIdOrdenExamen();
    }

    // Valida la consulta antes de guardar (relacion obligatoria en BD)
    @Override
    public void btnCrearhandler(ActionEvent ae) {
        if (consultaSeleccionada()) {
            super.btnCrearhandler(ae);
        }
    }

    @Override
    public void btnModificarHandler() {
        if (consultaSeleccionada()) {
            super.btnModificarHandler();
        }
    }

    private boolean consultaSeleccionada() {
        if (registro != null && registro.getIdConsultaProcedimientoPaso() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione una consulta", "El paso de consulta es obligatorio"));
            return false;
        }
        return true;
    }

    // Opciones del selector de consulta-procedimiento-paso
    public List<ConsultaProcedimientoPaso> getPasosConsulta() {
        if (pasosConsulta == null) {
            pasosConsulta = cppDAO.findRange(0, 100);
        }
        return pasosConsulta;
    }
}