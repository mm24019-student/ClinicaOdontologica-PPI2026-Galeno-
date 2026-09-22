package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ConsultaProcedimientoPasoModel extends AbstracCrudModel<ConsultaProcedimientoPaso> {

    // Valores permitidos para "estado". En el Paso 9 la regex debe aceptar
    // exactamente estos mismos; si cambias uno aquí, cámbialo allá también.
    private static final List<String> ESTADOS = List.of("PENDIENTE", "EN_PROCESO", "COMPLETADO", "CANCELADO", "FINALIZADO");
    @Inject
    private ConsultaProcedimientoPasoDAO pasoDAO;

    // Para llenar los combos.
    @Inject
    private ConsultaProcedimientoDAO cpDAO;

    @Inject
    private PersonaRolDAO personaRolDAO;

    // Para mostrar el nombre del procedimiento en lugar de su UUID.
    @Inject
    private ProcedimientoDAO procedimientoDAO;

    private List<ConsultaProcedimiento> consultasProcedimiento;

    private List<PersonaRol> personasRol;

    private List<Procedimiento> procedimientos;

    @Override
    protected InterfaceDAO<ConsultaProcedimientoPaso> getDAO() {
        return pasoDAO;
    }

    // Registro nuevo: UUID generado y estado inicial PENDIENTE.
    @Override
    protected ConsultaProcedimientoPaso crearRegistroNuevo() {
        ConsultaProcedimientoPaso p = new ConsultaProcedimientoPaso(UUID.randomUUID());
        p.setEstado("PENDIENTE");
        return p;
    }

    @Override
    protected UUID obtenerId(ConsultaProcedimientoPaso registro) {
        return registro.getIdConsultaProcedimientoPaso();
    }

    // Opciones del combo "Estado".
    public List<String> getEstados() {
        return ESTADOS;
    }

    // Listas para los combos (se cargan solo la primera vez).
    public List<ConsultaProcedimiento> getConsultasProcedimiento() {
        if (consultasProcedimiento == null) {
            consultasProcedimiento = cpDAO.findRange(0, 100);
        }
        return consultasProcedimiento;
    }

    public List<PersonaRol> getPersonasRol() {
        if (personasRol == null) {
            personasRol = personaRolDAO.findRange(0, 100);
        }
        return personasRol;
    }

    private List<Procedimiento> getProcedimientos() {
        if (procedimientos == null) {
            procedimientos = procedimientoDAO.findRange(0, 100);
        }
        return procedimientos;
    }

    // "Consulta 3f2a9c1d (21/09/2026 10:30) - Limpieza dental"
    public String etiquetaConsultaProcedimiento(ConsultaProcedimiento cp) {
        if (cp == null) {
            return "";
        }
        String consulta = etiquetaConsulta(cp.getIdConsulta());
        String procedimiento = nombreProcedimiento(cp.getIdProcedimiento());
        return procedimiento.isEmpty() ? consulta : consulta + " - " + procedimiento;
    }

    // "Consulta 3f2a9c1d (21/09/2026 10:30)"
    private String etiquetaConsulta(Consulta c) {
        if (c == null || c.getIdConsulta() == null) {
            return "";
        }
        String fecha = c.getFechaInicio() == null ? ""
                : " (" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(c.getFechaInicio()) + ")";
        return "Consulta " + c.getIdConsulta().toString().substring(0, 8) + fecha;
    }

    // "Ana Pérez - Odontólogo"
    public String etiquetaPersonaRol(PersonaRol pr) {
        if (pr == null || pr.getIdPersona() == null) {
            return "";
        }
        String nombres = Objects.toString(pr.getIdPersona().getNombres(), "");
        String apellidos = Objects.toString(pr.getIdPersona().getApellidos(), "");
        String rol = pr.getIdRol() == null ? "" : Objects.toString(pr.getIdRol().getNombre(), "");
        return (nombres + " " + apellidos).trim() + (rol.isEmpty() ? "" : " - " + rol);
    }

    // Busca el nombre del procedimiento a partir de su UUID suelto.
    private String nombreProcedimiento(UUID id) {
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
