package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
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

    @Inject
    private ExamenResultadoModel examenResultadoModel;

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

    public List<ConsultaProcedimientoPaso> getPasosConsulta() {
        if (pasosConsulta == null) {
            pasosConsulta = cppDAO.findRange(0, 100);
        }
        return pasosConsulta;
    }

    // NUEVO: fuente de sugerencias para el p:autoComplete de Consulta (paso)
    public List<ConsultaProcedimientoPaso> completarPasosConsulta(String query) {

        List<ConsultaProcedimientoPaso> resultado = new ArrayList<>();

        String texto = query == null
                ? ""
                : query.trim().toLowerCase();

        for (ConsultaProcedimientoPaso paso : getPasosConsulta()) {

            String estado = paso.getEstado() == null
                    ? ""
                    : paso.getEstado().toLowerCase();

            String id = paso.getIdConsultaProcedimientoPaso() == null
                    ? ""
                    : paso.getIdConsultaProcedimientoPaso().toString().toLowerCase();

            if (texto.isEmpty() || estado.contains(texto) || id.contains(texto)) {
                resultado.add(paso);
            }

            if (resultado.size() >= 20) {
                break;
            }
        }

        return resultado;
    }

    
    public void seleccionarOrdenExamen(SelectEvent<OrdenExamen> event) {
        OrdenExamen ordenSeleccionada = event.getObject();
        examenResultadoModel.cargarPorOrdenExamen(ordenSeleccionada);
    }
}