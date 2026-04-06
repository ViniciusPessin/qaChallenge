# Capítulo QA Challenge — Testes Automatizados

Solução completa para o desafio técnico de **QA Sênior**, cobrindo automação Web, API e Performance.

---

## 📊 Relatórios de Execução

| Módulo | Link |
|--------|------|
| 🌐 Web — Blog do Agi | [Ver relatório Allure](https://viniciuspessin.github.io/qaChallenge/allure-web) |
| 🐶 API — Dog API | [Ver relatório Allure](https://viniciuspessin.github.io/qaChallenge/allure-api) |
| ⚡ Performance — BlazDemo | [Ver relatório JMeter](https://viniciuspessin.github.io/qaChallenge/jmeter-report) |

---

## 📁 Estrutura do Projeto

```
capitulo-qa-challenge/
├── qa-web/                          # Testes Web — Blog do Agi (Selenium + JUnit 5)
│   └── src/test/java/br/com/qa/web/
│       ├── base/BaseTest.java       # Setup/teardown do WebDriver
│       ├── pages/BlogSearchPage.java # Page Object da busca
│       └── tests/BlogSearchTest.java # Cenários de teste
│
├── qa-api/                          # Testes de API — Dog API (RestAssured + JUnit 5)
│   └── src/test/java/br/com/qa/api/
│       ├── base/BaseApiTest.java    # Configuração do RestAssured
│       └── tests/DogApiTest.java    # Cenários de teste
│
├── qa-performance/                  # Testes de Performance — BlazDemo (JMeter)
│   └── jmeter/
│       └── blazedemo-performance-tests.jmx
│
├── .github/workflows/ci.yml         # Pipeline GitHub Actions
├── .gitlab-ci.yml                   # Pipeline GitLab CI/CD
└── pom.xml                          # POM pai (Maven multi-módulo)
```

---

## ✅ Pré-requisitos

| Ferramenta        | Versão mínima | Observação                            |
|-------------------|---------------|---------------------------------------|
| Java (JDK)        | 17+           | `java -version`                       |
| Maven             | 3.9+          | `mvn -version`                        |
| Google Chrome     | Qualquer      | Para testes Web (ChromeDriver gerenciado automaticamente via WebDriverManager) |
| Apache JMeter     | 5.6+          | Apenas para testes de Performance     |

---

## 🌐 Módulo 1 — Testes Web (Blog do Agi)

### Cenários automatizados

| # | Cenário | Severidade |
|---|---------|------------|
| 1 | Busca por termo válido ("crédito") deve retornar ao menos 1 artigo | 🔴 CRITICAL |
| 2 | Busca por termo inválido deve exibir mensagem de "nenhum resultado" | 🟡 NORMAL |
| 3 | URL após busca deve conter parâmetro `?s=` com o termo pesquisado | 🟢 MINOR |
| 4 | Todos os artigos retornados devem possuir título visível (integridade) | 🟡 NORMAL |

### Padrões aplicados
- **Page Object Model (POM)**: seletores e ações da página centralizados em `BlogSearchPage.java`
- **Navegação direta via URL**: busca feita via `?s=termo` para máxima estabilidade em headless
- **WebDriverManager**: gerencia o ChromeDriver automaticamente sem configuração manual
- **Headless Chrome**: executa sem interface gráfica — ideal para CI/CD

### Como executar

```bash
mvn test -pl qa-web
mvn allure:serve -pl qa-web
```

---

## 🐶 Módulo 2 — Testes de API (Dog API)

**URL base:** `https://dog.ceo/api`

### Endpoints testados

#### `GET /breeds/list/all`
| # | Cenário | Severidade |
|---|---------|------------|
| 1 | Deve retornar HTTP 200 e status `"success"` | 🔴 BLOCKER |
| 2 | Payload deve conter map de raças com raças conhecidas (hound, labrador, poodle) | 🔴 CRITICAL |
| 3 | Cada raça deve ter lista (vazia ou com sub-raças) como valor | 🟡 NORMAL |
| 4 | Tempo de resposta inferior a 3 segundos | 🟡 NORMAL |

#### `GET /breed/{breed}/images`
| # | Cenário | Severidade |
|---|---------|------------|
| 5 | Raças válidas (hound, labrador, poodle, beagle) devem retornar lista de imagens não vazia | 🔴 CRITICAL |
| 6 | Todas as URLs devem ser válidas (`https://` + extensão `.jpg/.png`) | 🔴 CRITICAL |
| 7 | Raça inválida deve retornar HTTP 404 e status `"error"` | 🟡 NORMAL |
| 8 | Sub-raça válida (`hound/afghan`) deve retornar imagens | 🟡 NORMAL |

#### `GET /breeds/image/random`
| # | Cenário | Severidade |
|---|---------|------------|
| 9 | Deve retornar HTTP 200 e URL de imagem válida | 🔴 BLOCKER |
| 10 | Duas chamadas devem (provavelmente) retornar URLs diferentes | 🟢 MINOR |
| 11 | `GET /breeds/image/random/{count}` deve retornar exatamente N imagens | 🟡 NORMAL |
| 12 | Tempo de resposta inferior a 2 segundos | 🟡 NORMAL |

### Como executar

```bash
mvn test -pl qa-api
mvn allure:serve -pl qa-api
```

---

## ⚡ Módulo 3 — Testes de Performance (BlazDemo)

**URL:** `https://www.blazedemo.com`

**Critério de aceitação:** 250 req/s com P90 (90th percentil) < 2 segundos

### Fluxo testado
```
GET  /                      → Página inicial
POST /reserve.php           → Seleção de voo (Boston → London)
POST /purchase.php          → Formulário com dados do passageiro
POST /confirmation.php      → Confirmação da compra ✅
```

### Tipos de teste

| Tipo | Usuários | Ramp-up | Iterações | Objetivo |
|------|----------|---------|-----------|----------|
| **Load Test** | 250 | 60s | 5 por usuário | Validar comportamento em carga sustentada |
| **Spike Test** | 250 | 5s | 3 por usuário | Validar resiliência em picos abruptos |

### Como executar

```bash
jmeter -n \
  -t qa-performance/jmeter/blazedemo-performance-tests.jmx \
  -l qa-performance/target/results.jtl \
  -e -o qa-performance/target/html-report
```

### 📊 Análise do Critério de Aceitação

O critério de **250 req/s com P90 < 2s** é desafiador para o ambiente BlazDemo, que é um site de demonstração sem infraestrutura dedicada. O objetivo do teste é demonstrar a capacidade técnica de modelar o fluxo completo com correlação de parâmetros dinâmicos, load test, spike test e análise crítica dos resultados.

---

## 🚀 CI/CD

Pipeline em `.github/workflows/ci.yml` com 3 jobs:
- **api-tests**: testes de API + relatório Allure publicado no GitHub Pages
- **web-tests**: testes Web com Chrome headless + relatório Allure publicado no GitHub Pages
- **performance-tests**: JMeter executado em merges para `master` + relatório publicado no GitHub Pages

---

## 🛠️ Stack Tecnológica

| Categoria | Tecnologia |
|-----------|------------|
| Linguagem | Java 17 |
| Build | Maven 3.9 |
| Testes Web | Selenium 4 + WebDriverManager |
| Testes API | RestAssured 5 |
| Framework de Testes | JUnit 5 |
| Asserções | AssertJ |
| Relatórios | Allure 2.27 |
| Performance | Apache JMeter 5.6 |
| CI/CD | GitHub Actions + GitLab CI/CD |

---

## 👤 Autor

**Vinicius Pessin** — Desafio técnico QA Sênior — Capítulo Qualidade