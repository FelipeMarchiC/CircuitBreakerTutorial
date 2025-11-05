package br.ifsp.bes.circuitBreaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CircuitBreakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CircuitBreakerApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean
    public JedisPool jedisPool()
    {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setJmxEnabled(false);

        return new JedisPool(poolConfig,"localhost", 6379);
    }
}
