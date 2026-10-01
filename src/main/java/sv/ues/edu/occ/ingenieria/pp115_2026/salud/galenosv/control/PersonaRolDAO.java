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
    // Devuelve todas las personas, no solo las asignadas a un rol.
    public List<Persona> listarPersonas() {
        TypedQuery<Persona> q = em.createNamedQuery("Persona.findAll", Persona.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    // Devuelve todos los roles, no solo los asignados a una persona.
    public List<Rol> listarRoles() {
        TypedQuery<Rol> q = em.createNamedQuery("Rol.findAll", Rol.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    // Devuelve todas las clinicas, no solo las asignadas a una persona.
    public List<Clinica> listarClinicas() {
        TypedQuery<Clinica> q = em.createNamedQuery("Clinica.findAll", Clinica.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    // Devuelve una persona por su id, o null si no existe.
    public Persona buscarPersona(UUID id) {
        return id == null ? null : em.find(Persona.class, id);
    }

    public Rol buscarRol(UUID id) {
        return id == null ? null : em.find(Rol.class, id);
    }

    public Clinica buscarClinica(UUID id) {
        return id == null ? null : em.find(Clinica.class, id);
    }

    // Devuelve solo los roles/clinica asignados a una persona especifica.
    // Se usa en la pestana "Roles" dentro de la pantalla de Persona, en vez
    // de traer siempre los 100 PersonaRol de todas las personas.
    public List<PersonaRol> findByPersona(UUID idPersona) {
        return buscarPorPadre(
                "SELECT pr FROM PersonaRol pr WHERE pr.idPersona.idPersona = :id",
                "id", idPersona);
    }

    // Devuelve todas las combinaciones persona/rol/clinica con sus relaciones
    // ya cargadas (JOIN FETCH), porque las entidades son LAZY y el selector
    // de sesion las lee fuera de la transaccion.
    public List<PersonaRol> listarConDetalle() {
        TypedQuery<PersonaRol> q = em.createQuery(
                "SELECT pr FROM PersonaRol pr "
                + "JOIN FETCH pr.idPersona p "
                + "JOIN FETCH pr.idRol r "
                + "LEFT JOIN FETCH pr.idClinica c "
                + "ORDER BY p.apellidos, p.nombres, r.nombre",
                PersonaRol.class);
        q.setMaxResults(200);
        return q.getResultList();
    }

}