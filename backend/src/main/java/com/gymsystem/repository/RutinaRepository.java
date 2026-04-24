package com.gymsystem.repository;

import com.gymsystem.model.NivelRutina;
import com.gymsystem.model.ObjetivoRutina;
import com.gymsystem.model.Rutina;
import com.gymsystem.model.TipoRutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {

    List<Rutina> findByTipoOrderByNombreAsc(TipoRutina tipo);

    List<Rutina> findByAlumnoIdOrderByFechaCreacionDesc(Long alumnoId);

    @Query("""
        SELECT r FROM Rutina r
        WHERE (:tipo IS NULL OR r.tipo = :tipo)
          AND (:nivel IS NULL OR r.nivel = :nivel)
          AND (:objetivo IS NULL OR r.objetivo = :objetivo)
          AND (:disciplinaId IS NULL OR r.disciplina.id = :disciplinaId)
          AND (:alumnoId IS NULL OR r.alumno.id = :alumnoId)
          AND (:soloActivas = FALSE OR r.activa = TRUE)
          AND (:texto IS NULL OR :texto = ''
               OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(r.descripcion, '')) LIKE LOWER(CONCAT('%', :texto, '%')))
        ORDER BY r.fechaCreacion DESC, r.nombre ASC
    """)
    List<Rutina> findFiltered(@Param("tipo") TipoRutina tipo,
                              @Param("nivel") NivelRutina nivel,
                              @Param("objetivo") ObjetivoRutina objetivo,
                              @Param("disciplinaId") Long disciplinaId,
                              @Param("alumnoId") Long alumnoId,
                              @Param("soloActivas") boolean soloActivas,
                              @Param("texto") String texto);

    long countByTipo(TipoRutina tipo);
}
