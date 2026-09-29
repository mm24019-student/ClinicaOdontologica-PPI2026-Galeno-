package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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

    // completeMethod del autocomplete de Examen. Usa filtrar() de
    // AbstracCrudModel (busca por nombre sin distinguir mayúsculas),
    // omite inactivos y limita a 20 sugerencias.
    public List<Examen> completarExamenes(String query) {
        return filtrar(getExamenes(), query, Examen::getNombre).stream()
                .filter(e -> e.getActivo() == null || e.getActivo())
                .limit(20)
                .collect(Collectors.toList());
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

    // completeMethod del autocomplete de Tipo de examen (misma lógica).
    public List<TipoExamen> completarTiposExamen(String query) {
        return filtrar(getTiposExamen(), query, TipoExamen::getNombre).stream()
                .filter(t -> t.getActivo() == null || t.getActivo())
                .limit(20)
                .collect(Collectors.toList());
    }

// Tras crear/modificar/eliminar, AbstracCrudModel llama a este hook.
// Si hay examen padre (pestaña dentro de Examen) se filtra por ese examen;
// si no (pantalla independiente), lista todo.
    @Override
    protected void recargarLista() {
        if (examenPadre != null) {
            cargarPorExamen(examenPadre);
        } else {
            super.recargarLista();
        }
    }

    @Override
    protected boolean validarAntesDeGuardar() {
        return requerir(registro.getIdExamen(), "Seleccione un examen", "El examen es obligatorio")
                && requerir(registro.getIdTipoExamen(), "Seleccione un tipo de examen", "El tipo de examen es obligatorio");
    }

}
