package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
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
public class ConsultaProcedimientoModel extends AbstracdetallecrudModel<ConsultaProcedimiento, Consulta> {

    @Inject
    private ConsultaProcedimientoDAO cpDAO;

    // Solo hace falta el DAO de Procedimiento: sigue siendo un combo normal
    // porque idProcedimiento en la entidad es un UUID suelto, no una
    // relación @ManyToOne (a diferencia de idConsulta, que sí lo es).
    @Inject
    private ProcedimientoDAO procedimientoDAO;

    private List<Procedimiento> procedimientos;

    private Procedimiento procedimientoSeleccionado;

    public Procedimiento getProcedimientoSeleccionado() {
        return procedimientoSeleccionado;
    }

    public void setProcedimientoSeleccionado(Procedimiento procedimientoSeleccionado) {
        this.procedimientoSeleccionado = procedimientoSeleccionado;

        if (this.registro != null) {
            this.registro.setIdProcedimiento(
                    procedimientoSeleccionado == null
                            ? null
                            : procedimientoSeleccionado.getIdProcedimiento()
            );
        }
    }

       public List<Procedimiento> completarProcedimientos(String query) {
        return filtrar(getProcedimientos(), query, Procedimiento::getNombre);
    }

    @Override
    public void onRowSelect(SelectEvent<ConsultaProcedimiento> event) {
        super.onRowSelect(event);

        UUID idProcedimiento = event.getObject().getIdProcedimiento();

        if (idProcedimiento != null) {
            this.procedimientoSeleccionado
                    = procedimientoDAO.buscar(idProcedimiento);
        } else {
            this.procedimientoSeleccionado = null;
        }
    }

    @Override
    protected InterfaceDAO<ConsultaProcedimiento> getDAO() {
        return cpDAO;
    }

    // ---- Los 3 métodos que pide AbstracdetallecrudModel ----
    @Override
    protected List<ConsultaProcedimiento> buscarPorPadre(UUID idPadre) {
        return cpDAO.findByConsulta(idPadre);
    }



    @Override
    protected UUID obtenerIdPadre(Consulta padre) {
        return padre.getIdConsulta();
    }

    @Override
    protected void asignarPadre(ConsultaProcedimiento hijo, Consulta padre) {
        hijo.setIdConsulta(padre);
    }

    
    // Registro nuevo: UUID generado y fecha de inicio = ahora.
    // configurarNuevoRegistro() (heredado) ya se encarga de asignar el
    // padre (la consulta activa) automáticamente después de esto.
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

    @Override
    protected void configurarNuevoRegistro(ConsultaProcedimiento nuevoRegistro) {
        super.configurarNuevoRegistro(nuevoRegistro);
        this.procedimientoSeleccionado = null;
    }

    // Combo para elegir qué Procedimiento se está aplicando.
    public List<Procedimiento> getProcedimientos() {
        if (procedimientos == null) {
            procedimientos = procedimientoDAO.findRange(0, 100);
        }
        return procedimientos;
    }

    // Nombre del procedimiento a partir de su UUID suelto (para la tabla).
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
}
