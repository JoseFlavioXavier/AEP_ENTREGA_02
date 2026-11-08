# Banco de dados - Banco de Alimentos de Londrina

SGBD usado: MariaDB 10.11 (precisa de 10.6 ou mais, por causa do JSON_TABLE).

## Como executar

Abra o terminal dentro desta pasta e rode:

    mariadb -u root -p --default-character-set=utf8mb4 < 00_executar_tudo.sql

Atenção: o script apaga e recria o banco `banco_alimentos`. Para repetir os testes do zero, basta rodar o comando de novo.

Também dá para executar os arquivos um por um, nesta ordem:

| Arquivo | Conteúdo |
| --- | --- |
| 01_ddl.sql | Banco, tabelas, chaves, restrições, índices e categorias |
| 02_triggers.sql | Saldo do lote, bloqueio de saída inválida e proteção do histórico |
| 03_funcoes_procedures.sql | Funções de validade e procedures de doação, distribuição e perda |
| 04_views.sql | Estoque por validade, doações por período, distribuições por trimestre, indicadores, rastreabilidade e conferência de saldo |
| 05_dados_teste.sql | Dados de teste (rodar uma vez após o 01 a 04) |
| 06_consultas.sql | Consultas SQL de negócio |
| 07_testes_erros.sql | Cenários inválidos que devem ser bloqueados |

As datas de validade dos dados de teste são relativas ao dia da execução, então sempre existem lotes vencidos e lotes que vencem em até 7 dias.
