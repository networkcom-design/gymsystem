package com.gymsystem.service;

import com.gymsystem.model.Ejercicio;
import com.gymsystem.model.Rutina;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@Service
public class RutinaPdfService {
    public byte[] generarPdf(Rutina rutina) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);

        doc.add(new Paragraph(rutina.getNombre())
                .setFontSize(22).setBold()
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(8));

        doc.add(new Paragraph("Nivel: " + rutina.getNivel() + "  |  Objetivo: " + rutina.getObjetivo())
                .setFontSize(11).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(120,120,120)).setMarginBottom(4));

        if (rutina.getDescripcion() != null && !rutina.getDescripcion().isBlank()) {
            doc.add(new Paragraph(rutina.getDescripcion())
                    .setFontSize(11).setMarginBottom(16));
        }

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{3,1,1,1,1,2}))
                .useAllAvailableWidth();

        for (String h : List.of("Ejercicio","Series","Reps","Descanso","Peso","Notas")) {
            tabla.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setBold().setFontSize(10))
                    .setBackgroundColor(new DeviceRgb(30,30,30))
                    .setFontColor(ColorConstants.WHITE).setPadding(6));
        }

        for (Ejercicio e : rutina.getEjercicios()) {
            tabla.addCell(new Cell().add(new Paragraph(e.getNombre()).setFontSize(10)).setPadding(5));
            tabla.addCell(new Cell().add(new Paragraph(String.valueOf(e.getSeries())).setFontSize(10)).setPadding(5));
            tabla.addCell(new Cell().add(new Paragraph(e.getRepeticiones()).setFontSize(10)).setPadding(5));
            tabla.addCell(new Cell().add(new Paragraph(e.getDescanso() != null ? e.getDescanso() : "")).setFontSize(10).setPadding(5));
            tabla.addCell(new Cell().add(new Paragraph(e.getPeso() != null ? e.getPeso() : "")).setFontSize(10).setPadding(5));
            tabla.addCell(new Cell().add(new Paragraph(e.getNotas() != null ? e.getNotas() : "")).setFontSize(10).setPadding(5));
        }

        doc.add(tabla);
        doc.close();
        return baos.toByteArray();
    }
}
