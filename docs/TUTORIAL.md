# Tutorial: Circuit Breaker com Resilience4j em Spring Boot

Esse material abrange a base teórica, a decisão tecnológica e a implementação prática do padrão Circuit Breaker, utilizando a biblioteca Resilience4j no contexto do framework Spring Boot. O objetivo é estabelecer uma arquitetura de microsserviços mais robusta e tolerante a falhas.

## Sumário

1.  Fundamentação Teórica e Conceitual
2.  Análise e Escolha Tecnológica
3.  Visão Geral da Prova de Conceito (PoC)
4.  Mini-Tutorial para Reprodução Prática

-----

## 1\. Fundamentação Teórica e Conceitual

### 1.1 O Padrão Circuit Breaker

O Circuit Breaker é um padrão de resiliência fundamental. Sua finalidade é evitar que uma falha persistente ou um recurso externo lento provoque uma falha em cascata em um sistema distribuído. Sua operação se baseia em três estados primários:

| Estado | Descrição | Comportamento Primário |
| :--- | :--- | :--- |
| **CLOSED (Fechado)** | Estado operacional normal. | Permite que as requisições atinjam o recurso externo. |
| **OPEN (Aberto)** | O limite de falhas foi excedido. | Bloqueia as requisições imediatamente (padrão *fail fast*). |
| **HALF\_OPEN (Meio-Aberto)** | Período de recuperação. | Permite um número limitado de requisições de teste para verificar a saúde do recurso. |

### 1.2 Objetivos de Implementação

A adoção do Circuit Breaker visa atingir os seguintes objetivos de qualidade:

  * **Minimização da Latência:** A latência percebida é reduzida ao evitar que o consumidor espere por um *timeout* extenso em um serviço sabidamente inoperante.
  * **Proteção de Recursos:** Preserva recursos internos do consumidor (como *threads* e *conexões de pool*) de ficarem retidos em espera.
  * **Gestão de Recuperação:** Permite que o recurso externo se recupere sem ser sobrecarregado por tentativas imediatas de reconexão.

### 1.3 Componentes de Resiliência Correlatos

O Circuit Breaker deve ser integrado a uma estratégia de resiliência mais ampla:

  * **Timeouts:** Impõe um limite de tempo máximo de espera por uma resposta, evitando chamadas pendentes indefinidamente.
  * **Retries (Tentativas):** Permite novas tentativas de chamada, idealmente utilizando estratégias de **Backoff Exponencial** para evitar sobrecarga.
  * **Fallback:** Fornece uma resposta alternativa (e.g., dados em cache, valor padrão) quando o caminho primário falha ou é interrompido.

### 1.4 Antipadrões a Serem Evitados

  * **Ausência de Timeout:** A falta de um limite de tempo pode levar ao esgotamento de *threads* e recursos.
  * **Retries Sem Backoff:** Tentativas imediatas e agressivas aumentam a carga e impedem a recuperação do serviço instável.

-----

## 2\. Análise e Escolha Tecnológica

A seguir, apresentamos uma comparação técnica das opções disponíveis no ecossistema Java para a implementação de resiliência:

| Tecnologia | Vantagens | Desvantagens | Recomendação |
| :--- | :--- | :--- | :--- |
| **Resilience4j** | Conjunto completo de padrões (CB, Retry, TimeLimiter, Bulkhead); Alta atividade de desenvolvimento; Integração nativa com Spring Boot e Micrometer. | Requer aprendizado da configuração modular. | **Adotada** |
| Netflix Hystrix | Referência histórica do padrão. | **Projeto descontinuado.** Não deve ser utilizado em novas implementações. | Obsoleta |
| Spring Retry | Simplicidade na gestão de Retry; Forte integração com o *core* do Spring. | Não implementa o padrão Circuit Breaker nativamente. | Combinável |

### 2.1 Decisão Adotada: Resilience4j

A escolha do **Resilience4j** se justifica por ser uma biblioteca **ativa e modular**, que oferece o conjunto completo de funcionalidades de resiliência. Sua integração fluida com o Spring Boot e sua capacidade de fornecer métricas detalhadas a tornam a solução técnica mais robusta para ambientes de microsserviços modernos.

-----

## 3\. Visão Geral da Prova de Conceito (PoC)

A implementação prática é demonstrada através de um serviço de consulta à previsão do tempo, que utiliza cache para garantir a disponibilidade.

### 3.1 Fluxo de Execução com Resiliência

1.  Requisição HTTP: `GET /weather/ok/{city}`.
2.  Camada de Serviço: Execução da chamada à **API Externa**. Esta chamada é envolvida pelos padrões **Circuit Breaker** e **TimeLimiter**.
3.  Monitoramento: O Resilience4j monitora a taxa de falhas e o tempo de resposta.
4.  Falha/Timeout: Em caso de erro ou de estouro do tempo limite, o método de **Fallback** é acionado.
5.  Fallback: O método de fallback consulta os dados no **Redis (Cache)** e retorna o valor em cache para o cliente.

### 3.2 Estrutura do Código

Os elementos chave da implementação estão localizados em:

  * `application.yaml`: Contém as configurações de *thresholds* do Circuit Breaker e do TimeLimiter.
  * `WeatherService.java`: Contém as anotações `@CircuitBreaker`, `@TimeLimiter` e a definição do método de *fallback*.

-----

## 4\. Mini-Tutorial para Reprodução Prática

Para replicar esta Prova de Conceito em seu ambiente, siga os procedimentos descritos abaixo.

### 4.1 Pré-requisitos Técnicos

  * Java Development Kit (JDK) 17 ou superior.
  * Maven Wrapper (presente no projeto).
  * Uma instância do **Redis** em execução na porta padrão (`localhost:6379`).
  * Uma chave de acesso válida para a API de Previsão do Tempo.

### 4.2 Configuração Inicial

1.  **Configuração da Chave da API:**

    Edite o arquivo `src/main/resources/application.yaml` e insira a chave da API:

    ```yaml
    weather:
      api:
        key: SUA_CHAVE_AQUI 
    ```

2.  **Inicialização do Redis (Exemplo Docker):**

    ```bash
    docker run --name redis -p 6379:6379 -d redis:7
    docker exec -it redis redis-cli ping  # resposta esperada: PONG
    ```

### 4.3 Execução e Testes

1.  **Executar a Aplicação:**

    Inicie o serviço Spring Boot:

    ```bash
    ./mvnw spring-boot:run
    ```

2.  **Verificação do Fluxo Normal:**

    Execute uma chamada. A resposta deve ser da API externa, e os dados devem ser armazenados no cache.

    ```bash
    curl http://localhost:8080/weather/ok/London
    ```

3.  **Observação do Circuit Breaker/TimeLimiter:**

      * **Acionamento do Fallback:** Exceda o *timeout* configurado (e.g., simulando latência). O **Fallback** será ativado, retornando a resposta do cache.
      * **Abertura do Circuito:** Repita as chamadas com falha ou *timeout* até que a **taxa de falhas** configure a abertura do Circuit Breaker.
      * **Fail Fast:** No estado **OPEN**, as chamadas subsequentes falharão imediatamente sem tentar contatar o serviço externo, conforme demonstrado nos logs.

### 4.4 Dicas de Debug e Monitoramento

  * **Verificação de Cache:** Utilize o cliente Redis para inspecionar os dados armazenados:
    ```bash
    docker exec -it redis redis-cli GET London
    ```
  * **Experimentação:** Altere os parâmetros `failure-rate-threshold` ou `waitDurationInOpenState` no `application.yaml` para observar o impacto imediato na transição de estados do Circuit Breaker.