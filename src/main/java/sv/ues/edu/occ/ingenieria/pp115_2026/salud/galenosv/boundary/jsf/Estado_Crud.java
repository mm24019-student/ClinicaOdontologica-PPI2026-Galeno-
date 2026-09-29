package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

/**
 * Estados en los que puede estar la pantalla de un CRUD (los usa AbstracCrudModel
 * y todos los Model que la heredan):
 * NINGUNO = no hay registro en edición; CREAR = se está escribiendo un registro
 * nuevo (botón Nuevo); MODIFICAR = se seleccionó un registro existente para editarlo.
 * Los botones de la vista se habilitan o deshabilitan según este valor.
 *
 * @author guillermo
 */
public enum Estado_Crud {
    NINGUNO, CREAR, MODIFICAR;
}
