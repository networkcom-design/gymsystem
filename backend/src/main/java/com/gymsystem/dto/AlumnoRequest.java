package com.gymsystem.dto;
import java.time.LocalDate;
import java.util.Set;
public class AlumnoRequest {
    private String nombre, apellido, dni, email, telefono, contactoEmergencia;
    private LocalDate fechaNacimiento, fechaAlta;
    private Boolean activo;
    private Set<Long> disciplinaIds;

    public String getNombre() { return nombre; } public void setNombre(String n) { this.nombre = n; }
    public String getApellido() { return apellido; } public void setApellido(String a) { this.apellido = a; }
    public String getDni() { return dni; } public void setDni(String d) { this.dni = d; }
    public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
    public String getTelefono() { return telefono; } public void setTelefono(String t) { this.telefono = t; }
    public String getContactoEmergencia() { return contactoEmergencia; } public void setContactoEmergencia(String c) { this.contactoEmergencia = c; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; } public void setFechaNacimiento(LocalDate f) { this.fechaNacimiento = f; }
    public LocalDate getFechaAlta() { return fechaAlta; } public void setFechaAlta(LocalDate f) { this.fechaAlta = f; }
    public Boolean getActivo() { return activo; } public void setActivo(Boolean a) { this.activo = a; }
    public Set<Long> getDisciplinaIds() { return disciplinaIds; } public void setDisciplinaIds(Set<Long> d) { this.disciplinaIds = d; }
}
