## Tutorial: Circuit Breaker com Resilience4j (Spring Boot + Redis)

Este tutorial utiliza uma PoC para demonstrar, passo a passo, como aplicar Circuit Breaker e TimeLimiter, além de implementar fallback com cache Redis.

### Objetivos de Aprendizagem
- Compreender quando e por que aplicar Circuit Breaker e TimeLimiter.  
- Configurar e monitorar as transições de estado do Circuit Breaker.  
- Implementar fallback seguro utilizando Redis como fonte de dados de contingência.  
- Desenvolver testes (manuais e automatizados) que simulam falhas reais.

### Visão Geral da Arquitetura
- Endpoint: `GET /weather/ok/{city}`  
- Serviço: realiza consulta à WeatherAPI e armazena a resposta no Redis (chave = cidade)  
- Resiliência: anotação `@TimeLimiter` (timeout) combinada com `@CircuitBreaker` (abre/fecha o circuito)  
- Fallback: em caso de timeout, falha ou circuito aberto, retorna dados em cache do Redis  

***

## 1. Preparação do Ambiente  
- Java 17+ e Maven Wrapper (`mvnw` / `mvnw.cmd`)  
- Redis rodando em `localhost:6379`  
- Chave válida da WeatherAPI  

Formas de executar o Redis através do Docker:    
```powershell
docker run --name redis -p 6379:6379 -d redis:7
docker exec -it redis redis-cli ping   # responde PONG
```

Formas alternativas de executar o Redis:
1) WSL (Ubuntu)  
```bash
sudo apt update && sudo apt install -y redis-server
sudo service redis-server start
redis-cli ping
```
2) Windows (Memurai)  
- Instale o Memurai Community (escutando em `localhost:6379`).

Verificação da porta no Windows:  
```powershell
Test-NetConnection -ComputerName localhost -Port 6379
```

***

## 2. Configuração da Aplicação  
Coloque sua chave de API no arquivo `src/main/resources/application.yaml`:  
```yaml
weather:
  api:
    key: SUA_CHAVE_AQUI
```

Configuração Resilience4j padrão:  
- CircuitBreaker `weatherApi`: `sliding-window-size: 5`, `failure-rate-threshold: 50`, `wait-duration-in-open-state: 5s`  
- TimeLimiter `weatherApi`: `timeout-duration: 5s`

Para iniciar a aplicação, execute:  
```powershell
./mvnw.cmd spring-boot:run
```

***

## 3. Exercícios

### 3.1 – Primeira chamada + funcionamento do cache  
1) Realize uma chamada válida:  
```
Invoke-RestMethod http://localhost:8080/weather/ok/London
```
2) Verifique se a resposta foi armazenada no Redis:  
```
docker exec -it redis redis-cli GET London
```

### 3.2 – Timeout + fallback  
1) Execute múltiplas chamadas com aumento gradativo do tempo de resposta (1s, 2s, 4s, 8s) até ultrapassar o timeout definido (5s), para ativar o fallback.  
2) Observe que, após o timeout, a resposta será retirada do cache Redis, se disponível; caso contrário, será retornada a mensagem `{"message":"cache_miss"}`.

### 3.3 – Simulação de indisponibilidade do Redis  
1) Pare o container Redis:  
```powershell
docker stop redis
```
2) Faça uma chamada ao endpoint e observe o fallback seguro, que deve retornar `{"message":"fallback_error"}`, evitando erro 500.  
3) Reinicie o Redis e repita os testes.

### 3.4 – Teste com chave da API inválida  
1) Altere a chave no arquivo `application.yaml` para uma inválida e realize os testes para diferenciar falhas de autenticação das situações de timeout e circuito aberto.

Recomenda-se utilizar o arquivo `requests.http` disponível na raiz do projeto com extensão REST Client para facilitar os testes.

***

## 4. Exercícios Complementares  
- Ajuste o `timeout-duration` para observar variações no acionamento do fallback.  
- Experimente alterar `sliding-window-size` e `failure-rate-threshold` e observe o impacto na abertura do circuito.  
- Implemente um header `X-From-Cache: true|false` para indicar a origem da resposta.  
- Crie um endpoint para retornar o estado do CircuitBreaker, utilizando Spring Boot Actuator.  
- Simule latências e falhas utilizando WireMock na WeatherAPI.

***

## 5. Solução de Problemas Comuns  
- Erro 500: verifique a assinatura do método fallback `fallbackWeather(String city, Throwable throwable)` e a disponibilidade do Redis.  
- Mensagem `cache_miss`: indica que não há dado em cache; execute uma chamada bem-sucedida antes.  
- Mensagem `fallback_error`: indica falha de acesso ao Redis no fallback.  
- Porta 8080 ocupada: altere a propriedade `server.port` no arquivo `application.yaml`.

***

## 6. Próximos Passos Recomendados  
- Integrar Spring Boot Actuator para métricas, health checks e monitoramento do Circuit Breaker.  
- Utilizar Testcontainers (com Redis) e WireMock para testes de integração confiáveis.  
- Implementar práticas de observabilidade com logs estruturados, métricas e dashboards.

***

## Referências  
- Resilience4j: `https://resilience4j.readme.io/`  
- WeatherAPI: `https://www.weatherapi.com/`

---

## Endpoints
- `GET /weather/ok/{city}`: Endpoint principal
- Observação: endpoints de Actuator/monitoramento podem ser adicionados conforme necessidade didática