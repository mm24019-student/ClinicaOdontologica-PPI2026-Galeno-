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
@Table(name = "rol", schema = "public")
@NamedQueries({
    @NamedQuery(name = "Rol.findAll", query = "SELECT r FROM Rol r"),
    @NamedQuery(name = "Rol.findByNombre", query = "SELECT r FROM Rol r WHERE r.nombre = :nombre"),
    @NamedQuery(name = "Rol.findByActivo", query = "SELECT r FROM Rol r WHERE r.activo = :activo"),
    @NamedQuery(name = "Rol.findByObservaciones", query = "SELECT r FROM Rol r WHERE r.observaciones = :observaciones")})
public class Rol implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_rol")
    private UUID idRol;
    @Size(max = 155)
    @NotBlank
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "idRol", fetch = FetchType.LAZY)
    private List<PersonaRol> personaRolList;
    @OneToMany(mappedBy = "idRol", fetch = FetchType.LAZY)
    private List<ProcedimientoPaso> procedimientoPasoList;
    public Rol() {
    }
    public Rol(UUID idRol) {
        this.idRol = idRol;
    }
    public UUID getIdRol() {
        return idRol;
    }
    public void setIdRol(UUID idRol) {
        this.idRol = idRol;
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
    public List<PersonaRol> getPersonaRolList() {
        return personaRolList;
    }
    public void setPersonaRolList(List<PersonaRol> personaRolList) {
        this.personaRolList = personaRolList;
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
        hash += (idRol != null ? idRol.hashCode() : 0);
        return hash;
    }
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Rol)) {
            return false;
        }
        Rol other = (Rol) object;
        if ((this.idRol == null && other.idRol != null) || (this.idRol != null && !this.idRol.equals(other.idRol))) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return "sv.ues.edu.occ.ingenieria.pp115_2026.salud.galeanosv.entity.Rol[ idRol=" + idRol + " ]";
    }
    
}