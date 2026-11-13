# Banco de Alimentos de Londrina (AEP 2026.2)

Atividade de Estudos Programados do 4º semestre de Engenharia de Software (Unicesumar), com as disciplinas de Banco de Dados, Sistemas Operacionais, Programação Orientada a Objetos e Engenharia de Requisitos.

- **Grupo:** Gabriel Castro, José Flávio Xavier, Pedro Lucas Silva Raimundo e Wesley Brene.
- **Versão avaliada:** tag `entrega-2` (2ª entrega, prazo 13/11/2026).
- **Documento completo da AEP:** [`docs/AEP_Banco_de_Alimentos_Londrina.pdf`](docs/AEP_Banco_de_Alimentos_Londrina.pdf), também em [`.docx`](docs/AEP_Banco_de_Alimentos_Londrina.docx). Ele reúne as quatro disciplinas, e cada professor pode ir direto à seção indicada abaixo.

## Mapa do repositório
| Pasta | Conteúdo |
| --- | --- |
| `banco/` | Scripts SQL do banco de dados (MariaDB) |
| `sist_op/` | Simulador de Sistemas Operacionais (Java) |
| `src/` e `test/` | Sistema Java (domínio, serviços, telas Swing) e testes JUnit 5 |
| `docs/` | Documento completo, relatórios parciais por disciplina e diagramas de classes |

## Requisitos para executar
- **Java 21** (o projeto foi executado com o Amazon Corretto 21) e o **IntelliJ IDEA**.
- **MariaDB 10.6 ou superior**, apenas para o banco de dados (os scripts usam recursos específicos do MariaDB, então não rodam no MySQL).
- **JUnit 5**, para os testes da pasta `test`.

---

## Professor Leonardo Rocha: Banco de Dados e Sistemas Operacionais

### Banco de Dados
**Documento:** [`docs/banco_de_dados/banco_de_dados.docx`](docs/banco_de_dados/banco_de_dados.docx) ou a seção 2.1 do documento completo.

**O que verificar, conforme o enunciado (2ª entrega):**
| Item | Onde |
| --- | --- |
| a) Modelo físico e DDL (tabelas, índices, FKs, CHECK/UNIQUE) | `banco/01_ddl.sql` e seções 2.1.4 a 2.1.6 |
| b) Views, procedures/functions e triggers | `banco/02_triggers.sql`, `03_funcoes_procedures.sql`, `04_views.sql` e seção 2.1.7 |
| c) Dados de teste (DML) | `banco/05_dados_teste.sql` e seção 2.1.8 |
| d) Consultas SQL de negócio | `banco/06_consultas.sql` e seção 2.1.9 |
| Testes de integridade (21 cenários de erro) | `banco/07_testes_erros.sql` e seção 2.1.10 |

**Como executar:**
1. Inicie o serviço, por exemplo `sudo systemctl start mariadb` (no Windows, inicie o serviço MariaDB).
2. Entre na pasta do banco, **obrigatoriamente**, porque o script chama os demais arquivos por caminho relativo:
   ```
   cd banco
   sudo mariadb -t --default-character-set=utf8mb4 < 00_executar_tudo.sql
   ```
   No Windows: `mariadb -u root -p -t --default-character-set=utf8mb4 < 00_executar_tudo.sql`.
3. O script apaga e recria o banco `banco_alimentos`, executa os 7 arquivos em ordem e mostra na tela as 12 consultas e os 21 cenários de erro. **Resultado esperado:** todos os cenários de erro aparecem como `BLOQUEADO`, e a conferência final mostra `PRESERVADO` e `OK`.
4. Para explorar depois:
   ```
   sudo mariadb banco_alimentos -e "SHOW TABLES; SHOW TRIGGERS; SHOW PROCEDURE STATUS WHERE Db='banco_alimentos';"
   ```

### Sistemas Operacionais
**Documento:** [`docs/sistemas_operacionais/relatorio_sist_op.docx`](docs/sistemas_operacionais/relatorio_sist_op.docx) ou a seção 2.2 do documento completo. O código está em `sist_op/` (veja também `sist_op/README.md`).

**O que verificar, conforme o enunciado (2ª entrega):**
| Item | Onde |
| --- | --- |
| Simulação do galpão com mutex e semáforo, sem condição de corrida nem deadlock | `sist_op/src/sistop/GalpaoManager.java` e seção 2.2.5 |
| Simulação de substituição de páginas (LRU contra FIFO) no cache | `sist_op/src/sistop/CacheEstoque.java` e seções 2.2.3 e 2.2.6 |

**Como executar:**
- **IntelliJ:** abra `sist_op/src/sistop/SimuladorSO.java` e execute o `main` (ícone verde ao lado do método).
- **Terminal:**
  ```
  cd sist_op
  javac -d out src/sistop/*.java
  java -cp out sistop.SimuladorSO
  ```
