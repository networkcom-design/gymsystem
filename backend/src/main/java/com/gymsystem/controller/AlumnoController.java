package com.gymsystem.controller;

import com.gymsystem.dto.AlumnoRequest;
import com.gymsystem.model.Alumno;
import com.gymsystem.model.Cuota;
import com.gymsystem.model.Disciplina;
import com.gymsystem.model.EstadoCuota;
import com.gymsystem.repository.AlumnoRepository;
import com.gymsystem.repository.CuotaRepository;
import com.gymsystem.repository.DisciplinaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    private final AlumnoRepository alumnoRepo;
    private final DisciplinaRepository disciplinaRepo;
    private final CuotaRepository cuotaRepo;


    public AlumnoController(AlumnoRepository alumnoRepo, DisciplinaRepository disciplinaRepo, CuotaRepository cuotaRepo) {
        this.alumnoRepo = alumnoRepo;
        this.disciplinaRepo = disciplinaRepo;
        this.cuotaRepo = cuotaRepo;
    }

    @GetMapping
    public List<Alumno> list() { return alumnoRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Alumno> get(@PathVariable Long id) {
        return alumnoRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/cuotas")
    public ResponseEntity<List<Cuota>> cuotas(@PathVariable Long id) {
        if (!alumnoRepo.existsById(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cuotaRepo.findByAlumnoIdOrderByAnioDescMesDesc(id));
    }

    @PostMapping
    public Alumno create(@RequestBody AlumnoRequest req) {
        Alumno a = new Alumno();
        apply(a, req);
        if (a.getFechaAlta() == null) a.setFechaAlta(LocalDate.now());
        return alumnoRepo.save(a);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alumno> update(@PathVariable Long id, @RequestBody AlumnoRequest req) {
        return alumnoRepo.findById(id).map(a -> { apply(a, req); return ResponseEntity.ok(alumnoRepo.save(a)); })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!alumnoRepo.existsById(id)) return ResponseEntity.notFound().build();
        alumnoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void apply(Alumno a, AlumnoRequest req) {
        if (req.getNombre() != null) a.setNombre(req.getNombre());
        if (req.getApellido() != null) a.setApellido(req.getApellido());
        if (req.getDni() != null) a.setDni(req.getDni());
        if (req.getEmail() != null) a.setEmail(req.getEmail());
        if (req.getTelefono() != null) a.setTelefono(req.getTelefono());
        if (req.getFechaNacimiento() != null) a.setFechaNacimiento(req.getFechaNacimiento());
        if (req.getFechaAlta() != null) a.setFechaAlta(req.getFechaAlta());
        if (req.getContactoEmergencia() != null) a.setContactoEmergencia(req.getContactoEmergencia());
        if (req.getActivo() != null) a.setActivo(req.getActivo());
        if (req.getDisciplinaIds() != null) {
            Set<Disciplina> set = new HashSet<>(disciplinaRepo.findAllById(req.getDisciplinaIds()));
            a.setDisciplinas(set);
        }
    }
    @GetMapping("/por-dni/{dni}")
    public ResponseEntity<?> buscarPorDni(@PathVariable String dni) {
        return alumnoRepo.findByDni(dni)
                .map(alumno -> {
                    List<Cuota> cuotas = cuotaRepo.findByAlumnoIdOrderByAnioDescMesDesc(alumno.getId());

                    Optional<Cuota> ultimaPagada = cuotas.stream()
                            .filter(c -> c.getEstado() == EstadoCuota.PAGADA)
                            .max(Comparator.comparingInt((Cuota c) -> c.getAnio() * 100 + c.getMes()));

                    boolean tieneVencidasSinPagar = cuotas.stream()
                            .anyMatch(c -> c.getEstado() == EstadoCuota.VENCIDA);

                    LocalDate hoy = LocalDate.now();
                    String estadoCuota;
                    String fechaVencimiento = null;

                    if (ultimaPagada.isEmpty()) {
                        estadoCuota = tieneVencidasSinPagar ? "VENCIDA" : "SIN_CUOTA";
                    } else {
                        Cuota ultima = ultimaPagada.get();
                        fechaVencimiento = ultima.getFechaVencimiento() != null
                                ? ultima.getFechaVencimiento().toString() : null;

                        int mesActual = hoy.getYear() * 100 + hoy.getMonthValue();
                        int mesUltima = ultima.getAnio() * 100 + ultima.getMes();

                        boolean tieneVencidasPosteriores = cuotas.stream()
                                .filter(c -> c.getEstado() == EstadoCuota.VENCIDA)
                                .anyMatch(c -> (c.getAnio() * 100 + c.getMes()) > mesUltima);

                        if (tieneVencidasPosteriores) {
                            estadoCuota = "VENCIDA";
                        } else if (mesUltima >= mesActual) {
                            estadoCuota = "AL_DIA";
                        } else if (mesUltima == mesActual - 1) {
                            estadoCuota = "POR_VENCER";
                        } else {
                            estadoCuota = "VENCIDA";
                        }
                    }

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("id", alumno.getId());
                    resp.put("nombre", alumno.getNombre());
                    resp.put("apellido", alumno.getApellido());
                    resp.put("dni", alumno.getDni());
                    resp.put("activo", alumno.isActivo());
                    resp.put("estadoCuota", estadoCuota);
                    resp.put("fechaVencimientoCuota", fechaVencimiento);

                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
