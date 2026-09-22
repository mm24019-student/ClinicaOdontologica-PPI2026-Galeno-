package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author oscar
 */
@Entity
@Table(name = "procedimiento", schema = "public")
@NamedQueries({
        @NamedQuery(name = "Procedimiento.findAll", query = "SELECT p FROM Procedimiento p"),
        @NamedQuery(name = "Procedimiento.findByNombre", query = "SELECT p FROM Procedimiento p WHERE p.nombre = :nombre"),
        @NamedQuery(name = "Procedimiento.findByActivo", query = "SELECT p FROM Procedimiento p WHERE p.activo = :activo"),
        @NamedQuery(name = "Procedimiento.findByObservaciones", query = "SELECT p FROM Procedimiento p WHERE p.observaciones = :observaciones") })
public class Procedimiento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_procedimiento")
    private UUID idProcedimiento;
    @Size(min = 5, max = 156, message = "El nombre debe tener entre 5 y 156 caracteres")
    @Column(name = "nombre")
    @NotBlank(message = "El nombre del procedimiento es obligatorio")
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "idProcedimiento", fetch = FetchType.LAZY)
    private List<ProcedimientoPaso> procedimientoPasoList;

    public Procedimiento() {
    }

    public Procedimiento(UUID idProcedimiento) {
        this.idProcedimiento = idProcedimiento;
    }

    public UUID getIdProcedimiento() {
        return idProcedimiento;
    }

    public void setIdProcedimiento(UUID idProcedimiento) {
        this.idProcedimiento = idProcedimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<ProcedimientoPaso> getProcedimientoPasoList() {
        return procedimientoPasoList;
    }

    public void setProcedimientoPasoList(List<ProcedimientoPaso> procedimientoPasoList) {
        this.procedimientoPasoList = procedimientoPasoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProcedimiento != null ? idProcedimiento.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Procedimiento)) {
            return false;
        }
        Procedimiento other = (Procedimiento) object;
        if ((this.idProcedimiento == null && other.idProcedimiento != null)
                || (this.idProcedimiento != null && !this.idProcedimiento.equals(other.idProcedimiento))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.ues.edu.occ.ingenieria.pp115_2026.salud.galeanosv.entity.Procedimiento[ idProcedimiento="
                + idProcedimiento + " ]";
    }

}