package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

@Named
@ViewScoped
public class PersonaModel extends AbstracCrudTabsModel<Persona> {

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
    
    // Recuerda en qué pestaña estaba el usuario para que los refrescos
    // ajax (seleccionar fila, crear/actualizar rol, etc.) no lo regresen
    // siempre a "Datos de Persona".
    @Override
    protected InterfaceDAO<Persona> getDAO() {
        return personaDAO;
    }

    @Override
    protected void resetearHijos() {
        // La orden todavía no existe / ya no aplica -> el hijo (Resultados)
        // no debe conservar el registro/estado de la orden anterior.
        personaRolDetalleModel.cargarDe(null);
        personaDocumentoModel.cargarDe(null);
        personaMedioContactoModel.cargarDe(null);
    }

    @Override
    protected Persona crearRegistroNuevoBase() {
        Persona p = new Persona(UUID.randomUUID());
        p.setFechaCreacion(new Date());
        // Las pestañas de detalle se vacían en resetearHijos(), que
        // AbstracCrudTabsModel.crearRegistroNuevo() invoca justo después.
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
        personaRolDetalleModel.cargarDe(registro);
        personaDocumentoModel.cargarDe(registro);
        personaMedioContactoModel.cargarDe(registro);
    }
}