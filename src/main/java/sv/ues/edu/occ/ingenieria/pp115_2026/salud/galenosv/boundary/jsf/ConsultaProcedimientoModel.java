package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ConsultaProcedimientoModel extends AbstracCrudModel<ConsultaProcedimiento> {

    @Inject
    private ConsultaProcedimientoDAO cpDAO;

    // Estos dos DAO llenan los combos "Consulta" y "Procedimiento".
    @Inject
    private ConsultaDAO consultaDAO;

    @Inject
    private ProcedimientoDAO procedimientoDAO;

    private List<Consulta> consultas;

    private List<Procedimiento> procedimientos;

    @Override
    protected InterfaceDAO<ConsultaProcedimiento> getDAO() {
        return cpDAO;
    }

    // Registro nuevo: UUID generado y fecha de inicio = ahora.
    @Override
    protected ConsultaProcedimiento crearRegistroNuevo() {
        ConsultaProcedimiento cp = new ConsultaProcedimiento(UUID.randomUUID());
        cp.setFechaInicio(new Date());
        return cp;
    }

    @Override
    protected UUID obtenerId(ConsultaProcedimiento registro) {
        return registro.getIdConsultaProcedimiento();
    }

    // Listas para los combos (se cargan solo la primera vez).
    public List<Consulta> getConsultas() {
        if (consultas == null) {
            consultas = consultaDAO.findRange(0, 100);
        }
        return consultas;
    }

    public List<Procedimiento> getProcedimientos() {
        if (procedimientos == null) {
            procedimientos = procedimientoDAO.findRange(0, 100);
        }
        return procedimientos;
    }

    // Busca el nombre del procedimiento a partir de su UUID suelto.
    public String nombreProcedimiento(UUID id) {
        if (id == null) {
            return "";
        }
        return getProcedimientos().stream()
                .filter(p -> id.equals(p.getIdProcedimiento()))
                .map(Procedimiento::getNombre)
                .findFirst()
                .orElse(id.toString());
    }

    // Texto legible de una consulta: "Consulta 3f2a9c1d (21/09/2026 10:30)".
    public String etiquetaConsulta(Consulta c) {
        if (c == null || c.getIdConsulta() == null) {
            return "";
        }
        String fecha = c.getFechaInicio() == null ? ""
                : " (" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(c.getFechaInicio()) + ")";
        return "Consulta " + c.getIdConsulta().toString().substring(0, 8) + fecha;
    }
}
