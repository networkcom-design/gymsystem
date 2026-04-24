package com.gymsystem.repository;

import com.gymsystem.model.ProgresoAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgresoRepository extends JpaRepository<ProgresoAlumno, Long> {
    List<ProgresoAlumno> findByAlumnoIdOrderByFechaDesc(Long alumnoId);
}
