package br.ifsp.bes.circuitBreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerLogger {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerLogger(CircuitBreakerRegistry circuitBreakerRegistry) {this.circuitBreakerRegistry= circuitBreakerRegistry;}

    @PostConstruct
    public void init(){
        var circuitBreaker= circuitBreakerRegistry.circuitBreaker("weatherApi");
        circuitBreaker.getEventPublisher().onStateTransition(e-> System.out.println("circuit breaker mudou de estado para: " + circuitBreaker.getState())).onEvent(e-> System.out.println("CircuitBreaker state: " + circuitBreaker.getState()));
    }
}
