package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antonio
 */
public abstract class DefaultDAO<T> implements InterfaceDAO<T>, Serializable{
    
    private static final long serialVersionUID = 1L;

    public abstract EntityManager getEntityManager();

    @SuppressWarnings("unchecked")

    private Class<T> getEntityClass() {
        ParameterizedType tipoParametrizado = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) tipoParametrizado.getActualTypeArguments()[0];
    }

    @Override
    public void crear(T registro) throws IllegalArgumentException, IllegalStateException {
        if (registro != null) {
            try {
                getEntityManager().persist(registro);
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                throw new IllegalStateException("Error al crear el registro", ex);
            }
        } else {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
    }

    @Override
    public void eliminar(UUID id) throws IllegalArgumentException, IllegalStateException {
        if (id != null) {
            try {
                T managed = getEntityManager().find(getEntityClass(), id);
                if (managed != null) {
                    getEntityManager().remove(managed);
                } else {
                    throw new IllegalArgumentException("No existe un registro con ese id");
                }
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                throw new IllegalStateException("Error al eliminar el registro", ex);
            }
        } else {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
    }

    @Override
    public T actualizar(T registro) throws IllegalArgumentException, IllegalStateException {
        if (registro != null) {
            try {
                return getEntityManager().merge(registro);
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                throw new IllegalStateException("Error al actualizar el registro", ex);
            }
        } else {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

    }

    @Override
    public T buscar(UUID id) throws IllegalArgumentException, IllegalStateException {
        if (id != null) {
            try {
                return getEntityManager().find(getEntityClass(), id);
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                throw new IllegalStateException("Error al buscar el registro", ex);
            }
        } else {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
    }

    String getFindAllQueryName() {
        return getEntityClass().getSimpleName() + ".findAll";
    }

    @Override
    public List<T> findRange(int first, int max) throws IllegalArgumentException, IllegalStateException {
        if (first >= 0 && max > 0) {
            try {
                TypedQuery<T> q = getEntityManager().createNamedQuery(getFindAllQueryName(), getEntityClass());
                q.setFirstResult(first);
                q.setMaxResults(max);
                return q.getResultList();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                throw new IllegalStateException("Error al consultar los registros", ex);
            }
        }
        throw new IllegalArgumentException("first debe ser >= 0 y max debe ser > 0");
    }
}
