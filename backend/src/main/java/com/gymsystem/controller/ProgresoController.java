package com.gymsystem.controller;

import com.gymsystem.model.Alumno;
import com.gymsystem.model.ProgresoAlumno;
import com.gymsystem.repository.AlumnoRepository;
import com.gymsystem.repository.ProgresoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progreso")
public class ProgresoController {

    private final ProgresoRepository progresoRepo;
    private final AlumnoRepository alumnoRepo;

    public ProgresoController(ProgresoRepository progresoRepo, AlumnoRepository alumnoRepo) {
        this.progresoRepo = progresoRepo;
        this.alumnoRepo = alumnoRepo;
    }

    @GetMapping("/alumno/{alumnoId}")
    public ResponseEntity<List<ProgresoAlumno>> byAlumno(@PathVariable Long alumnoId) {
        if (!alumnoRepo.existsById(alumnoId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(progresoRepo.findByAlumnoIdOrderByFechaDesc(alumnoId));
    }

    @PostMapping("/alumno/{alumnoId}")
    public ResponseEntity<ProgresoAlumno> create(@PathVariable Long alumnoId,
                                                  @RequestBody ProgresoAlumno body) {
        return alumnoRepo.findById(alumnoId).map(a -> {
            body.setId(null);
            body.setAlumno(a);
            return ResponseEntity.ok(progresoRepo.save(body));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgresoAlumno> update(@PathVariable Long id,
                                                  @RequestBody ProgresoAlumno body) {
        return progresoRepo.findById(id).map(p -> {
            p.setFecha(body.getFecha());
            p.setPeso(body.getPeso());
            p.setAltura(body.getAltura());
            p.setPorcentajeGrasa(body.getPorcentajeGrasa());
            p.setCintura(body.getCintura());
            p.setPecho(body.getPecho());
            p.setBrazos(body.getBrazos());
            p.setPiernas(body.getPiernas());
            p.setGluteos(body.getGluteos());
            p.setNotas(body.getNotas());
            return ResponseEntity.ok(progresoRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!progresoRepo.existsById(id)) return ResponseEntity.notFound().build();
        progresoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
