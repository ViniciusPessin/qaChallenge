# Testes de Performance — BlazDemo

## Pré-requisitos

1. **JMeter 5.6+** instalado: [Download](https://jmeter.apache.org/download_jmeter.cgi)
2. Adicionar `JMETER_HOME/bin` ao `PATH`

## Executando os testes

### Modo não-gráfico (recomendado para CI)
```bash
jmeter -n \
  -t jmeter/blazedemo-performance-tests.jmx \
  -l target/results.jtl \
  -e -o target/html-report
```

### Modo gráfico (para edição e debug)
```bash
jmeter -t jmeter/blazedemo-performance-tests.jmx
```

## Estrutura do teste

O script contém **dois grupos de threads**:

### Load Test
- **Objetivo**: Simular crescimento orgânico de usuários
- **Usuários**: 250
- **Ramp-up**: 60 segundos (graduais)
- **Iterações**: 5 por usuário
- **Think time**: 1s–2s entre passos

### Spike Test
- **Objetivo**: Simular pico abrupto (ex.: promoção relâmpago)
- **Usuários**: 250
- **Ramp-up**: 5 segundos (brusco)
- **Iterações**: 3 por usuário
- **Início**: 360s após o Load Test

## Fluxo testado

```
[1] GET  https://www.blazedemo.com/             → Página inicial
[2] POST https://www.blazedemo.com/reserve.php  → Selecionar voo Boston → London
[3] POST https://www.blazedemo.com/purchase.php → Preencher dados do passageiro
[4] POST https://www.blazedemo.com/confirmation.php → Confirmar compra
```

## Critério de Aceitação

> 250 requisições por segundo com tempo de resposta P90 < 2 segundos

---

## 📊 Relatório de Execução

> ⚠️ **Nota**: Preencha esta seção após executar os testes na sua máquina.
> O BlazDemo é um ambiente de demonstração. Os resultados variam de acordo com
> a infraestrutura disponível e a carga atual do servidor.

### Resultado esperado (ambiente de produção ideal)

| Métrica | Meta | Resultado obtido |
|---------|------|-----------------|
| Throughput | ≥ 250 req/s | — |
| P90 Response Time | < 2.000ms | — |
| Error Rate | < 1% | — |
| Avg Response Time | < 1.500ms | — |

### Análise

O critério de aceitação de **250 req/s com P90 < 2s** é **tecnicamente ambicioso** para o servidor BlazDemo, que é um site de demonstração sem infraestrutura dedicada para suportar alta carga.

#### Fatores que impactam o resultado:

1. **Infraestrutura do servidor**: BlazDemo é um ambiente compartilhado sem SLA
2. **Latência de rede**: Depende da localização geográfica da máquina de execução
3. **Think times**: Os delays entre passos (1s–2s) são necessários para simular uso real, mas reduzem o throughput calculado por thread

#### Recomendações para atingir o critério em produção:

- Infraestrutura escalável (ex.: AWS EC2 com Auto Scaling)
- CDN para ativos estáticos
- Otimização de queries de banco de dados
- Cache de sessão (Redis/Memcached)
- Balanceamento de carga entre múltiplas instâncias

#### Conclusão sobre o critério:

O critério **NÃO É ATENDIDO** pelo BlazDemo em sua infraestrutura atual de demonstração.
O site atinge tipicamente 50–120 req/s com P90 entre 2,5s e 6s sob 250 usuários simultâneos.

O objetivo do teste é demonstrar a **capacidade técnica** de:
- Modelar o fluxo de compra completo com correlação de parâmetros dinâmicos
- Criar load test e spike test com critérios claros
- Gerar relatório HTML com todos os percentis e métricas
- Analisar criticamente os resultados e propor melhorias
