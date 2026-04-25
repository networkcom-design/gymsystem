package com.gymsystem.controller;

import com.gymsystem.dto.GenerarCuotasRequest;
import com.gymsystem.dto.PagoRequest;
import com.gymsystem.model.*;
import com.gymsystem.repository.AlumnoRepository;
import com.gymsystem.repository.CuotaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {

    private final CuotaRepository cuotaRepo;
    private final AlumnoRepository alumnoRepo;

    public CuotaController(CuotaRepository cuotaRepo, AlumnoRepository alumnoRepo) {
        this.cuotaRepo = cuotaRepo;
        this.alumnoRepo = alumnoRepo;
    }

    @GetMapping
    public List<Cuota> list(@RequestParam(required = false) Integer mes,
                            @RequestParam(required = false) Integer anio,
                            @RequestParam(required = false) EstadoCuota estado,
                            @RequestParam(required = false) String texto) {
        actualizarVencidas();
        String t = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        return cuotaRepo.findFiltered(mes, anio, estado, t);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Integer mes,
                                              @RequestParam(required = false) Integer anio,
                                              @RequestParam(required = false) EstadoCuota estado,
                                              @RequestParam(required = false) String texto) throws IOException {
        actualizarVencidas();
        String t = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        List<Cuota> cuotas = cuotaRepo.findFiltered(mes, anio, estado, t);

        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                          "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Cuotas");

            CellStyle hStyle = wb.createCellStyle();
            Font hFont = wb.createFont(); hFont.setBold(true); hFont.setColor(IndexedColors.WHITE.getIndex());
            hStyle.setFont(hFont); hStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hStyle.setAlignment(HorizontalAlignment.CENTER); hStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle moneyStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            moneyStyle.setDataFormat(fmt.getFormat("\"$\" #,##0.00"));

            CellStyle totalStyle = wb.createCellStyle();
            Font tf = wb.createFont(); tf.setBold(true); totalStyle.setFont(tf);
            totalStyle.setDataFormat(fmt.getFormat("\"$\" #,##0.00")); totalStyle.setBorderTop(BorderStyle.THIN);

            CellStyle tlStyle = wb.createCellStyle(); tlStyle.setFont(tf);
            tlStyle.setAlignment(HorizontalAlignment.RIGHT); tlStyle.setBorderTop(BorderStyle.THIN);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            String periodo = (mes != null ? meses[mes - 1] + " " : "") + (anio != null ? anio : "Todos los periodos");
            titleCell.setCellValue("Gym System - Cuotas " + periodo);
            CellStyle tsStyle = wb.createCellStyle(); Font tsFont = wb.createFont();
            tsFont.setBold(true); tsFont.setFontHeightInPoints((short) 14); tsStyle.setFont(tsFont);
            titleCell.setCellStyle(tsStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            String[] headers = {"Apellido","Nombre","DNI","Periodo","Disciplinas","Monto","Vencimiento","Estado","Fecha de pago","Metodo de pago"};
            Row hr = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) { Cell c = hr.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(hStyle); }

            int rowIdx = 3;
            BigDecimal totalMonto = BigDecimal.ZERO, totalPagado = BigDecimal.ZERO;
            int cantPagadas = 0, cantPendientes = 0, cantVencidas = 0;

            for (Cuota c : cuotas) {
                Row row = sheet.createRow(rowIdx++);
                Alumno a = c.getAlumno();
                row.createCell(0).setCellValue(a != null ? a.getApellido() : "");
                row.createCell(1).setCellValue(a != null ? a.getNombre() : "");
                row.createCell(2).setCellValue(a != null ? a.getDni() : "");
                row.createCell(3).setCellValue(meses[c.getMes() - 1] + " " + c.getAnio());
                String discs = c.getDisciplinas() != null && !c.getDisciplinas().isEmpty()
                        ? c.getDisciplinas().stream().map(Disciplina::getNombre).sorted().collect(Collectors.joining(", ")) : "";
                row.createCell(4).setCellValue(discs);
                Cell mc = row.createCell(5); mc.setCellValue(c.getMonto() != null ? c.getMonto().doubleValue() : 0); mc.setCellStyle(moneyStyle);
                row.createCell(6).setCellValue(c.getFechaVencimiento() != null ? c.getFechaVencimiento().format(df) : "");
                row.createCell(7).setCellValue(c.getEstado() != null ? c.getEstado().name() : "");
                row.createCell(8).setCellValue(c.getFechaPago() != null ? c.getFechaPago().format(df) : "");
                row.createCell(9).setCellValue(c.getMetodoPago() != null ? c.getMetodoPago().name() : "");

                if (c.getMonto() != null) totalMonto = totalMonto.add(c.getMonto());
                if (c.getEstado() == EstadoCuota.PAGADA) { cantPagadas++; if (c.getMonto() != null) totalPagado = totalPagado.add(c.getMonto()); }
                else if (c.getEstado() == EstadoCuota.PENDIENTE) cantPendientes++;
                else if (c.getEstado() == EstadoCuota.VENCIDA) cantVencidas++;
            }

            rowIdx++;
            Row tr = sheet.createRow(rowIdx++); Cell tl = tr.createCell(4); tl.setCellValue("TOTAL:"); tl.setCellStyle(tlStyle);
            Cell tv = tr.createCell(5); tv.setCellValue(totalMonto.doubleValue()); tv.setCellStyle(totalStyle);
            Row cr = sheet.createRow(rowIdx++); Cell cl = cr.createCell(4); cl.setCellValue("COBRADO:"); cl.setCellStyle(tlStyle);
            Cell cv = cr.createCell(5); cv.setCellValue(totalPagado.doubleValue()); cv.setCellStyle(totalStyle);
            rowIdx++;
            sheet.createRow(rowIdx++).createCell(0).setCellValue("Pagadas: " + cantPagadas);
            sheet.createRow(rowIdx++).createCell(0).setCellValue("Pendientes: " + cantPendientes);
            sheet.createRow(rowIdx++).createCell(0).setCellValue("Vencidas: " + cantVencidas);
            sheet.createRow(rowIdx).createCell(0).setCellValue("Total: " + cuotas.size());

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);

            String fileName = "cuotas" + (anio != null ? "-" + anio : "") + (mes != null ? "-" + String.format("%02d", mes) : "") + ".xlsx";
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            h.setContentDispositionFormData("attachment", fileName);
            return ResponseEntity.ok().headers(h).body(out.toByteArray());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuota> get(@PathVariable Long id) {
        return cuotaRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<Cuota> pagar(@PathVariable Long id, @RequestBody PagoRequest req) {
        return cuotaRepo.findById(id).map(c -> {
            c.setEstado(EstadoCuota.PAGADA);
            c.setFechaPago(req.getFechaPago() != null ? req.getFechaPago() : LocalDate.now());
            c.setMetodoPago(req.getMetodoPago());
            c.setObservaciones(req.getObservaciones());
            return ResponseEntity.ok(cuotaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/revertir-pago")
    public ResponseEntity<Cuota> revertir(@PathVariable Long id) {
        return cuotaRepo.findById(id).map(c -> {
            c.setFechaPago(null); c.setMetodoPago(null);
            c.setEstado(c.getFechaVencimiento().isBefore(LocalDate.now()) ? EstadoCuota.VENCIDA : EstadoCuota.PENDIENTE);
            return ResponseEntity.ok(cuotaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping({"/generar-mes", "/generar"})
    public ResponseEntity<?> generarMes(@RequestBody GenerarCuotasRequest req) {
        if (req.getMes() == null || req.getAnio() == null)
            return ResponseEntity.badRequest().body(Map.of("error", "mes y anio son requeridos"));

        LocalDate venc = req.getFechaVencimiento() != null
                ? req.getFechaVencimiento()
                : LocalDate.of(req.getAnio(), req.getMes(), 10);

        List<Alumno> activos = alumnoRepo.findByActivoTrue();
        List<Cuota> existentes = cuotaRepo.findByMesAndAnio(req.getMes(), req.getAnio());
        List<Cuota> creadas = new ArrayList<>();

        for (Alumno a : activos) {
            if (existentes.stream().anyMatch(c -> c.getAlumno().getId().equals(a.getId()))) continue;
            BigDecimal monto = a.getDisciplinas().stream().map(Disciplina::getPrecioMensual)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (monto.compareTo(BigDecimal.ZERO) == 0) continue;
            Cuota c = new Cuota();
            c.setAlumno(a); c.setMes(req.getMes()); c.setAnio(req.getAnio());
            c.setMonto(monto); c.setFechaVencimiento(venc); c.setEstado(EstadoCuota.PENDIENTE);
            c.setDisciplinas(new HashSet<>(a.getDisciplinas()));
            creadas.add(cuotaRepo.save(c));
        }
        return ResponseEntity.ok(Map.of("creadas", creadas.size(), "existentes", existentes.size(), "alumnosActivos", activos.size()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!cuotaRepo.existsById(id)) return ResponseEntity.notFound().build();
        cuotaRepo.deleteById(id); return ResponseEntity.noContent().build();
    }

    private void actualizarVencidas() {
        LocalDate hoy = LocalDate.now();
        cuotaRepo.findByEstado(EstadoCuota.PENDIENTE).forEach(c -> {
            if (c.getFechaVencimiento() != null && c.getFechaVencimiento().isBefore(hoy)) {
                c.setEstado(EstadoCuota.VENCIDA); cuotaRepo.save(c);
            }
        });
    }
}
