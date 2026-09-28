package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import org.primefaces.event.SelectEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

@Named
@ViewScoped
public class ExamenModel extends AbstracCrudTabsModel<Examen> {

    @Inject
    private ExamenDAO eDAO;

    @Inject
    private ExamenTipoExamenModel examenTipoExamenModel;

    @Override
    protected InterfaceDAO<Examen> getDAO() {
        return eDAO;
    }

     @Override
    protected void resetearHijos() {
        // La orden todavía no existe / ya no aplica -> el hijo (Resultados)
        // no debe conservar el registro/estado de la orden anterior.
        examenTipoExamenModel.cargarPorExamen(null);
    }
    
    
    @Override
    protected Examen crearRegistroNuevoBase() {
        Examen e = new Examen(UUID.randomUUID());
        e.setActivo(Boolean.TRUE);
        return e;
    }

    @Override
    protected UUID obtenerId(Examen registro) {
        return registro.getIdExamen();
    }

    public void seleccionarExamen(SelectEvent<Examen> event) {
        Examen examenSeleccionado = event.getObject();
        examenTipoExamenModel.cargarPorExamen(examenSeleccionado);
    }

}
