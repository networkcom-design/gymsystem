package com.gymsystem.repository;
import com.gymsystem.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    List<Alumno> findByActivoTrue();
    long countByActivoTrue();
}
