package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 * @author oscar
 */
@Stateless
@LocalBean
public class MedioContactoDAO extends DefaultDAO<MedioContacto> {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // ---- Métodos "extra" para llenar y resolver los combos del formulario ----
    public List<Persona> listarPersonas() {
        TypedQuery<Persona> q = em.createNamedQuery("Persona.findAll", Persona.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    public List<TipoMedioContacto> listarTiposMedioContacto() {
        TypedQuery<TipoMedioContacto> q = em.createNamedQuery("TipoMedioContacto.findAll", TipoMedioContacto.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    public Persona buscarPersona(UUID id) {
        return id == null ? null : em.find(Persona.class, id);
    }

    public TipoMedioContacto buscarTipoMedioContacto(UUID id) {
        return id == null ? null : em.find(TipoMedioContacto.class, id);
    }

    // Devuelve solo los medios de contacto de una persona especifica. Se usa
    // en la pestana "Medios de Contacto" dentro de la pantalla de Persona,
    // en vez de traer siempre los 100 MedioContacto de todas las personas.
    public List<MedioContacto> findByPersona(UUID idPersona) {
        return buscarPorPadre(
                "SELECT m FROM MedioContacto m WHERE m.idPersona.idPersona = :id",
                "id", idPersona);
    }

}