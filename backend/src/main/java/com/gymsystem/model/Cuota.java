package com.gymsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cuotas")
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alumno_id", nullable = false)
    @JsonIgnoreProperties({"cuotas", "disciplinas"})
    private Alumno alumno;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Column(length = 500)
    private String observaciones;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "cuota_disciplina",
        joinColumns = @JoinColumn(name = "cuota_id"),
        inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
    private Set<Disciplina> disciplinas = new HashSet<>();

    public Cuota() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno a) { this.alumno = a; }
    public Integer getMes() { return mes; }
    public void setMes(Integer m) { this.mes = m; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer a) { this.anio = a; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal m) { this.monto = m; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate f) { this.fechaPago = f; }
    public EstadoCuota getEstado() { return estado; }
    public void setEstado(EstadoCuota e) { this.estado = e; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago m) { this.metodoPago = m; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String o) { this.observaciones = o; }
    public Set<Disciplina> getDisciplinas() { return disciplinas; }
    public void setDisciplinas(Set<Disciplina> d) { this.disciplinas = d; }
}
