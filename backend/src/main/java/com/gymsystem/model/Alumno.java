package com.gymsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true)
    private String dni;

    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private LocalDate fechaAlta;
    private String contactoEmergencia;
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "alumno_disciplina",
        joinColumns = @JoinColumn(name = "alumno_id"),
        inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
    private Set<Disciplina> disciplinas = new HashSet<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Cuota> cuotas = new HashSet<>();

    public Alumno() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getApellido() { return apellido; }
    public void setApellido(String a) { this.apellido = a; }
    public String getDni() { return dni; }
    public void setDni(String d) { this.dni = d; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String t) { this.telefono = t; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate f) { this.fechaNacimiento = f; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate f) { this.fechaAlta = f; }
    public String getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(String c) { this.contactoEmergencia = c; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Set<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<Disciplina> d) { this.disciplinas = d; }
    public Set<Cuota> getCuotas() { return cuotas; }
    public void setCuotas(Set<Cuota> c) { this.cuotas = c; }
}
