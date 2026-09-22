package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author oscar
 */
@Entity
@Table(name = "medio_contacto", schema = "public")
@NamedQueries({
    @NamedQuery(name = "MedioContacto.findAll", query = "SELECT m FROM MedioContacto m"),
    @NamedQuery(name = "MedioContacto.findByValor", query = "SELECT m FROM MedioContacto m WHERE m.valor = :valor"),
    @NamedQuery(name = "MedioContacto.findByFechaCreacion", query = "SELECT m FROM MedioContacto m WHERE m.fechaCreacion = :fechaCreacion")})
public class MedioContacto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_medio_contacto")
    private UUID idMedioContacto;
    @Size(min = 3, max = 250)
    @NotBlank
    @Column(name = "valor")
    private String valor;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @JoinColumn(name = "id_persona", referencedColumnName = "id_persona")
    @ManyToOne(fetch = FetchType.LAZY)
    private Persona idPersona;
    @JoinColumn(name = "id_tipo_medio_contacto", referencedColumnName = "id_tipo_medio_contacto")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoMedioContacto idTipoMedioContacto;

    public MedioContacto() {
    }

    public MedioContacto(UUID idMedioContacto) {
        this.idMedioContacto = idMedioContacto;
    }

    public UUID getIdMedioContacto() {
        return idMedioContacto;
    }

    public void setIdMedioContacto(UUID idMedioContacto) {
        this.idMedioContacto = idMedioContacto;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Persona getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Persona idPersona) {
        this.idPersona = idPersona;
    }

    public TipoMedioContacto getIdTipoMedioContacto() {
        return idTipoMedioContacto;
    }

    public void setIdTipoMedioContacto(TipoMedioContacto idTipoMedioContacto) {
        this.idTipoMedioContacto = idTipoMedioContacto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMedioContacto != null ? idMedioContacto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MedioContacto)) {
            return false;
        }
        MedioContacto other = (MedioContacto) object;
        if ((this.idMedioContacto == null && other.idMedioContacto != null) || (this.idMedioContacto != null && !this.idMedioContacto.equals(other.idMedioContacto))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.ues.edu.occ.ingenieria.pp115_2026.salud.galeanosv.entity.MedioContacto[ idMedioContacto=" + idMedioContacto + " ]";
    }

}