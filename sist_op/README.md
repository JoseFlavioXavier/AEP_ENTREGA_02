# Simulador de Sistemas Operacionais (AEP 2026.2)

Simulação em Java do galpão do Banco de Alimentos (recepção, triagem e expedição) e comparação de substituição de cache (FIFO e LRU). Relatório: `docs/relatorio_sist_op/relatorio_sist_op.docx`.

## Como executar
- **IntelliJ:** abra `sist_op/src/sistop/SimuladorSO.java` e execute o `main` (módulo `sist_op`).
- **Terminal:** na pasta `sist_op`, rode `javac -d out src/sistop/*.java` e depois `java -cp out sistop.SimuladorSO`.

## O que cada parte faz
| Arquivo | Conteúdo |
| --- | --- |
| `GalpaoManager.java` | 3 threads (recepção, triagem e expedição). `ReentrantLock` (mutex) protege os contadores e `Semaphore` limita as threads de cada etapa. |
| `CacheEstoque.java` | Cache de capacidade 4 com política FIFO ou LRU, contando hits e misses. |
| `SimuladorSO.java` | Interface Swing com duas abas: simulação do galpão e cache de estoque. |

## Como verificar
1. **Aba "Simulação Galpão":** clique em *Iniciar Simulação*. O monitor mostra o estado de cada thread, e os contadores sempre somam um múltiplo de 5. *Pausar / Parar* encerra as threads.
2. **Aba "Cache de Estoque":** escolha FIFO ou LRU e use *Simular Acesso Sequencial* ou consulte um ID. O log mostra hit ou miss, o conteúdo do cache e os totais.
