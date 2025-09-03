package com.projectone.PalliativeCare.service;


import com.mongodb.DuplicateKeyException;
import com.projectone.PalliativeCare.dto.LoginRequestDTO;
import com.projectone.PalliativeCare.dto.LoginResponseDTO;
import com.projectone.PalliativeCare.dto.RegisterRequestDTO;
import com.projectone.PalliativeCare.dto.UserAccountDTO;
import com.projectone.PalliativeCare.exception.UserAlreadyExistsException;
import com.projectone.PalliativeCare.model.ActivityType;
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

    @Autowired
    private ActivityService activityService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public LoginResponseDTO register(RegisterRequestDTO registerRequest) {

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
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        try {
            userRepo.save(user);
            activityService.logActivity(
                    user.getId(),
                    ActivityType.USER_CREATED,
                    "",
                    "Creation of User: "+ user.getFirstName()+" "+ user.getLastName());
        } catch (DuplicateKeyException e) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
//        return jwtService.generateToken(user.getEmail());

        // Generate token and convert user to DTO
        String token = jwtService.generateToken(user.getEmail());
        UserAccountDTO userAccountDTO = convertToUserAccountDTO(user);

        return new LoginResponseDTO(token, userAccountDTO);
    }


    // This method returns the token or throws an exception if authentication fails
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // The authenticate method will throw an exception if credentials are bad
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            // Get user from database
            User user = userRepo.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Generate token and convert user to DTO
            String token = jwtService.generateToken(loginRequest.getEmail());
            UserAccountDTO userAccountDTO = convertToUserAccountDTO(user);

            return new LoginResponseDTO(token, userAccountDTO);
        }
        // This part is technically unreachable if auth fails, as it throws an exception first
        throw new BadCredentialsException("Invalid username or password");
    }

    // Helper method to convert User to UserAccountDTO
    private UserAccountDTO convertToUserAccountDTO(User user) {
        UserAccountDTO dto = new UserAccountDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole()); // Convert enum to string

        // Add any additional fields you want to include
        // Add any additional fields you want to include
        if (user.getMiddleName() != null) {
            dto.setMiddleName(user.getMiddleName());
        }
        if (user.getMobile() != null) {
            dto.setMobile(user.getMobile());
        }
        if (user.getBirthDate() != null) {
            dto.setBirthDate(user.getBirthDate());
        }

        return dto;
        }
}