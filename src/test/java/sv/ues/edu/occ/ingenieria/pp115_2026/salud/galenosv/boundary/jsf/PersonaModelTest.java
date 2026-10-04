package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

import static org.mockito.Mockito.*;

/**
 * Prueba de PersonaModel. Lo único propio de esta clase (no heredado de
 * AbstracCrudModel, ya cubierto vía RolModel) es la orquestación: avisarle
 * en cascada a los 3 beans de detalle (PersonaRolDetalleModel,
 * PersonaDocumentoModel, PersonaMedioContactoModel) cuál es la persona
 * activa, y resetear tabActivo para no dejar al usuario en una pestaña
 * inválida.
 *
 * @author oscar
 */
@ExtendWith(MockitoExtension.class)
public class PersonaModelTest {

    @Mock
    private PersonaDAO personaDAO;
    @Mock
    private PersonaRolDetalleModel personaRolDetalleModel;
    @Mock
    private PersonaDocumentoModel personaDocumentoModel;
    @Mock
    private PersonaMedioContactoModel personaMedioContactoModel;
    @Mock
    private ClinicaDAO clinicaDAO;

    @InjectMocks
    private PersonaModel bean;

 
  

    @Test
    public void btnNuevoHandler_avisaEnCascadaConNullALosTresDetalles() {
        bean.btnNuevoHandler(null);

        verify(personaRolDetalleModel).cargarDe(null);
        verify(personaDocumentoModel).cargarDe(null);
        verify(personaMedioContactoModel).cargarDe(null);
    }



    @Test
    @SuppressWarnings("unchecked")
    public void onRowSelect_avisaEnCascadaConLaPersonaALosTresDetalles() {
        Persona persona = new Persona(UUID.randomUUID());
        SelectEvent<Persona> event = mock(SelectEvent.class);
        when(event.getObject()).thenReturn(persona);

        bean.onRowSelect(event);

        verify(personaRolDetalleModel).cargarDe(persona);
        verify(personaDocumentoModel).cargarDe(persona);
        verify(personaMedioContactoModel).cargarDe(persona);
    }

   
}