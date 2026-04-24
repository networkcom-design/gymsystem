package com.gymsystem.controller;

import com.gymsystem.model.EstadoCuota;
import com.gymsystem.model.TipoRutina;
import com.gymsystem.repository.AlumnoRepository;
import com.gymsystem.repository.CuotaRepository;
import com.gymsystem.repository.DisciplinaRepository;
import com.gymsystem.repository.RutinaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AlumnoRepository alumnoRepo;
    private final DisciplinaRepository disciplinaRepo;
    private final CuotaRepository cuotaRepo;
    private final RutinaRepository rutinaRepo;

    public DashboardController(AlumnoRepository alumnoRepo, DisciplinaRepository disciplinaRepo,
                               CuotaRepository cuotaRepo, RutinaRepository rutinaRepo) {
        this.alumnoRepo = alumnoRepo;
        this.disciplinaRepo = disciplinaRepo;
        this.cuotaRepo = cuotaRepo;
        this.rutinaRepo = rutinaRepo;
    }

    @GetMapping
    public Map<String, Object> resumen() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();

        Map<String, Object> r = new HashMap<>();
        r.put("alumnosActivos", alumnoRepo.countByActivoTrue());
        r.put("alumnosTotales", alumnoRepo.count());
        r.put("disciplinas", disciplinaRepo.count());

        BigDecimal ingresos = cuotaRepo.sumIngresosDelMes(mes, anio);
        r.put("ingresosDelMes", ingresos != null ? ingresos : BigDecimal.ZERO);

        r.put("cuotasPendientesMes", cuotaRepo.countByEstadoAndMesAndAnio(EstadoCuota.PENDIENTE, mes, anio));
        r.put("cuotasPagadasMes", cuotaRepo.countByEstadoAndMesAndAnio(EstadoCuota.PAGADA, mes, anio));
        r.put("cuotasVencidasTotal", cuotaRepo.countByEstado(EstadoCuota.VENCIDA));

        r.put("rutinasPlantillas", rutinaRepo.countByTipo(TipoRutina.PLANTILLA));
        r.put("rutinasPersonalizadas", rutinaRepo.countByTipo(TipoRutina.PERSONALIZADA));

        r.put("mes", mes);
        r.put("anio", anio);
        return r;
    }
}
