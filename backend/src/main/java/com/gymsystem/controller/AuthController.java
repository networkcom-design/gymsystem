package com.gymsystem.controller;

import com.gymsystem.dto.LoginRequest;
import com.gymsystem.dto.LoginResponse;
import com.gymsystem.model.Usuario;
import com.gymsystem.repository.UsuarioRepository;
import com.gymsystem.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Usuario> opt = usuarioRepository.findByUsername(req.getUsername());
        if (opt.isEmpty() || !passwordEncoder.matches(req.getPassword(), opt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario o contraseña inválidos"));
        }
        Usuario u = opt.get();
        return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(u.getUsername()), u.getUsername(), u.getNombre()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        if (!jwtService.isValid(token)) return ResponseEntity.status(401).build();
        String username = jwtService.extractUsername(token);
        return usuarioRepository.findByUsername(username)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("username", u.getUsername(), "nombre", u.getNombre())))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
}
