package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RolConverterTest {

    @Mock
    private RolDAO rolDAO;

    @InjectMocks
    private RolConverter converter;

    @Test
    public void dao_devuelveElDaoInyectado() {
        assertSame(rolDAO, converter.dao());
    }

    @Test
    public void id_devuelveElIdRol() {
        UUID id = UUID.randomUUID();
        assertEquals(id, converter.id(new Rol(id)));
    }

    @Test
    public void getAsObject_uuidValido_buscaLaEntidadEnElDao() {
        UUID id = UUID.randomUUID();
        Rol esperada = new Rol(id);
        when(rolDAO.buscar(id)).thenReturn(esperada);

        assertSame(esperada, converter.getAsObject(null, null, id.toString()));
    }

    @Test
    public void getAsString_entidadConId_devuelveElUuidComoTexto() {
        UUID id = UUID.randomUUID();
        assertEquals(id.toString(), converter.getAsString(null, null, new Rol(id)));
    }

    @Test
    public void getAsString_entidadSinId_devuelveCadenaVacia() {
        assertEquals("", converter.getAsString(null, null, new Rol()));
    }
}