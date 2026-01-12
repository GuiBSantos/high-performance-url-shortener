#  High Performance URL Shortener API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)

API de encurtamento de URLs focada em **alta performance**, **escalabilidade** e **analytics em tempo real**. O projeto utiliza uma arquitetura híbrida de Cache-Aside com Redis para garantir latência mínima (< 10ms) nos redirecionamentos, mesmo sob alta carga.

##  Arquitetura & Design Patterns

O sistema foi desenhado para resolver gargalos de leitura e escrita em sistemas de alto tráfego:

* **Cache Strategy (Redis):** Implementação de *Cache-Aside*. URLs requisitadas são armazenadas no Redis com TTL configurável. Leituras subsequentes ocorrem inteiramente em memória, sem tocar no banco de dados.
* **Smart Analytics:** A contagem de acessos (`access_count`) utiliza uma estratégia de escrita otimizada: o incremento é feito no Redis (para velocidade imediata) e sincronizado com o PostgreSQL para persistência duradoura.
* **Database Migrations (Flyway):** Versionamento automático do Schema do banco de dados, garantindo que a estrutura (como a tabela `urls`) seja criada consistentemente em qualquer ambiente (Dev/Prod).
* **Docker Multi-Stage Build:** O container de produção é otimizado, contendo apenas o JRE necessário e o artefato compilado, reduzindo o tamanho da imagem e aumentando a segurança.

##  Tech Stack

* **Core:** Java 21 (LTS), Spring Boot 3
* **Dados:** PostgreSQL 16, Redis 7 (Alpine)
* **Infra:** Docker, Docker Compose
* **Testes:** k6 (Load Testing)
* **Doc:** Swagger / OpenAPI

##  Como Rodar

O projeto está totalmente containerizado. Você não precisa ter Java ou Banco de Dados instalados na sua máquina, apenas o Docker.

### Pré-requisitos
* Docker & Docker Compose.

### Passo a Passo

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/GuiBSantos/high-performance-url-shortener.git](https://github.com/GuiBSantos/high-performance-url-shortener.git)
    cd high-performance-url-shortener
    ```

2.  **Suba a aplicação:**
    ```bash
    docker-compose up --build
    ```
    *Aguarde até ver a mensagem `Started Application` no log. O Flyway aplicará as tabelas automaticamente.*

3.  **Acesse a Documentação (Swagger):**
    [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

##  Endpoints Principais

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **POST** | `/api/shorten` | Cria uma URL curta e retorna o código. |
| **GET** | `/{shortCode}` | Redireciona para a URL original (Cacheado no Redis). |
| **GET** | `/api/stats/{shortCode}` | Retorna o total de cliques (Analytics). |

##  Configuração (Variáveis de Ambiente)

O projeto utiliza perfis do Spring. Para rodar localmente com Docker, o `docker-compose.yml` já injeta as configurações necessárias usando o perfil `prod`:

| Variável | Descrição | Padrão (Docker Compose) |
| :--- | :--- | :--- |
| `DB_URL_PROD` | JDBC URL do Postgres | `jdbc:postgresql://postgres:5432/shortener` |
| `DB_USER` | Usuário do Banco | `postgres` |
| `DB_PASSWORD` | Senha do Banco | `12345` |
| `REDIS_HOST` | Host do Redis | `redis` |
| `REDIS_PORT` | Porta do Redis | `6379` |
| `REDIS_PASSWORD` | Senha do Redis | `12345` |

## Performance

O projeto inclui scripts de teste de carga (se aplicável). Em testes locais simulando concorrência:

* **Throughput:** Alta capacidade de reqs/s devido ao Cache.
* **Latência (Cache Hit):** < 5ms
* **Latência (Cache Miss):** ~20-50ms (dependendo do disco)
