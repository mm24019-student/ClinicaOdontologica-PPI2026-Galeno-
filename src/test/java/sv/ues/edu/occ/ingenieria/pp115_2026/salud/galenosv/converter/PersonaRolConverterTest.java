/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.convert.ConverterException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;


/**
 * Prueba de PersonaRolConverter. La lógica común (getAsObject / getAsString) ya
 * queda cubierta por AbstractEntityConverterTest; aquí solo se prueba lo
 * propio: el DAO que usa, la llave primaria que expone y que todo funciona
 * de punta a punta con esa entidad.
 *
 * @author antonio
 */
@ExtendWith(MockitoExtension.class)
public class PersonaRolConverterTest {
 
    @Mock
    private PersonaRolDAO personaRolDAO;
 
    @InjectMocks
    private PersonaRolConverter converter;
 
    @Test
    public void dao_devuelveElDaoInyectado() {
        assertSame(personaRolDAO, converter.dao());
    }
 
    @Test
    public void id_devuelveElIdPersonaRol() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, converter.id(new PersonaRol(id)));
    }
 
    @Test
    public void getAsObject_uuidValido_buscaLaEntidadEnElDao() {
        UUID id = UUID.randomUUID();
        PersonaRol esperada = new PersonaRol(id);
        when(personaRolDAO.buscar(id)).thenReturn(esperada);
 
        assertSame(esperada, converter.getAsObject(null, null, id.toString()));
    }
 
    @Test
    public void getAsString_entidadConId_devuelveElUuidComoTexto() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id.toString(), converter.getAsString(null, null, new PersonaRol(id)));
    }
 
    @Test
    public void getAsString_entidadSinId_devuelveCadenaVacia() {
        assertEquals("", converter.getAsString(null, null, new PersonaRol()));
    }
 
    @Test
    public void mensajeValorInvalido_pideUnaPersonaRolValida() {
        assertEquals("Seleccione una persona/rol válida de la lista", converter.mensajeValorInvalido());
    }
 
    @Test
    public void getAsObject_valorQueNoEsUuid_usaElMensajeDePersonaRol() {
        ConverterException ex = assertThrows(ConverterException.class,
                () -> converter.getAsObject(null, null, "abc"));
 
        assertEquals(FacesMessage.SEVERITY_ERROR, ex.getFacesMessage().getSeverity());
        assertEquals("Seleccione una persona/rol válida de la lista", ex.getFacesMessage().getSummary());
        verifyNoInteractions(personaRolDAO);
    }
}
 