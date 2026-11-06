package com.zhengli.catning.auth;

import com.zhengli.catning.auth.dto.AuthResponse;
import com.zhengli.catning.auth.dto.LoginRequest;
import com.zhengli.catning.auth.dto.RegisterRequest;
import com.zhengli.catning.auth.dto.RegisterResponse;
import com.zhengli.catning.common.BusinessException;
import com.zhengli.catning.security.JwtService;
import com.zhengli.catning.user.CustomUserDetailsService;
import com.zhengli.catning.user.Role;
import com.zhengli.catning.user.User;
import com.zhengli.catning.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String phone = request.phone().trim();
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException(HttpStatus.CONFLICT, "该手机号已注册");
        }

        User user = new User(
                request.name().trim(),
                phone,
                passwordEncoder.encode(request.password()),
                Role.CUSTOMER
        );
        User saved = userRepository.save(user);
        return new RegisterResponse(
                saved.getId(),
                saved.getName(),
                saved.getPhone(),
                saved.getRole()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String phone = request.phone().trim();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(phone, request.password())
        );

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(phone);
        String token = jwtService.generateToken(userDetails, user.getRole());
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }
}
