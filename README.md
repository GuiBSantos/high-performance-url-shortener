#  High Performance URL Shortener API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)
![AWS](https://img.shields.io/badge/AWS-Cloud-FF9900)
![Terraform](https://img.shields.io/badge/Terraform-IaC-7B42BC)
![Ansible](https://img.shields.io/badge/Ansible-Config-EE0000)

API de encurtamento de URLs focada em **alta performance**, **escalabilidade** e **deploy automatizado**. O projeto utiliza uma arquitetura híbrida de Cache-Aside com Redis para garantir latência mínima (< 10ms) e infraestrutura como código (IaC) para provisionamento na AWS.

##  Arquitetura & Design

O sistema foi desenhado para suportar alta carga de leitura e escrita:

* **Cache Strategy (Redis):** Implementação de *Cache-Aside*. URLs requisitadas ficam em memória. Leituras subsequentes não tocam no banco de dados.
* **Async Processing:** A contagem de acessos (Analytics) é processada de forma assíncrona para não bloquear a thread principal de requisição.
* **Docker Multi-Stage Build:** Imagens otimizadas (< 200MB) usando JRE Alpine.
* **Infrastructure as Code (IaC):**
    * **Terraform:** Provisiona a VPC, Security Groups e Instância EC2 na AWS.
    * **Ansible:** Configura o servidor, instala Docker e faz o deploy da aplicação automaticamente.

##  Tech Stack

* **Core:** Java 21, Spring Boot 3
* **Dados:** PostgreSQL 16, Redis 7 (Alpine)
* **DevOps:** Docker, Docker Compose, Terraform, Ansible, AWS (EC2)
* **Doc:** Swagger / OpenAPI

##  Como Rodar (Localmente)

O projeto é "Zero Config" com Docker.

1.  **Clone e suba a aplicação:**
    ```bash
    git clone [https://github.com/GuiBSantos/high-performance-url-shortener.git](https://github.com/GuiBSantos/high-performance-url-shortener.git)
    cd high-performance-url-shortener
    docker-compose up --build
    ```

2.  **Acesse a Documentação:**
     [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

##  Como Rodar (Deploy na AWS)

Este projeto contém automação completa de infraestrutura.

### Pré-requisitos
* Conta na AWS e AWS CLI configurado.
* Terraform e Docker instalados localmente.

### 1. Provisionar Infraestrutura (Terraform)
```bash
cd infra/terraform
terraform init
terraform apply -auto-approve
