package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class TipoDocumentoDAO extends DefaultDAO<TipoDocumento> {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {

        return em;
    }

    // true si ya existe un tipo de documento con ese nombre (ej. dos "DUI").
    // Compara sin distinguir mayusculas ni espacios en los extremos.
    // idExcluir es el registro que se esta editando, para no compararlo consigo
    // mismo al modificar; en uno nuevo no existe en la BD, asi que no afecta.
    public boolean existeNombre(String nombre, UUID idExcluir) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        Long total = em.createQuery(
                "SELECT COUNT(t) FROM TipoDocumento t "
                + "WHERE LOWER(TRIM(t.nombre)) = :nombre "
                + "AND t.idTipoDocumento <> :excluir", Long.class)
                .setParameter("nombre", nombre.trim().toLowerCase())
                .setParameter("excluir", idExcluir != null ? idExcluir : new UUID(0L, 0L))
                .getSingleResult();
        return total > 0;
    }

}