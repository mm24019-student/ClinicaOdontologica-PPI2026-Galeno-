package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenResultadoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Named
@ViewScoped
public class OrdenExamenModel extends AbstracCrudTabsModel<OrdenExamen> {

    @Inject
    private OrdenExamenDAO oeDAO;

    @Inject
    private ExamenResultadoDAO erDAO;

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
    protected void resetearHijos() {
        // La orden todavía no existe / ya no aplica -> el hijo (Resultados)
        // no debe conservar el registro/estado de la orden anterior.
        examenResultadoModel.cargarDe(null);
    }

    @Override
    protected OrdenExamen crearRegistroNuevoBase() {
        OrdenExamen o = new OrdenExamen(UUID.randomUUID());
        o.setFechaCreacion(new Date());
        return o;
    }

    @Override
    protected boolean tieneRegistrosDependientes(OrdenExamen registro) {
        return !erDAO.findByOrdenExamen(registro.getIdOrdenExamen()).isEmpty();
    }

    @Override
    protected UUID obtenerId(OrdenExamen registro) {
        return registro.getIdOrdenExamen();
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

    @Override
    public void onRowSelect(SelectEvent<OrdenExamen> event) {
        super.onRowSelect(event);
        activeTabIndex = 0;
    }

    public void seleccionarOrdenExamen(SelectEvent<OrdenExamen> event) {
        OrdenExamen ordenSeleccionada = event.getObject();
        examenResultadoModel.cargarDe(ordenSeleccionada);
    }

    @Override
    protected boolean validarAntesDeGuardar() {
        return requerir(registro.getIdConsultaProcedimientoPaso(), "Seleccione una consulta", "El paso de consulta es obligatorio");
    }

}
