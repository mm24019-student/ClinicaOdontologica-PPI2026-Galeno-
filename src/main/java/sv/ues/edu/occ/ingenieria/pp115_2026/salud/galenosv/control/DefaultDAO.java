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
//Clase abstracta DefaultDAO que implementa la interfaz InterfaceDAO y 
//proporciona una implementación genérica de los métodos CRUD (crear, eliminar, actualizar, buscar y findRange) para entidades JPA.
public abstract class DefaultDAO<T> implements InterfaceDAO<T>, Serializable{
    
//Declaración de la constante serialVersionUID para la serialización de objetos
    private static final long serialVersionUID = 1L;


//Declaración del método abstracto getEntityManager() que debe ser implementado por las subclases para proporcionar 
//el EntityManager específico de la entidad.
    public abstract EntityManager getEntityManager();

    @SuppressWarnings("unchecked")

//Declaración del método privado getEntityClass() que obtiene la clase de la entidad genérica T, 
//para luego retornar la clase de la entidad genérica T utilizando reflexión. Este método se utiliza para obtener la clase de la entidad en tiempo de ejecución.
    private Class<T> getEntityClass() {
        ParameterizedType tipoParametrizado = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) tipoParametrizado.getActualTypeArguments()[0];
    }

//Implementación del método crear() para crear un registro en la base de datos. Verifica si el registro no es nulo,
//si es válido, guarda el registro utilizando el EntityManager. Si ocurre algún error, se registra en el log y se lanza una excepción.
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

    //Implementación del método eliminar() para eliminar un registro de la base de datos. Verifica si el id no es nulo,
    //busca el registro en la base de datos y lo elimina si existe. Si ocurre algún error, se registra en el log y se lanza una excepción.
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

//Implementación del método actualizar() para actualizar un registro en la base de datos. Verifica si el registro no es nulo,
//si es válido, actualiza el registro utilizando el EntityManager. Si ocurre algún error,
// se registra en el log y se lanza una excepción.
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

//Implementación del método buscar() para buscar un registro en la base de datos por su id. Verifica si el id no es nulo,
//si es válido, busca el registro utilizando el EntityManager. Si ocurre algún error,
// se registra en el log y se lanza una excepción.
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

//Implementación del método findRange() para obtener un rango de registros de la base de datos. Verifica si los parámetros first y max son válidos,
//si son válidos, crea una consulta utilizando el EntityManager y establece los resultados a partir de los parámetros first y max.
//Si ocurre algún error, se registra en el log y se lanza una excepcion.
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
