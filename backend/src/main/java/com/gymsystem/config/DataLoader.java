package com.gymsystem.config;

import com.gymsystem.model.*;
import com.gymsystem.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final DisciplinaRepository disciplinaRepo;
    private final AlumnoRepository alumnoRepo;
    private final CuotaRepository cuotaRepo;
    private final RutinaRepository rutinaRepo;
    private final ProgresoRepository progresoRepo;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepo, DisciplinaRepository disciplinaRepo,
                      AlumnoRepository alumnoRepo, CuotaRepository cuotaRepo,
                      RutinaRepository rutinaRepo, ProgresoRepository progresoRepo,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.disciplinaRepo = disciplinaRepo;
        this.alumnoRepo = alumnoRepo;
        this.cuotaRepo = cuotaRepo;
        this.rutinaRepo = rutinaRepo;
        this.progresoRepo = progresoRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepo.count() == 0) {
            usuarioRepo.save(new Usuario("admin", passwordEncoder.encode("admin123"), "Administrador Gym System"));
        }

        if (disciplinaRepo.count() == 0) {
            Disciplina musc  = disciplinaRepo.save(new Disciplina("Musculacion",  new BigDecimal("18000.00"), "Entrenamiento con pesas y maquinas"));
            Disciplina func  = disciplinaRepo.save(new Disciplina("Funcional",    new BigDecimal("14000.00"), "Entrenamiento funcional y HIIT"));
            Disciplina cross = disciplinaRepo.save(new Disciplina("CrossFit",     new BigDecimal("20000.00"), "Entrenamiento de alta intensidad variado"));
            Disciplina yoga  = disciplinaRepo.save(new Disciplina("Yoga",         new BigDecimal("12000.00"), "Flexibilidad, equilibrio y meditacion"));
            Disciplina spin  = disciplinaRepo.save(new Disciplina("Spinning",     new BigDecimal("13000.00"), "Ciclismo indoor de alta intensidad"));
            Disciplina pilat = disciplinaRepo.save(new Disciplina("Pilates",      new BigDecimal("14000.00"), "Fortalecimiento del core y postura"));
            Disciplina zumba = disciplinaRepo.save(new Disciplina("Zumba",        new BigDecimal("11000.00"), "Baile fitness latino"));
            Disciplina kick  = disciplinaRepo.save(new Disciplina("Kickboxing",   new BigDecimal("16000.00"), "Artes marciales y cardio"));

            if (alumnoRepo.count() == 0) {
                Alumno a1 = crearAlumno("Carlos",     "Gomez",     "32111222", "carlos.g@mail.com",   "3624111001", LocalDate.of(1988, 3, 15), Set.of(musc, func));
                Alumno a2 = crearAlumno("Laura",      "Martinez",  "37445566", "laura.m@mail.com",    "3624111002", LocalDate.of(1995, 7, 22), Set.of(yoga, pilat));
                Alumno a3 = crearAlumno("Diego",      "Sanchez",   "40778899", "diego.s@mail.com",    "3624111003", LocalDate.of(2001, 11, 5), Set.of(cross, musc));
                Alumno a4 = crearAlumno("Romina",     "Flores",    "43001122", "romi.f@mail.com",     "3624111004", LocalDate.of(2003, 1, 30), Set.of(spin, zumba));
                Alumno a5 = crearAlumno("Pablo",      "Rios",      "29334455", "pablo.r@mail.com",    "3624111005", LocalDate.of(1985, 9, 18), Set.of(musc));
                Alumno a6 = crearAlumno("Jimena",     "Castro",    "44667788", "jimena.c@mail.com",   "3624111006", LocalDate.of(2004, 5, 11), Set.of(func, kick));
                Alumno a7 = crearAlumno("Hernan",     "Vargas",    "35990011", "hernan.v@mail.com",   "3624111007", LocalDate.of(1992, 12, 3), Set.of(cross));
                Alumno a8 = crearAlumno("Agustina",   "Pereyra",   "41223345", "agus.p@mail.com",     "3624111008", LocalDate.of(2002, 8, 25), Set.of(pilat, yoga, spin));

                List<Alumno> alumnos = alumnoRepo.saveAll(List.of(a1, a2, a3, a4, a5, a6, a7, a8));

                LocalDate hoy = LocalDate.now();
                int mesActual = hoy.getMonthValue(), anioActual = hoy.getYear();
                int mesAnterior = mesActual == 1 ? 12 : mesActual - 1;
                int anioAnterior = mesActual == 1 ? anioActual - 1 : anioActual;

                for (Alumno a : alumnos) {
                    BigDecimal monto = a.getDisciplinas().stream().map(Disciplina::getPrecioMensual)
                            .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

                    Cuota prev = new Cuota();
                    prev.setAlumno(a); prev.setMes(mesAnterior); prev.setAnio(anioAnterior); prev.setMonto(monto);
                    prev.setFechaVencimiento(LocalDate.of(anioAnterior, mesAnterior, 10));
                    prev.setFechaPago(LocalDate.of(anioAnterior, mesAnterior, 7));
                    prev.setEstado(EstadoCuota.PAGADA); prev.setMetodoPago(MetodoPago.TRANSFERENCIA);
                    prev.setDisciplinas(new HashSet<>(a.getDisciplinas()));
                    cuotaRepo.save(prev);

                    Cuota curr = new Cuota();
                    curr.setAlumno(a); curr.setMes(mesActual); curr.setAnio(anioActual); curr.setMonto(monto);
                    curr.setFechaVencimiento(LocalDate.of(anioActual, mesActual, 10));
                    curr.setDisciplinas(new HashSet<>(a.getDisciplinas()));
                    if (a.getId() % 2 == 0) {
                        curr.setEstado(EstadoCuota.PAGADA);
                        curr.setFechaPago(LocalDate.of(anioActual, mesActual, 5));
                        curr.setMetodoPago(MetodoPago.EFECTIVO);
                    } else {
                        curr.setEstado(EstadoCuota.PENDIENTE);
                    }
                    cuotaRepo.save(curr);
                }

                // Rutinas plantilla
                if (rutinaRepo.count() == 0) {
                    // Hipertrofia - Dia A (Pecho/Hombros/Triceps)
                    Rutina rA = new Rutina();
                    rA.setNombre("Hipertrofia - Dia A: Empuje");
                    rA.setDescripcion("Pecho, hombros y triceps. Rango de repeticiones 8-12. Tempo 2-0-2.");
                    rA.setTipo(TipoRutina.PLANTILLA); rA.setNivel(NivelRutina.INTERMEDIO);
                    rA.setObjetivo(ObjetivoRutina.HIPERTROFIA); rA.setDia("Dia A");
                    rA.setDisciplina(musc);
                    rA.setEjercicios(new ArrayList<>(Arrays.asList(
                            ej(1,"Press banca plano",4,"8-10","2 min","75-80% 1RM",GrupoMuscular.PECHO),
                            ej(2,"Press inclinado mancuernas",3,"10-12","90s","Controlado en bajada",GrupoMuscular.PECHO),
                            ej(3,"Aperturas en polea",3,"12-15","60s","Foco en contraccion",GrupoMuscular.PECHO),
                            ej(4,"Press militar",4,"8-10","2 min","Sentado o de pie",GrupoMuscular.HOMBROS),
                            ej(5,"Elevaciones laterales",3,"12-15","60s","Codos ligeramente flexionados",GrupoMuscular.HOMBROS),
                            ej(6,"Extension triceps polea",3,"12-15","60s","Codos fijos",GrupoMuscular.TRICEPS)
                    )));
                    rutinaRepo.save(rA);

                    // Hipertrofia - Dia B (Espalda/Biceps)
                    Rutina rB = new Rutina();
                    rB.setNombre("Hipertrofia - Dia B: Traccion");
                    rB.setDescripcion("Espalda y biceps. Foco en la contraccion y recorrido completo.");
                    rB.setTipo(TipoRutina.PLANTILLA); rB.setNivel(NivelRutina.INTERMEDIO);
                    rB.setObjetivo(ObjetivoRutina.HIPERTROFIA); rB.setDia("Dia B");
                    rB.setDisciplina(musc);
                    rB.setEjercicios(new ArrayList<>(Arrays.asList(
                            ej(1,"Peso muerto",4,"5","3 min","Tecnica estricta",GrupoMuscular.ESPALDA),
                            ej(2,"Remo con barra",4,"8-10","2 min","Pecho apoyado en banco si es necesario",GrupoMuscular.ESPALDA),
                            ej(3,"Dominadas",4,"al fallo","2 min","Asistidas si es necesario",GrupoMuscular.ESPALDA),
                            ej(4,"Remo en polea baja",3,"12","90s","Retraccion escapular",GrupoMuscular.ESPALDA),
                            ej(5,"Curl con barra",3,"10-12","90s","Sin inercia",GrupoMuscular.BICEPS),
                            ej(6,"Curl martillo mancuernas",3,"12","60s","Cada lado",GrupoMuscular.BICEPS)
                    )));
                    rutinaRepo.save(rB);

                    // Hipertrofia - Dia C (Piernas)
                    Rutina rC = new Rutina();
                    rC.setNombre("Hipertrofia - Dia C: Piernas");
                    rC.setDescripcion("Cuadriceps, femoral y gluteos. Dia mas intenso de la semana.");
                    rC.setTipo(TipoRutina.PLANTILLA); rC.setNivel(NivelRutina.INTERMEDIO);
                    rC.setObjetivo(ObjetivoRutina.HIPERTROFIA); rC.setDia("Dia C");
                    rC.setDisciplina(musc);
                    rC.setEjercicios(new ArrayList<>(Arrays.asList(
                            ej(1,"Sentadilla con barra",5,"6-8","3 min","Profundidad completa",GrupoMuscular.PIERNAS),
                            ej(2,"Prensa de piernas",4,"10-12","2 min","Pies al ancho de caderas",GrupoMuscular.PIERNAS),
                            ej(3,"Extension de cuadriceps",3,"15","60s","Contraccion en el tope",GrupoMuscular.PIERNAS),
                            ej(4,"Curl femoral tumbado",3,"12-15","60s","Lento en la bajada",GrupoMuscular.PIERNAS),
                            ej(5,"Hip thrust",4,"10-12","90s","Gluteo en contraccion 2 seg",GrupoMuscular.GLUTEOS),
                            ej(6,"Gemelos de pie",4,"15-20","45s","Recorrido completo",GrupoMuscular.PIERNAS)
                    )));
                    rutinaRepo.save(rC);

                    // Funcional - Full Body principiante
                    Rutina rFull = new Rutina();
                    rFull.setNombre("Funcional Full Body - Principiante");
                    rFull.setDescripcion("Rutina de cuerpo completo ideal para iniciar. 3 dias por semana con descanso entre sesiones.");
                    rFull.setTipo(TipoRutina.PLANTILLA); rFull.setNivel(NivelRutina.PRINCIPIANTE);
                    rFull.setObjetivo(ObjetivoRutina.ACONDICIONAMIENTO); rFull.setDia("Full Body");
                    rFull.setDisciplina(func);
                    rFull.setEjercicios(new ArrayList<>(Arrays.asList(
                            ej(1,"Sentadilla libre",3,"15","60s","Peso corporal",GrupoMuscular.PIERNAS),
                            ej(2,"Flexiones de brazos",3,"10","60s","Rodillas si es necesario",GrupoMuscular.PECHO),
                            ej(3,"Remo con goma elastica",3,"12","60s","Tension constante",GrupoMuscular.ESPALDA),
                            ej(4,"Estocadas alternadas",3,"10 c/lado","60s","Peso corporal",GrupoMuscular.PIERNAS),
                            ej(5,"Plancha",3,"30 seg","45s","Core activado, no hundir caderas",GrupoMuscular.CORE),
                            ej(6,"Burpees",3,"8","90s","Modificados si es necesario",GrupoMuscular.FULLBODY)
                    )));
                    rutinaRepo.save(rFull);

                    // CrossFit WOD - avanzado
                    Rutina rCf = new Rutina();
                    rCf.setNombre("CrossFit - AMRAP 20 min");
                    rCf.setDescripcion("AMRAP 20 minutos. Hacer el mayor numero de rondas posible en 20 min.");
                    rCf.setTipo(TipoRutina.PLANTILLA); rCf.setNivel(NivelRutina.AVANZADO);
                    rCf.setObjetivo(ObjetivoRutina.RESISTENCIA); rCf.setDia("WOD");
                    rCf.setDisciplina(cross);
                    rCf.setEjercicios(new ArrayList<>(Arrays.asList(
                            ej(1,"Thrusters (barra)",1,"10","AMRAP","43 kg hombres / 29 kg mujeres",GrupoMuscular.FULLBODY),
                            ej(2,"Pull-ups",1,"10","AMRAP","Estrictos o kipping",GrupoMuscular.ESPALDA),
                            ej(3,"Box jumps",1,"10","AMRAP","60 cm hombres / 50 cm mujeres",GrupoMuscular.PIERNAS)
                    )));
                    rutinaRepo.save(rCf);

                    // Rutina personalizada para Carlos (musculacion avanzada)
                    Rutina rPersonal = new Rutina();
                    rPersonal.setNombre("Plan Fuerza - " + a1.getApellido() + " " + a1.getNombre());
                    rPersonal.setDescripcion("Plan de fuerza maxima 5x5. Incrementar 2.5kg por semana en cada ejercicio.");
                    rPersonal.setTipo(TipoRutina.PERSONALIZADA); rPersonal.setNivel(NivelRutina.AVANZADO);
                    rPersonal.setObjetivo(ObjetivoRutina.FUERZA); rPersonal.setDia("Lunes / Miercoles / Viernes");
                    rPersonal.setDisciplina(musc); rPersonal.setAlumno(a1);
                    rPersonal.setEjercicios(new ArrayList<>(Arrays.asList(
                            ej(1,"Sentadilla",5,"5","3 min","100 kg - progresion semanal",GrupoMuscular.PIERNAS),
                            ej(2,"Press banca",5,"5","3 min","80 kg - progresion semanal",GrupoMuscular.PECHO),
                            ej(3,"Peso muerto",1,"5","5 min","120 kg - solo lunes",GrupoMuscular.ESPALDA),
                            ej(4,"Press militar",5,"5","3 min","50 kg",GrupoMuscular.HOMBROS),
                            ej(5,"Remo Pendlay",5,"5","3 min","70 kg",GrupoMuscular.ESPALDA)
                    )));
                    rutinaRepo.save(rPersonal);

                    // Progreso de Carlos (3 entradas historicas)
                    progresoRepo.save(crearProgreso(a1, LocalDate.now().minusMonths(3), new BigDecimal("88.5"), new BigDecimal("178"), new BigDecimal("22"), "95", "102", "38", "62", "99", "Inicio del plan de fuerza"));
                    progresoRepo.save(crearProgreso(a1, LocalDate.now().minusMonths(2), new BigDecimal("87.0"), new BigDecimal("178"), new BigDecimal("20"), "93", "103", "39", "63", "100", "Bajando grasa, manteniendo fuerza"));
                    progresoRepo.save(crearProgreso(a1, LocalDate.now().minusMonths(1), new BigDecimal("86.2"), new BigDecimal("178"), new BigDecimal("18"), "91", "104", "40", "64", "101", "Evolucion muy positiva"));

                    // Progreso de Laura
                    progresoRepo.save(crearProgreso(a2, LocalDate.now().minusMonths(2), new BigDecimal("62.0"), new BigDecimal("165"), new BigDecimal("28"), "74", "90", "30", "57", "95", "Primera medicion"));
                    progresoRepo.save(crearProgreso(a2, LocalDate.now().minusMonths(1), new BigDecimal("61.0"), new BigDecimal("165"), new BigDecimal("26"), "72", "90", "30", "57", "94", "Mejora en flexibilidad notable"));
                }
            }
        }
    }

    private Alumno crearAlumno(String nombre, String apellido, String dni, String email,
                                String tel, LocalDate fechaNac, Set<Disciplina> disciplinas) {
        Alumno a = new Alumno();
        a.setNombre(nombre); a.setApellido(apellido); a.setDni(dni);
        a.setEmail(email); a.setTelefono(tel); a.setFechaNacimiento(fechaNac);
        a.setFechaAlta(LocalDate.now().minusMonths(4));
        a.setContactoEmergencia("Familiar - 3624000000");
        a.setActivo(true); a.setDisciplinas(new HashSet<>(disciplinas));
        return a;
    }

    private Ejercicio ej(int orden, String nombre, int series, String reps, String descanso, String notas, GrupoMuscular gm) {
        Ejercicio e = new Ejercicio();
        e.setOrden(orden); e.setNombre(nombre); e.setSeries(series);
        e.setRepeticiones(reps); e.setDescanso(descanso); e.setNotas(notas); e.setGrupoMuscular(gm);
        return e;
    }

    private ProgresoAlumno crearProgreso(Alumno alumno, LocalDate fecha,
                                          BigDecimal peso, BigDecimal altura, BigDecimal grasa,
                                          String cintura, String pecho, String brazos, String piernas, String gluteos, String notas) {
        ProgresoAlumno p = new ProgresoAlumno();
        p.setAlumno(alumno); p.setFecha(fecha); p.setPeso(peso); p.setAltura(altura);
        p.setPorcentajeGrasa(grasa);
        p.setCintura(new BigDecimal(cintura)); p.setPecho(new BigDecimal(pecho));
        p.setBrazos(new BigDecimal(brazos)); p.setPiernas(new BigDecimal(piernas));
        p.setGluteos(new BigDecimal(gluteos)); p.setNotas(notas);
        return p;
    }
}
