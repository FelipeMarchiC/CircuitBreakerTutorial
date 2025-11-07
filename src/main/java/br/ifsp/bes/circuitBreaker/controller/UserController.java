package br.ifsp.bes.circuitBreaker.controller;

import br.ifsp.bes.circuitBreaker.model.User;
import br.ifsp.bes.circuitBreaker.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public CompletableFuture<User> getUser(@PathVariable String id) {
        return userService.getUser(id);
    }
}