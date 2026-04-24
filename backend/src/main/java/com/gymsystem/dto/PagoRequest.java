package com.gymsystem.dto;
import com.gymsystem.model.MetodoPago;
import java.time.LocalDate;
public class PagoRequest {
    private LocalDate fechaPago;
    private MetodoPago metodoPago;
    private String observaciones;
    public LocalDate getFechaPago() { return fechaPago; } public void setFechaPago(LocalDate f) { this.fechaPago = f; }
    public MetodoPago getMetodoPago() { return metodoPago; } public void setMetodoPago(MetodoPago m) { this.metodoPago = m; }
    public String getObservaciones() { return observaciones; } public void setObservaciones(String o) { this.observaciones = o; }
}
