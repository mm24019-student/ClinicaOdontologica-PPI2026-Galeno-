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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenTipoExamen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

@Named
@ViewScoped
public class ExamenTipoExamenModel extends AbstracCrudModel<ExamenTipoExamen> {

    @Inject
    private ExamenTipoExamenDAO eteDAO;

    @Inject
    private ExamenDAO eDAO;

    @Inject
    private TipoExamenDAO teDAO;

    private List<Examen> examenes;
    private List<TipoExamen> tiposExamen;
    private Examen examenPadre;

    @Override
    protected InterfaceDAO<ExamenTipoExamen> getDAO() {
        return eteDAO;
    }

    @Override
    protected ExamenTipoExamen crearRegistroNuevo() {
        ExamenTipoExamen r = new ExamenTipoExamen(UUID.randomUUID());
        r.setFechaCreacion(new Date());

        if (examenPadre != null) {
            r.setIdExamen(examenPadre);
        }

        return r;
    }

    @Override
    protected UUID obtenerId(ExamenTipoExamen registro) {
        return registro.getIdExamenTipoExamen();
    }

    // Valida las dos relaciones antes de guardar
    @Override
    public void btnCrearhandler(ActionEvent ae) {
        if (relacionesCompletas()) {
            super.btnCrearhandler(ae);
        }
    }

    @Override
    public void btnModificarHandler() {
        if (relacionesCompletas()) {
            super.btnModificarHandler();
        }
    }

    private boolean relacionesCompletas() {
        if (registro == null) {
            return true; // el metodo base ya avisa que el registro es nulo
        }
        if (registro.getIdExamen() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione un examen", "El examen es obligatorio"));
            return false;
        }
        if (registro.getIdTipoExamen() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione un tipo de examen", "El tipo de examen es obligatorio"));
            return false;
        }
        return true;
    }

    // Opciones de los selectores (se cargan una vez por vista)
    public List<Examen> getExamenes() {
        if (examenes == null) {
            examenes = eDAO.findRange(0, 100);
        }
        return examenes;
    }

    public List<TipoExamen> getTiposExamen() {
        if (tiposExamen == null) {
            tiposExamen = teDAO.findRange(0, 100);
        }
        return tiposExamen;
    }

    public void cargarPorExamen(Examen examen) {

        examenPadre = examen;
        registro = null;
        estado = Estado_Crud.NINGUNO;

        if (examen != null && examen.getIdExamen() != null) {

            setWrappedData(
                    eteDAO.findByExamen(examen.getIdExamen())
            );

        } else {

            setWrappedData(new ArrayList<>());

        }
    }

    public List<TipoExamen> completarTiposExamen(String query) {

        List<TipoExamen> resultado = new ArrayList<>();

        String texto = query == null
                ? ""
                : query.trim().toLowerCase();

        for (TipoExamen tipo : getTiposExamen()) {

            if (tipo.getActivo() != null
                    && !tipo.getActivo()) {
                continue;
            }

            String nombre = tipo.getNombre() == null
                    ? ""
                    : tipo.getNombre().toLowerCase();

            if (texto.isEmpty() || nombre.contains(texto)) {
                resultado.add(tipo);
            }

            if (resultado.size() >= 20) {
                break;
            }
        }

        return resultado;
    }
}
