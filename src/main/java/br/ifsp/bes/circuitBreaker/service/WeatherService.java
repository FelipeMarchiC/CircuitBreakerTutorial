package br.ifsp.bes.circuitBreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final JedisPool JedisPool;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(RestTemplate restTemplate, JedisPool jedisPool) {
        this.restTemplate = restTemplate;
        this.JedisPool = jedisPool;
    }

    @TimeLimiter(name = "weatherApi")
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    @Retry(name = "weatherApi")
    public CompletableFuture<String> getWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            String url = String.format("https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no", apiKey, city);
            try (Jedis jedis = JedisPool.getResource()) {
                var response = restTemplate.getForObject(url, String.class);
                jedis.setex(city,300, response);
                return response;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        });

    }
    public CompletableFuture<String> fallbackWeather(String city, Throwable t)
    {
        return CompletableFuture.supplyAsync(() -> {
            String cached;
            try (Jedis jedis = JedisPool.getResource()) {
                cached = jedis.get(city);
                return  "motivo da chamada do fallback: "+t.getMessage() +"\nresultado do cache no fallback: \n" + cached;
            } catch (Exception e) {
                return "não foi possível recuperar o cache: \n" + e.getMessage() ;
            }

        });

    }
}

