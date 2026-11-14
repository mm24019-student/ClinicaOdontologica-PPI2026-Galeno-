package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

/**
 * Generador del comprobante PDF de una cita usando OpenPDF.
 */
public final class TicketPdf {

    public static final class Procedimiento {
        final String nombre;
        final Date fechaInicio;
        final List<Paso> pasos = new ArrayList<>();

        public Procedimiento(String nombre, Date fechaInicio) {
            this.nombre = nombre;
            this.fechaInicio = fechaInicio;
        }

        public void agregarPaso(Paso paso) {
            pasos.add(paso);
        }
    }

    public static final class Paso {
        final String nombre;
        final String estado;
        final String valor;
        final Date fechaInicio;
        final String persona;
        final String rol;

        public Paso(String nombre, String estado, String valor, Date fechaInicio, String persona, String rol) {
            this.nombre = nombre;
            this.estado = estado;
            this.valor = valor;
            this.fechaInicio = fechaInicio;
            this.persona = persona;
            this.rol = rol;
        }
    }

    private TicketPdf() {
    }

    public static byte[] generar(Consulta consulta, List<Procedimiento> procedimientos) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(42, 27, 101));
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(0, 136, 127));
            Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font pequeno = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

            Paragraph pTitulo = new Paragraph("GalenoSV - Comprobante de cita", titulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitulo);
            Paragraph pGen = new Paragraph("Generado el " + fecha(new Date()), pequeno);
            pGen.setAlignment(Element.ALIGN_CENTER);
            pGen.setSpacingAfter(12f);
            doc.add(pGen);

            doc.add(new Paragraph("Resumen de la cita", subtitulo));
            doc.add(tablaCita(consulta, etiqueta, normal));

            doc.add(espacio(8f));
            doc.add(new Paragraph("Procedimientos", subtitulo));
            if (procedimientos == null || procedimientos.isEmpty()) {
                Paragraph vacio = new Paragraph("Sin procedimientos registrados.", normal);
                vacio.setSpacingBefore(4f);
                doc.add(vacio);
            } else {
                for (Procedimiento proc : procedimientos) {
                    Paragraph cab = new Paragraph(texto(proc.nombre) + "  (inicio: " + fecha(proc.fechaInicio) + ")", etiqueta);
                    cab.setSpacingBefore(8f);
                    cab.setSpacingAfter(2f);
                    doc.add(cab);
                    if (proc.pasos == null || proc.pasos.isEmpty()) {
                        doc.add(new Paragraph("    Sin pasos registrados.", normal));
                    } else {
                        doc.add(tablaPasos(proc.pasos, etiqueta, normal, pequeno));
                    }
                }
            }

            doc.close();
        } catch (DocumentException ex) {
            throw new IllegalStateException("No se pudo generar el comprobante PDF", ex);
        }
        return salida.toByteArray();
    }

    private static PdfPTable tablaCita(Consulta consulta, Font etiqueta, Font normal) {
        PdfPTable tabla = new PdfPTable(new float[]{1.2f, 2.8f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(6f);
        fila(tabla, "Fecha y hora", fecha(consulta == null ? null : consulta.getFechaInicio()), etiqueta, normal);
        fila(tabla, "Fin de la cita", fecha(consulta == null ? null : consulta.getFechaFin()), etiqueta, normal);
        PersonaRol pr = consulta == null ? null : consulta.getIdPersonaRol();
        Persona persona = pr == null ? null : pr.getIdPersona();
        fila(tabla, "Paciente", persona == null ? "-"
                : (texto(persona.getNombres()) + " " + texto(persona.getApellidos())).trim(), etiqueta, normal);
        fila(tabla, "Documentos", documentos(persona), etiqueta, normal);
        fila(tabla, "Clínica", pr == null || pr.getIdClinica() == null ? "-" : texto(pr.getIdClinica().getNombre()), etiqueta, normal);
        fila(tabla, "Rol", pr == null || pr.getIdRol() == null ? "-" : texto(pr.getIdRol().getNombre()), etiqueta, normal);
        fila(tabla, "Referencia", consulta == null ? "-" : texto(consulta.getReferenciaExterna()), etiqueta, normal);
        fila(tabla, "Observaciones", consulta == null ? "-" : texto(consulta.getObservaciones()), etiqueta, normal);
        return tabla;
    }

    private static PdfPTable tablaPasos(List<Paso> pasos, Font etiqueta, Font normal, Font pequeno) {
        PdfPTable tabla = new PdfPTable(new float[]{2.2f, 1.1f, 1.8f, 1.4f, 2f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(4f);
        encabezado(tabla, "Paso", etiqueta);
        encabezado(tabla, "Estado", etiqueta);
        encabezado(tabla, "Responsable", etiqueta);
        encabezado(tabla, "Fecha", etiqueta);
        encabezado(tabla, "Registro", etiqueta);
        for (Paso paso : pasos) {
            celda(tabla, texto(paso.nombre), normal);
            celda(tabla, texto(paso.estado), normal);
            String resp = texto(paso.persona);
            if (paso.rol != null && !paso.rol.isBlank()) {
                resp = resp + " (" + paso.rol + ")";
            }
            celda(tabla, resp.trim(), normal);
            celda(tabla, fecha(paso.fechaInicio), pequeno);
            celda(tabla, paso.valor == null ? "" : paso.valor.trim().replace("\n", " "), pequeno);
        }
        return tabla;
    }

    private static void fila(PdfPTable tabla, String et, String valor, Font etiqueta, Font normal) {
        PdfPCell c1 = new PdfPCell(new Phrase(et, etiqueta));
        c1.setPadding(5f);
        c1.setBackgroundColor(new Color(238, 241, 247));
        tabla.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(valor == null || valor.isBlank() ? "-" : valor, normal));
        c2.setPadding(5f);
        tabla.addCell(c2);
    }

    private static void encabezado(PdfPTable tabla, String texto, Font etiqueta) {
        PdfPCell c = new PdfPCell(new Phrase(texto, etiqueta));
        c.setPadding(4f);
        c.setBackgroundColor(new Color(42, 27, 101));
        c.setPhrase(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
        tabla.addCell(c);
    }

    private static void celda(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null || texto.isBlank() ? "-" : texto, fuente));
        c.setPadding(4f);
        tabla.addCell(c);
    }

    private static Paragraph espacio(float puntos) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(puntos);
        return p;
    }

    private static String documentos(Persona persona) {
        if (persona == null || persona.getDocumentoList() == null || persona.getDocumentoList().isEmpty()) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        for (Documento documento : persona.getDocumentoList()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(documento.getIdTipoDocumento() == null ? "Documento"
                    : texto(documento.getIdTipoDocumento().getNombre()));
            sb.append(": ").append(texto(documento.getValor()));
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private static String fecha(Date fecha) {
        if (fecha == null) {
            return "-";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fecha);
    }

    private static String texto(String texto) {
        return texto == null ? "" : texto;
    }
}
