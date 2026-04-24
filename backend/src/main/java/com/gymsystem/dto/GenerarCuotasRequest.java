package com.gymsystem.dto;
import java.time.LocalDate;
public class GenerarCuotasRequest {
    private Integer mes, anio;
    private LocalDate fechaVencimiento;
    public Integer getMes() { return mes; } public void setMes(Integer m) { this.mes = m; }
    public Integer getAnio() { return anio; } public void setAnio(Integer a) { this.anio = a; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; } public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
}
