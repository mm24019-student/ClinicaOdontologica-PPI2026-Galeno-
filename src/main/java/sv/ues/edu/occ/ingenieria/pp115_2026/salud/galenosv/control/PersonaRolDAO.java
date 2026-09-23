package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * @author oscar
 */
@Stateless
@LocalBean
public class PersonaRolDAO extends DefaultDAO<PersonaRol>{

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

    public List<Rol> listarRoles() {
        TypedQuery<Rol> q = em.createNamedQuery("Rol.findAll", Rol.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    public List<Clinica> listarClinicas() {
        TypedQuery<Clinica> q = em.createNamedQuery("Clinica.findAll", Clinica.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    public Persona buscarPersona(UUID id) {
        return id == null ? null : em.find(Persona.class, id);
    }

    public Rol buscarRol(UUID id) {
        return id == null ? null : em.find(Rol.class, id);
    }

    public Clinica buscarClinica(UUID id) {
        return id == null ? null : em.find(Clinica.class, id);
    }

}