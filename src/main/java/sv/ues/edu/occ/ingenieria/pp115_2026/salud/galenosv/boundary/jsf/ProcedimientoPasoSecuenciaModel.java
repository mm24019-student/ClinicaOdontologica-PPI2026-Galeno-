package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ProcedimientoPasoSecuenciaModel extends AbstracCrudModel<ProcedimientoPasoSecuencia> {

    @Inject
    private ProcedimientoPasoSecuenciaDAO dao;
    @Inject
    private ProcedimientoPasoDAO pasoDAO;

    private List<ProcedimientoPaso> pasos;

    @Override
    protected InterfaceDAO<ProcedimientoPasoSecuencia> getDAO() {
        return dao;
    }

    @Override
    protected ProcedimientoPasoSecuencia crearRegistroNuevo() {
        return new ProcedimientoPasoSecuencia(UUID.randomUUID());   // esta tabla no tiene "activo"
    }

    @Override
    protected UUID obtenerId(ProcedimientoPasoSecuencia registro) {
        return registro.getIdProcedimientoPasoSecuencia();
    }

    // Lista para los dos combos (se carga solo la primera vez)
    public List<ProcedimientoPaso> getPasos() {
        if (pasos == null) {
            pasos = pasoDAO.findRange(0, 100);
        }
        return pasos;
    }

    // La referencia es un UUID suelto; este método busca el nombre del paso para mostrarlo en la tabla
    public String nombrePaso(UUID id) {
        if (id == null) {
            return "";
        }
        return getPasos().stream()
                .filter(p -> id.equals(p.getIdProcedimientoPaso()))
                .map(ProcedimientoPaso::getNombre)
                .findFirst()
                .orElse(id.toString());
    }
}
