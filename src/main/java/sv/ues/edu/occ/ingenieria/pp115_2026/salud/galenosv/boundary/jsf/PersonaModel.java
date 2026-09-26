package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

@Named
@ViewScoped
public class PersonaModel extends AbstracCrudModel<Persona> {

    @Inject
    private PersonaDAO personaDAO;

    // La asignación de Rol/Clinica ya no vive aquí: se delega por completo
    // a PersonaRolDetalleModel (AbstracdetallecrudModel<PersonaRol, Persona>),
    // que se encarga de cargar/crear/eliminar los PersonaRol de la persona
    // activa. PersonaModel solo le avisa cuál es esa persona.
    @Inject
    private PersonaRolDetalleModel personaRolDetalleModel;

    // Mismo patrón para las pestañas de Documentos y Medios de Contacto:
    // cada una es su propio AbstracdetallecrudModel<T, Persona>, y
    // PersonaModel solo les avisa cuál es la persona activa.
    @Inject
    private PersonaDocumentoModel personaDocumentoModel;
    @Inject
    private PersonaMedioContactoModel personaMedioContactoModel;

    @Inject
    private ClinicaDAO clinicaDAO;

    // Recuerda en qué pestaña estaba el usuario para que los refrescos
    // ajax (seleccionar fila, crear/actualizar rol, etc.) no lo regresen
    // siempre a "Datos de Persona".
    private int tabActivo = 0;

    public int getTabActivo() {
        return tabActivo;
    }

    public void setTabActivo(int tabActivo) {
        this.tabActivo = tabActivo;
    }

    @Override
    protected InterfaceDAO<Persona> getDAO() {
        return personaDAO;
    }

    @Override
    protected Persona crearRegistroNuevo() {
        Persona p = new Persona(UUID.randomUUID());
        p.setFechaCreacion(new Date());
        tabActivo = 0;
        // Todavía no hay persona guardada: las pestañas de detalle arrancan vacías.
        personaRolDetalleModel.cargarDe(null);
        personaDocumentoModel.cargarDe(null);
        personaMedioContactoModel.cargarDe(null);
        return p;
    }

    @Override
    protected UUID obtenerId(Persona registro) {
        return registro.getIdPersona();
    }

    // La tabla sigue usando p:dataTable + rowSelect, tal cual ya lo tenias.
    // Solo agregamos: despues de que AbstracCrudModel hace lo suyo (fija
    // registro y pone estado=MODIFICAR), le avisamos a PersonaRolDetalleModel
    // cuál es la persona activa para que cargue sus roles.
    @Override
    public void onRowSelect(SelectEvent<Persona> event) {
        super.onRowSelect(event);
        tabActivo = 0;
        personaRolDetalleModel.cargarDe(registro);
        personaDocumentoModel.cargarDe(registro);
        personaMedioContactoModel.cargarDe(registro);
    }

    public List<Clinica> getClinicasTabla() {
        return clinicaDAO.findRange(0, 100);
    }
}