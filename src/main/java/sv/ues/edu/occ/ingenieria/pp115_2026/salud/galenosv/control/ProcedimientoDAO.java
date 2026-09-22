package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
//Utilizamo Stateless para que que la clase no guarde estado entre invocacionnes.
@Stateless
//Utilizamos LocalBean para poder utiilizar la clase en el mismo proyecto sin necesidad de crear metodos de acceso.
@LocalBean
//Heredamos de DefaultDAO para poder utilizar los metodos genericos de la clase y implementamos 
public class ProcedimientoDAO extends DefaultDAO<Procedimiento>{

    //Creamos un EntityManager para poder realizar las operaciones de persistencia en la base de datos.
    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

//Retornamos el EntityManager para poder utilizarlo en la clase padre que seria DefaultDAO.
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
