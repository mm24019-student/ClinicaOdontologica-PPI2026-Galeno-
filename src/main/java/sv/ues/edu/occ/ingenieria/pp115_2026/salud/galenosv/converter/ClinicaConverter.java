package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;

@Named
@ApplicationScoped
public class ClinicaConverter extends AbstractEntityConverter<Clinica> {

    @Inject
    private ClinicaService dao;

    @Override
    protected InterfaceService<Clinica> dao() {
        return dao;
    }

    @Override
    protected UUID id(Clinica entidad) {
        return entidad.getIdClinica();
    }
}