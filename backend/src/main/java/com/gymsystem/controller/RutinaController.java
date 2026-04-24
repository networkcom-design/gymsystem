package com.gymsystem.controller;

import com.gymsystem.model.*;
import com.gymsystem.repository.AlumnoRepository;
import com.gymsystem.repository.DisciplinaRepository;
import com.gymsystem.repository.RutinaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
public class RutinaController {

    private final RutinaRepository rutinaRepo;
    private final AlumnoRepository alumnoRepo;
    private final DisciplinaRepository disciplinaRepo;

    public RutinaController(RutinaRepository rutinaRepo, AlumnoRepository alumnoRepo, DisciplinaRepository disciplinaRepo) {
        this.rutinaRepo = rutinaRepo;
        this.alumnoRepo = alumnoRepo;
        this.disciplinaRepo = disciplinaRepo;
    }

    @GetMapping
    public List<Rutina> list(@RequestParam(required = false) TipoRutina tipo,
                             @RequestParam(required = false) NivelRutina nivel,
                             @RequestParam(required = false) ObjetivoRutina objetivo,
                             @RequestParam(required = false) Long disciplinaId,
                             @RequestParam(required = false) Long alumnoId,
                             @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
                             @RequestParam(required = false) String texto) {
        String t = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        return rutinaRepo.findFiltered(tipo, nivel, objetivo, disciplinaId, alumnoId, soloActivas, t);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rutina> get(@PathVariable Long id) {
        return rutinaRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Rutina create(@RequestBody Rutina rutina) {
        rutina.setId(null);
        resolverReferencias(rutina);
        return rutinaRepo.save(rutina);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rutina> update(@PathVariable Long id, @RequestBody Rutina body) {
        return rutinaRepo.findById(id).map(existing -> {
            existing.setNombre(body.getNombre());
            existing.setDescripcion(body.getDescripcion());
            existing.setTipo(body.getTipo() != null ? body.getTipo() : TipoRutina.PLANTILLA);
            existing.setNivel(body.getNivel() != null ? body.getNivel() : NivelRutina.PRINCIPIANTE);
            existing.setObjetivo(body.getObjetivo());
            existing.setDia(body.getDia());
            existing.setActiva(body.isActiva());
            existing.setEjercicios(body.getEjercicios());

            if (body.getDisciplina() != null && body.getDisciplina().getId() != null)
                existing.setDisciplina(disciplinaRepo.findById(body.getDisciplina().getId()).orElse(null));
            else existing.setDisciplina(null);

            if (body.getAlumno() != null && body.getAlumno().getId() != null)
                existing.setAlumno(alumnoRepo.findById(body.getAlumno().getId()).orElse(null));
            else existing.setAlumno(null);

            return ResponseEntity.ok(rutinaRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!rutinaRepo.existsById(id)) return ResponseEntity.notFound().build();
        rutinaRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Duplica la rutina (para crear variantes) */
    @PostMapping("/{id}/duplicar")
    public ResponseEntity<Rutina> duplicar(@PathVariable Long id) {
        return rutinaRepo.findById(id).map(src -> {
            Rutina copia = new Rutina();
            copia.setNombre(src.getNombre() + " (copia)");
            copia.setDescripcion(src.getDescripcion());
            copia.setTipo(src.getTipo());
            copia.setNivel(src.getNivel());
            copia.setObjetivo(src.getObjetivo());
            copia.setDia(src.getDia());
            copia.setDisciplina(src.getDisciplina());
            copia.setAlumno(src.getAlumno());
            copia.setEjercicios(new ArrayList<>(src.getEjercicios()));
            copia.setActiva(true);
            return ResponseEntity.ok(rutinaRepo.save(copia));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Asigna una plantilla a un cliente: crea copia PERSONALIZADA para ese alumno */
    @PostMapping("/{id}/asignar-alumno")
    public ResponseEntity<Rutina> asignarAlumno(@PathVariable Long id,
                                                 @RequestParam Long alumnoId) {
        return rutinaRepo.findById(id).map(src ->
            alumnoRepo.findById(alumnoId).map(alumno -> {
                Rutina personalizada = new Rutina();
                personalizada.setNombre(src.getNombre());
                personalizada.setDescripcion(src.getDescripcion());
                personalizada.setTipo(TipoRutina.PERSONALIZADA);
                personalizada.setNivel(src.getNivel());
                personalizada.setObjetivo(src.getObjetivo());
                personalizada.setDia(src.getDia());
                personalizada.setDisciplina(src.getDisciplina());
                personalizada.setAlumno(alumno);
                personalizada.setEjercicios(new ArrayList<>(src.getEjercicios()));
                personalizada.setActiva(true);
                return ResponseEntity.ok(rutinaRepo.save(personalizada));
            }).orElse(ResponseEntity.notFound().build())
        ).orElse(ResponseEntity.notFound().build());
    }

    private void resolverReferencias(Rutina r) {
        Disciplina d = r.getDisciplina();
        r.setDisciplina(d != null && d.getId() != null ? disciplinaRepo.findById(d.getId()).orElse(null) : null);
        Alumno a = r.getAlumno();
        r.setAlumno(a != null && a.getId() != null ? alumnoRepo.findById(a.getId()).orElse(null) : null);
    }
}
