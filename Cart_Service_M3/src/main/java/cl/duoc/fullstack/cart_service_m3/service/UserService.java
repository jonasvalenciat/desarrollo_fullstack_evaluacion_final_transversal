package cl.duoc.fullstack.cart_service_m3.service;

import cl.duoc.fullstack.cart_service_m3.dto.UserCreateDTO;
import cl.duoc.fullstack.cart_service_m3.dto.UserResponseDTO;
import cl.duoc.fullstack.cart_service_m3.model.User;
import cl.duoc.fullstack.cart_service_m3.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<UserResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<UserResponseDTO> getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse);
    }

    public UserResponseDTO create(UserCreateDTO request) {
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.email());
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return toResponse(repository.save(user));
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
