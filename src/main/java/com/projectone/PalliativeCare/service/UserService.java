package com.projectone.PalliativeCare.service;


import com.mongodb.DuplicateKeyException;
import com.projectone.PalliativeCare.dto.LoginRequestDTO;
import com.projectone.PalliativeCare.dto.RegisterRequestDTO;
import com.projectone.PalliativeCare.model.Role;
import com.projectone.PalliativeCare.model.User;
import com.projectone.PalliativeCare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String register(RegisterRequestDTO registerRequest) {

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .middleName(registerRequest.getMiddleName())
                .lastName(registerRequest.getLastName())
                .birthDate(registerRequest.getBirthDate())
                .mobile(registerRequest.getMobile())
                .email(registerRequest.getEmail())
                .address(registerRequest.getAddress())
                .password(encoder.encode(registerRequest.getPassword())) // Hash the password
                .role(registerRequest.getRole() != null ? registerRequest.getRole() : Role.PATIENT)
                .build();

        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        try {
            userRepo.save(user);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Email already exists");
        }
        return jwtService.generateToken(user.getEmail());
    }


    // This method returns the token or throws an exception if authentication fails
    public String login(LoginRequestDTO loginRequest) {
        // The authenticate method will throw an exception if credentials are bad
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(loginRequest.getEmail());
        }
        // This part is technically unreachable if auth fails, as it throws an exception first
        throw new BadCredentialsException("Invalid username or password");
    }
}
