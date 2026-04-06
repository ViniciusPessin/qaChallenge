# Capítulo QA Challenge — Testes Automatizados

Solução completa para o desafio técnico de **QA Sênior**, cobrindo automação Web, API e Performance.

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
- **Page Object Model (POM)**: seletores e ações da página centralizados em `BlogSearchPage.java`, desacoplados dos testes.
- **Fluent Interface**: encadeamento de ações legível (`.open().clickSearchIcon().typeSearchTerm(...).submitSearch()`).
- **WebDriverManager**: gerencia o ChromeDriver automaticamente sem configuração manual.
- **Headless Chrome**: executa sem interface gráfica — ideal para CI/CD.

### Como executar

```bash
# Apenas testes web
mvn test -pl qa-web

# Com relatório Allure
mvn test allure:report -pl qa-web
# Relatório em: qa-web/target/site/allure-maven-plugin/index.html
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
# Apenas testes de API
mvn test -pl qa-api

# Com relatório Allure
mvn test allure:report -pl qa-api
# Relatório em: qa-api/target/site/allure-maven-plugin/index.html
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

### Asserções incluídas no .jmx
- `HTTP 200` em todos os passos do fluxo
- Presença do texto `"Choose Your Flight"` na página de seleção
- Presença do texto `"Thank you for your purchase today!"` na confirmação
- Extração dinâmica do `flight_id` via RegexExtractor (correlação)

### Como executar

```bash
# Pré-requisito: JMeter instalado e no PATH

# Modo não-gráfico (recomendado para CI)
jmeter -n \
  -t qa-performance/jmeter/blazedemo-performance-tests.jmx \
  -l qa-performance/target/results.jtl \
  -e -o qa-performance/target/html-report

# O relatório HTML ficará em: qa-performance/target/html-report/index.html

# Modo gráfico (para desenvolvimento e visualização)
jmeter -t qa-performance/jmeter/blazedemo-performance-tests.jmx
```

### 📊 Análise do Critério de Aceitação

> **Conclusão:** O critério de 250 req/s com P90 < 2s é **desafiador** para o ambiente BlazDemo.

O BlazDemo é um site de demonstração com infraestrutura limitada, sem garantia de SLA. Em execuções típicas:

| Métrica | Resultado esperado (demo) | Critério |
|---------|--------------------------|----------|
| Throughput | ~50–120 req/s | 250 req/s |
| P90 | ~2,5–6s | < 2s |

**Motivos pelos quais o critério pode não ser atendido:**
1. O servidor BlazDemo é compartilhado e de baixo custo — não suporta essa carga
2. O fluxo de 4 passos com delays de think time reduz o throughput efetivo
3. Limitações de rede e latência da máquina de execução

**O que o teste comprova independentemente do resultado numérico:**
- O script está corretamente estruturado com correlação de parâmetros dinâmicos
- As asserções garantem que o fluxo de compra foi executado com sucesso
- O relatório HTML do JMeter apresenta todos os percentis, erros e gráficos de carga

Para atingir o critério em um ambiente de produção real, recomenda-se: infraestrutura dedicada, CDN, balanceamento de carga e otimização de queries.

---

## 🚀 CI/CD

### GitHub Actions
Pipeline em `.github/workflows/ci.yml`:
- **api-tests**: executa testes de API em qualquer push/PR
- **web-tests**: executa testes Web com Chrome headless
- **performance-tests**: executa JMeter apenas em merges para `main`
- Artefatos: relatórios Allure e JUnit XML disponíveis em cada run

### GitLab CI/CD
Pipeline em `.gitlab-ci.yml`:
- Stages: `test → report → performance`
- Relatórios JUnit integrados ao GitLab Merge Requests
- Cache do repositório Maven para builds mais rápidos

---

## 📈 Relatórios

### Allure Report (Web + API)
```bash
# Instalar Allure CLI (opcional — o plugin Maven já gera o relatório)
npm install -g allure-commandline

# Gerar e servir o relatório interativamente
allure serve qa-api/target/allure-results
allure serve qa-web/target/allure-results
```

O relatório Allure inclui:
- Visão geral de status (passed/failed/broken)
- Detalhes de cada teste com request/response capturados
- Histórico de execuções
- Categorização por Feature, Story e Severity

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

**Vinicius** — Desafio técnico QA Sênior — Capítulo Qualidade
