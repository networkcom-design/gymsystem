package com.gymsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "progreso_alumnos")
public class ProgresoAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alumno_id", nullable = false)
    @JsonIgnoreProperties({"cuotas", "disciplinas"})
    private Alumno alumno;

    @Column(nullable = false)
    private LocalDate fecha;

    // Medidas basicas
    @Column(precision = 6, scale = 2)
    private BigDecimal peso;          // kg

    @Column(precision = 5, scale = 1)
    private BigDecimal altura;        // cm (se reutiliza de la ultima entrada)

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeGrasa; // %

    // Medidas corporales (cm)
    @Column(precision = 5, scale = 1)
    private BigDecimal cintura;

    @Column(precision = 5, scale = 1)
    private BigDecimal pecho;

    @Column(precision = 5, scale = 1)
    private BigDecimal brazos;        // promedio

    @Column(precision = 5, scale = 1)
    private BigDecimal piernas;       // promedio

    @Column(precision = 5, scale = 1)
    private BigDecimal gluteos;

    @Column(length = 500)
    private String notas;

    public ProgresoAlumno() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno a) { this.alumno = a; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate f) { this.fecha = f; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal p) { this.peso = p; }
    public BigDecimal getAltura() { return altura; }
    public void setAltura(BigDecimal a) { this.altura = a; }
    public BigDecimal getPorcentajeGrasa() { return porcentajeGrasa; }
    public void setPorcentajeGrasa(BigDecimal p) { this.porcentajeGrasa = p; }
    public BigDecimal getCintura() { return cintura; }
    public void setCintura(BigDecimal c) { this.cintura = c; }
    public BigDecimal getPecho() { return pecho; }
    public void setPecho(BigDecimal p) { this.pecho = p; }
    public BigDecimal getBrazos() { return brazos; }
    public void setBrazos(BigDecimal b) { this.brazos = b; }
    public BigDecimal getPiernas() { return piernas; }
    public void setPiernas(BigDecimal p) { this.piernas = p; }
    public BigDecimal getGluteos() { return gluteos; }
    public void setGluteos(BigDecimal g) { this.gluteos = g; }
    public String getNotas() { return notas; }
    public void setNotas(String n) { this.notas = n; }
}
