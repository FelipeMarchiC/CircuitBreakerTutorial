package br.ifsp.bes.circuitBreaker.service;

import br.ifsp.bes.circuitBreaker.model.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    @TimeLimiter(name = "userService")
    public CompletableFuture<User> getUser(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long delay = (long) (100 + Math.random() * 800);
                Thread.sleep(delay);

                if (delay > 500) {
                    throw new RuntimeException("Simulando falha lógica");
                }

                return new User(id, "User " + id + " (REAL)");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrompida", e);
            }
        });

    }

    private CompletableFuture<User> fallbackGetUser(String id, Throwable throwable) {
        return CompletableFuture.completedFuture(new User(id, "Fallback User"));
    }
}
