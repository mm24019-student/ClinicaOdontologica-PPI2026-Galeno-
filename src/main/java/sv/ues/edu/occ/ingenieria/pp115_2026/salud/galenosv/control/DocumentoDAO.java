package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

/**
 * @author oscar
 */
@Stateless
@LocalBean
public class DocumentoDAO extends DefaultDAO<Documento>{

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

    public List<TipoDocumento> listarTiposDocumento() {
        TypedQuery<TipoDocumento> q = em.createNamedQuery("TipoDocumento.findAll", TipoDocumento.class);
        q.setMaxResults(100);
        return q.getResultList();
    }

    public Persona buscarPersona(UUID id) {
        return id == null ? null : em.find(Persona.class, id);
    }

    public TipoDocumento buscarTipoDocumento(UUID id) {
        return id == null ? null : em.find(TipoDocumento.class, id);
    }

    // Devuelve solo los documentos de una persona especifica. Se usa en la
    // pestana "Documentos" dentro de la pantalla de Persona, en vez de traer
    // siempre los 100 Documento de todas las personas.
    public List<Documento> findByPersona(UUID idPersona) {
        return buscarPorPadre(
                "SELECT d FROM Documento d WHERE d.idPersona.idPersona = :id",
                "id", idPersona);
    }

    // ---- Validaciones de duplicados ----
    // true si la persona YA tiene un documento de ese tipo (ej. ya tiene un DUI).
    // idExcluir es el documento que se esta editando, para que no se compare
    // contra si mismo al modificar; en un registro nuevo no existe en la BD, asi
    // que no afecta.
    public boolean existeTipoParaPersona(UUID idPersona, UUID idTipoDocumento, UUID idExcluir) {
        Long total = em.createQuery(
                "SELECT COUNT(d) FROM Documento d "
                + "WHERE d.idPersona.idPersona = :persona "
                + "AND d.idTipoDocumento.idTipoDocumento = :tipo "
                + "AND d.idDocumento <> :excluir", Long.class)
                .setParameter("persona", idPersona)
                .setParameter("tipo", idTipoDocumento)
                .setParameter("excluir", idExcluir != null ? idExcluir : new UUID(0L, 0L))
                .getSingleResult();
        return total > 0;
    }

    // true si ese mismo numero/valor de ese tipo ya esta registrado (en cualquier
    // persona). Ej: el DUI 01234567-8 no puede pertenecer a dos personas.
    // Compara sin distinguir mayusculas ni espacios en los extremos.
    public boolean existeValorParaTipo(String valor, UUID idTipoDocumento, UUID idExcluir) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        Long total = em.createQuery(
                "SELECT COUNT(d) FROM Documento d "
                + "WHERE LOWER(TRIM(d.valor)) = :valor "
                + "AND d.idTipoDocumento.idTipoDocumento = :tipo "
                + "AND d.idDocumento <> :excluir", Long.class)
                .setParameter("valor", valor.trim().toLowerCase())
                .setParameter("tipo", idTipoDocumento)
                .setParameter("excluir", idExcluir != null ? idExcluir : new UUID(0L, 0L))
                .getSingleResult();
        return total > 0;
    }

}