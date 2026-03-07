# ✅ Configuração de Logging - INFO Level

## Arquivos Configurados

### 1. `src/main/resources/application.properties`
Adicionadas as seguintes configurações:
```properties
# Logging Configuration
logging.level.root=INFO
logging.level.com.polytradings=INFO
logging.level.io.nats=INFO
```

### 2. `src/main/resources/logback.xml` (NOVO)
Criado arquivo de configuração do Logback com:
- **Console Appender**: Logs aparecem no console
- **File Appender**: Logs salvos em `logs/bet-action-engine.log`
- **Rolling Policy**: Rotação diária de logs (mantém 30 dias)
- **Pattern**: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

### 3. `logs/` (NOVO)
Diretório criado para armazenar os arquivos de log

## Níveis de Log Configurados

| Logger | Level | Descrição |
|--------|-------|-----------|
| `root` | INFO | Nível padrão para todas as bibliotecas |
| `com.polytradings.betaction` | INFO | Logs da aplicação |
| `io.nats` | INFO | Logs da biblioteca NATS |

## Logs que Você Verá Agora

Com o nível INFO, você verá:
- ✅ Logs de conexão NATS
- ✅ Logs de subscrição ao tópico
- ✅ Logs de eventos recebidos (`logger.info()`)
- ✅ Logs de mudanças de status
- ✅ Logs de erros (`logger.error()`)

**NÃO verá:**
- ❌ Logs de DEBUG (`logger.debug()`)

## Como Alterar Temporariamente para DEBUG

Se precisar de mais detalhes para diagnosticar problemas, edite `logback.xml`:

```xml
<!-- Para debug geral -->
<root level="DEBUG">
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="FILE" />
</root>

<!-- Ou apenas para sua aplicação -->
<logger name="com.polytradings.betaction" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="FILE" />
</logger>
```

## Localização dos Logs

- **Console**: Logs aparecem durante `gradle run`
- **Arquivo**: `logs/bet-action-engine.log`
- **Arquivos Rotacionados**: `logs/bet-action-engine-YYYY-MM-DD.log`

## Exemplo de Output

```
2026-03-02 10:30:45.123 [main] INFO  c.p.b.config.NatsConfig - Connected to NATS server: nats://localhost:4222
2026-03-02 10:30:45.456 [main] INFO  c.p.b.i.n.NatsMarketEventListener - Subscribed to topic: market.prices.*
2026-03-02 10:30:45.789 [main] INFO  c.p.b.a.BetActionService - BetActionService started successfully
2026-03-02 10:30:50.234 [nats-1] INFO  c.p.b.a.BetActionService - Status changed for BTC: null -> NO_ACTION
```

## Verificação

Para verificar que está funcionando:
```bash
# Rodar a aplicação
gradle run

# Em outro terminal, ver os logs em tempo real
tail -f logs/bet-action-engine.log
```

