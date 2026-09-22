package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author antonio
 */
@Stateless //
@LocalBean //
public class ConsultaDAO extends DefaultDAO<Consulta> implements Serializable{
    
      // Inyectamos el EntityManager, que es la conexión con la base de datos.
    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    // Entrega la conexión a DefaultDAO para que realice las operaciones.
    @Override
    public EntityManager getEntityManager() {

        return em;
    }
}