- **Aba "Simulação Galpão":** clique em *Iniciar Simulação*. O monitor mostra o estado das três threads, e a soma das caixas na recepção, triadas e expedidas é sempre múltiplo de 5. *Pausar / Parar* encerra as threads.
- **Aba "Cache de Estoque":** escolha FIFO ou LRU e use *Simular Acesso Sequencial* ou consulte um ID. O log mostra cada hit e miss, o conteúdo do cache e os totais.

---

## Professor Cesar Malvezi: Programação Orientada a Objetos
**Documento:** [`docs/poo/programacao_orientada_a_objetos.docx`](docs/poo/programacao_orientada_a_objetos.docx) ou a seção 2.3 do documento completo. O código está em `src/` e `test/`, e os diagramas, em `docs/diagramas/`.

**O que verificar, conforme o enunciado (2ª entrega):**
| Item | Onde |
| --- | --- |
| A) Diagrama de classes refatorado e descrição das alterações | `docs/diagramas/` e seção 2.3.6 |
| B) Implementação das classes e associações | `src/` e seção 2.3.7 |
| C) Herança, polimorfismo, sobrecarga, interfaces e classes abstratas | `src/` e quadro da seção 2.3.8 |
| D) Coleções, exceções e validações | `src/` e seção 2.3.9 |
| E) Interface gráfica integrada às regras de negócio | classe `App` e seção 2.3.10 |
| F) Fluxo CRUD completo (Doador, Instituição, Alimento e Doação) | abas da interface e seção 2.3.11 |
| G) Testes de cenários válidos, inválidos, estoque insuficiente e lote vencido | `test/` e seção 2.3.12 |
| H) Documentação final e código-fonte | este repositório e seção 2.3.13 |

**Como executar:**
- **Interface gráfica (Swing):** abra `src/App.java` no IntelliJ e execute o `main`. A janela tem seis abas: Doadores, Instituições, Alimentos, Doação, Distribuição e Estoque. Sugestão de roteiro: cadastre um doador, uma instituição e um alimento; registre uma doação com um ou mais itens; faça uma distribuição; confira o saldo na aba Estoque e registre uma perda.
- **Demonstração no console:** execute o `main` da classe `Main` (`src/Main.java`). Ela usa os mesmos serviços, sem interface gráfica.
- **Testes automatizados (JUnit 5, 45 testes):** clique com o botão direito na pasta `test` e escolha *Run 'All Tests'*. **Resultado esperado:** 45 aprovados. Se a pasta `test` não aparecer como pasta de testes, use *Mark Directory as > Test Sources Root*; se os imports do `org.junit` ficarem em vermelho, adicione o JUnit 5 ao projeto (*Alt+Enter* sobre o `@Test`, depois *Add 'JUnit5.x' to classpath*).
- **Pelo terminal (somente a aplicação):**
  ```
  javac -d out src/*.java
  java -cp out App
  ```

**Observação:** os dados da aplicação ficam em memória e são perdidos ao fechar o programa. O banco de dados da seção 2.1 não está ligado à aplicação Java, mas o acesso passa pela interface `CrudRepository`, o que permite acrescentar uma implementação com banco sem alterar as telas nem as regras.

---

## Professor Luiz Nunes: Engenharia de Requisitos
**Documento:** [`docs/engenharia_de_requisitos/engenharia_de_requisitos.pdf`](docs/engenharia_de_requisitos/engenharia_de_requisitos.pdf) ou a seção 2.4 do documento completo. Não há nada a executar nesta disciplina.

**O que verificar, conforme o enunciado (2ª entrega):**
| Item | Onde, no documento completo |
| --- | --- |
| a) ERS revisada e consolidada para o MVP | seções 2.4.1 a 2.4.5 |
| b) Casos de uso detalhados (fluxos, exceções e aceite) | seção 2.4.6 (UC-01 a UC-12) |
| c) Priorização MoSCoW, com valor, risco e dependências | seção 2.4.3 |
| d) Matriz de rastreabilidade (requisitos, regras, casos de uso e funcionalidades implementadas) | seção 2.4.7 (matriz, situação de cada requisito e verificação dos casos de teste) |
| e) Validação por cenários, protótipos e critérios de aceite | seção 2.4.9 |
| f) Gestão de mudanças, com justificativas, impactos e versões | seção 2.4.10 |

**Para conferir a rastreabilidade com o que foi construído:** os requisitos que existem no sistema estão no Quadro de situação da seção 2.4.7, com a indicação da classe, da tela ou do objeto de banco correspondente. As diferenças entre a ERS e a implementação estão registradas na seção 2.4.8.
