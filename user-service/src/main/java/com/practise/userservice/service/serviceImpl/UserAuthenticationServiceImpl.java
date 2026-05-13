package com.practise.userservice.service.serviceImpl;

import com.practise.userservice.dto.AuthRequest;
import com.practise.userservice.dto.AuthResponse;
import com.practise.userservice.dto.TokenRefreshRequest;
import com.practise.userservice.dto.TokenRefreshResponse;
import com.practise.userservice.entity.RefreshToken;
import com.practise.userservice.entity.User;
import com.practise.userservice.repository.UserRepository;
import com.practise.userservice.security.CustomUserDetails;
import com.practise.userservice.security.CustomUserDetailsService;
import com.practise.userservice.security.JwtUtil;
import com.practise.userservice.service.RefreshTokenService;
import com.practise.userservice.service.UserAuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Service
public class UserAuthenticationServiceImpl implements UserAuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private  final User user;

    public UserAuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                         RefreshTokenService refreshTokenService,
                                         CustomUserDetailsService customUserDetailsService,
                                         JwtUtil jwtUtil, AuthenticationManager authenticationManager, User user) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.user = user;
    }

    @Override
    public boolean validateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public AuthResponse login(AuthRequest request) {

        if (!user.getPassword().equals(user.getConfirmPassword())){
            return new AuthResponse("password and confirm password not match",null,null,null,null);
        }
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();
        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
        User user = userRepository.findByEmail(userDetails.getUsername());
        log.info("AuthResponse - id: {}, name: {}, email: {}", user.getId(), user.getName(), user.getEmail());
        return new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getName(), user.getEmail());
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        RefreshToken refreshToken= refreshTokenService.findByToken(requestRefreshToken).map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("invalid or expired refresh token"));
        String userEmail= refreshToken.getUserEmail();
       UserDetails userDetails =  customUserDetailsService.loadUserByUsername(userEmail);
 // generate new acceess token
        String newAccessToken = jwtUtil.generateToken(userDetails);

        // rotate refresh token
        refreshTokenService.deleteByUserEmail(userEmail);
        RefreshToken refreshToken1 = refreshTokenService.createRefreshToken(userEmail);
        return  new TokenRefreshResponse(newAccessToken, refreshToken1.getToken());
    }
}
