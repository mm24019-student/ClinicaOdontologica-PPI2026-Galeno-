package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author antonio
 */
//FacesConverter(value = "consultaConverter", managed = true) indica que esta clase es un convertidor de JSF para la entidad Consulta.
//El atributo value define el nombre del convertidor, que se puede usar en las páginas JSF para convertir entre objetos Consulta y sus
//representaciones en cadena.
@FacesConverter(value = "consultaConverter", managed = true)

//La anotación @Inject se utiliza para inyectar una instancia de ConsultaDAO en esta clase, lo que permite acceder a los métodos de
//ConsultaDAO para buscar consultas en la base de datos.
public class ConsultaConverter implements Converter<Consulta>{
    
//Inyectamos la dependencia de ConsultaDAO para poder utilizar sus métodos en el convertidor.
     @Inject
    private ConsultaDAO dao;

    @Override
    public Consulta getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }

//El método getAsObject convierte una cadena (valor) en un objeto Consulta. Si el valor es nulo o está en blanco, devuelve null.
//Si el valor no es nulo ni está en blanco, se convierte a un UUID.
    @Override
    public String getAsString(FacesContext ctx, UIComponent c, Consulta consulta) {
        return (consulta == null || consulta.getIdConsulta() == null) ? "" : consulta.getIdConsulta().toString();
    }
}
