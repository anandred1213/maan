package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.*;
import com.practise.userservice.entity.RefreshToken;
import com.practise.userservice.entity.User;
import com.practise.userservice.repository.UserProfileImageRepository;
import com.practise.userservice.repository.UserRepository;
import com.practise.userservice.security.CustomUserDetails;
import com.practise.userservice.security.CustomUserDetailsService;
import com.practise.userservice.security.JwtUtil;
import com.practise.userservice.service.RefreshTokenService;
import com.practise.userservice.service.UserAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserProfileImageRepository userProfileImageRepository;


    public UserAuthenticationServiceImpl(UserRepository userRepository,
                                         PasswordEncoder passwordEncoder,
                                         RefreshTokenService refreshTokenService,
                                         CustomUserDetailsService customUserDetailsService,
                                         JwtUtil jwtUtil,
                                         AuthenticationManager authenticationManager,
                                         UserProfileImageRepository userProfileImageRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userProfileImageRepository = userProfileImageRepository;
    }

    @Override
    public boolean validateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public AuthResponse login(AuthRequest request) {

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();
        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
        User user = userRepository.findByEmail(userDetails.getUsername());

        String profileImageUrl = userProfileImageRepository.existsById(user.getId())
                ? "/users/profile-image"
                : null;

        log.info("Login successful - id: {}, name: {}, email: {}", user.getId(), user.getName(), user.getEmail());
        return new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getName(), user.getEmail(), profileImageUrl,user.getRole());
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("invalid or expired refresh token"));
        String userEmail = refreshToken.getUserEmail();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

        String newAccessToken = jwtUtil.generateToken(userDetails);

        refreshTokenService.deleteByUserEmail(userEmail);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userEmail);
        return new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());
    }
}
