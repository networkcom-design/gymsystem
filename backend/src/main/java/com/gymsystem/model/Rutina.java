package com.gymsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutinas")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRutina tipo = TipoRutina.PLANTILLA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRutina nivel = NivelRutina.PRINCIPIANTE;

    @Enumerated(EnumType.STRING)
    private ObjetivoRutina objetivo;

    // "Dia A", "Dia B", "Full Body", "Lunes/Miercoles/Viernes", etc.
    @Column(length = 80)
    private String dia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    // Solo se completa cuando tipo = PERSONALIZADA
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alumno_id")
    @JsonIgnoreProperties({"cuotas", "disciplinas"})
    private Alumno alumno;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rutina_ejercicios", joinColumns = @JoinColumn(name = "rutina_id"))
    @OrderColumn(name = "orden_idx")
    private List<Ejercicio> ejercicios = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    private boolean activa = true;

    public Rutina() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public TipoRutina getTipo() { return tipo; }
    public void setTipo(TipoRutina t) { this.tipo = t; }
    public NivelRutina getNivel() { return nivel; }
    public void setNivel(NivelRutina n) { this.nivel = n; }
    public ObjetivoRutina getObjetivo() { return objetivo; }
    public void setObjetivo(ObjetivoRutina o) { this.objetivo = o; }
    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }
    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina d) { this.disciplina = d; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno a) { this.alumno = a; }
    public List<Ejercicio> getEjercicios() { return ejercicios; }
    public void setEjercicios(List<Ejercicio> e) { this.ejercicios = e; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate f) { this.fechaCreacion = f; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
