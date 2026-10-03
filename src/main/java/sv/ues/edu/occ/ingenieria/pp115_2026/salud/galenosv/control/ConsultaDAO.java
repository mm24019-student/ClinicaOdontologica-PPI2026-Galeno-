package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;

/**
 *
 * @author antonio
 */
@Stateless //
@LocalBean //
public class ConsultaDAO extends DefaultDAO<Consulta> {

    // Inyectamos el EntityManager, que es la conexión con la base de datos.
    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    // Entrega la conexión a DefaultDAO para que realice las operaciones.
    @Override
    public EntityManager getEntityManager() {

        return em;
    }

    // Busca consultas por rango de fecha de inicio y/o por clínica. Cualquier
    // parámetro en null simplemente no filtra.
    //  - desde:          incluye consultas con fechaInicio >= desde
    //  - hastaExclusivo: incluye consultas con fechaInicio <  hastaExclusivo
    //                    (el modelo manda el inicio del día siguiente al "Hasta"
    //                    para que ese día completo entre en el resultado)
    //  - idClinica:      clínica de la persona/rol que registró la consulta
    // Trae persona, rol y clínica con JOIN FETCH porque la tabla los muestra
    // y las relaciones son LAZY. Las más recientes primero.
    public List<Consulta> buscarConFiltro(Date desde, Date hastaExclusivo, UUID idClinica, int max) {
        List<String> condiciones = new ArrayList<>();
        if (desde != null) {
            condiciones.add("c.fechaInicio >= :desde");
        }
        if (hastaExclusivo != null) {
            condiciones.add("c.fechaInicio < :hasta");
        }
        if (idClinica != null) {
            condiciones.add("pr.idClinica.idClinica = :idClinica");
        }
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT c FROM Consulta c "
                + "LEFT JOIN FETCH c.idPersonaRol pr "
                + "LEFT JOIN FETCH pr.idPersona p "
                + "LEFT JOIN FETCH p.documentoList d "
                + "LEFT JOIN FETCH d.idTipoDocumento "
                + "LEFT JOIN FETCH pr.idRol "
                + "LEFT JOIN FETCH pr.idClinica ");
        if (!condiciones.isEmpty()) {
            jpql.append("WHERE ").append(String.join(" AND ", condiciones)).append(' ');
        }
        jpql.append("ORDER BY c.fechaInicio DESC");

        TypedQuery<Consulta> q = em.createQuery(jpql.toString(), Consulta.class);
        if (desde != null) {
            q.setParameter("desde", desde);
        }
        if (hastaExclusivo != null) {
            q.setParameter("hasta", hastaExclusivo);
        }
        if (idClinica != null) {
            q.setParameter("idClinica", idClinica);
        }
        q.setMaxResults(max);
        return q.getResultList();
    }
}
