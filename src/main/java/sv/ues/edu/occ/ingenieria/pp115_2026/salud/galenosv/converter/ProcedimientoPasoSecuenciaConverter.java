package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoPasoSecuenciaConverter", managed = true) 
public class ProcedimientoPasoSecuenciaConverter implements Converter<ProcedimientoPasoSecuencia> {
    
       @Inject
    private ProcedimientoPasoSecuenciaDAO dao;
 
    @Override
    public ProcedimientoPasoSecuencia getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }
 
    @Override
    public String getAsString(FacesContext ctx, UIComponent c, ProcedimientoPasoSecuencia secuencia) {
        return (secuencia == null || secuencia.getIdProcedimientoPasoSecuencia() == null)
                ? "" : secuencia.getIdProcedimientoPasoSecuencia().toString();
    }
}
