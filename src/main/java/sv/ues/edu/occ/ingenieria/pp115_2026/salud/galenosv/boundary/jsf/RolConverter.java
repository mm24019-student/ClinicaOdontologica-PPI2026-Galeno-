package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;


import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "rolConverter", managed = true)
public class RolConverter implements Converter<Rol>{

    
    @Inject
    private RolDAO dao;

    @Override
    public Rol getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, Rol rol) {
        return (rol == null || rol.getIdRol() == null) ? "" : rol.getIdRol().toString();
    }
}