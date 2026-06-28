package cl.duoc.fullstack.user_service_m1.controller;

import cl.duoc.fullstack.user_service_m1.model.User;
import cl.duoc.fullstack.user_service_m1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", description = "Creates a user with name, email and password. The password is hashed with BCrypt before persisting.")
    @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = User.class),
                    examples = @ExampleObject(value = """
                            { "id": 1, "name": "Juan Pérez", "email": "juan@example.com", "password": "$2a$10$..." }""")))
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate email",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                            { "errors": { "email": "Invalid email format", "name": "Name is required" } }""")))
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by its primary key")
    @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = User.class),
                    examples = @ExampleObject(value = """
                            { "id": 1, "name": "Juan Pérez", "email": "juan@example.com", "password": "$2a$10$..." }""")))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                            { "error": "User not found with id: 99" }""")))
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
