USE banco_alimentos;

SELECT '1. Alimentos mais doados no mês' AS consulta;
SELECT a.nome AS alimento,
       a.unidade,
       SUM(l.quantidade_inicial) AS total_doado
FROM doacao o
JOIN lote l ON l.id_doacao = o.id_doacao
JOIN alimento a ON a.id_alimento = l.id_alimento
WHERE YEAR(o.data_doacao) = YEAR(CURDATE())
  AND MONTH(o.data_doacao) = MONTH(CURDATE())
GROUP BY a.id_alimento, a.nome, a.unidade
ORDER BY a.unidade, total_doado DESC;

SELECT '2. Saldo por lote e dias até o vencimento' AS consulta;
SELECT id_lote, alimento, unidade, saldo, validade, dias_para_vencer, situacao
FROM vw_estoque_validade
WHERE saldo > 0
ORDER BY dias_para_vencer;

SELECT '3. Distribuição por instituição no trimestre' AS consulta;
SELECT instituicao, unidade, qtd_distribuicoes, quantidade_total
FROM vw_distribuicoes_trimestre
WHERE ano = YEAR(CURDATE()) AND trimestre = QUARTER(CURDATE())
ORDER BY instituicao, unidade;

SELECT '4. Lotes que vencem em até 7 dias (alerta)' AS consulta;
SELECT id_lote, alimento, doador_origem, saldo, unidade, validade, dias_para_vencer
FROM vw_estoque_validade
WHERE saldo > 0 AND dias_para_vencer BETWEEN 0 AND 7
ORDER BY validade;

SELECT '5. Lotes vencidos com saldo (perda a registrar)' AS consulta;
SELECT id_lote, alimento, doador_origem, saldo, unidade, validade, dias_para_vencer
FROM vw_estoque_validade
WHERE saldo > 0 AND dias_para_vencer < 0
ORDER BY validade;

SELECT '6. Saldo atual por categoria' AS consulta;
SELECT categoria, unidade, SUM(saldo) AS saldo_total, COUNT(*) AS lotes_com_saldo
FROM vw_estoque_validade
WHERE saldo > 0 AND dias_para_vencer >= 0
GROUP BY categoria, unidade
ORDER BY categoria, unidade;

SELECT '7. Giro e perdas por categoria' AS consulta;
SELECT categoria, unidade, doado, distribuido, perdido, saldo_atual, giro_percentual, perda_percentual
FROM vw_indicadores_categoria;

SELECT '8. Doações por período' AS consulta;
SELECT ano_mes, unidade, qtd_doacoes, qtd_lotes, quantidade_total
FROM vw_doacoes_periodo;

SELECT '9. Ranking de doadores' AS consulta;
SELECT d.nome AS doador,
       COUNT(DISTINCT o.id_doacao) AS qtd_doacoes,
       COUNT(l.id_lote) AS qtd_lotes,
       MAX(o.data_doacao) AS ultima_doacao
FROM doador d
JOIN doacao o ON o.id_doador = d.id_doador
JOIN lote l ON l.id_doacao = o.id_doacao
GROUP BY d.id_doador, d.nome
ORDER BY qtd_doacoes DESC, qtd_lotes DESC, d.nome;

SELECT '10. Instituições sem recebimento nos últimos 30 dias' AS consulta;
SELECT i.nome AS instituicao, i.capacidade, MAX(d2.data_distribuicao) AS ultimo_recebimento
FROM instituicao i
LEFT JOIN distribuicao d ON d.id_instituicao = i.id_instituicao
       AND d.data_distribuicao >= NOW() - INTERVAL 30 DAY
LEFT JOIN distribuicao d2 ON d2.id_instituicao = i.id_instituicao
WHERE d.id_distribuicao IS NULL
GROUP BY i.id_instituicao, i.nome, i.capacidade
ORDER BY i.nome;

SELECT '11. Rastreabilidade do leite (origem, destinos e perdas)' AS consulta;
SELECT id_lote, alimento, doador_origem, validade, movimento, destino_ou_motivo, data_movimento, quantidade
FROM vw_rastreabilidade_lote
WHERE alimento = 'Leite Integral'
ORDER BY id_lote, data_movimento;

SELECT '12. Conferência do saldo gravado nos lotes' AS consulta;
SELECT conferencia, COUNT(*) AS lotes
FROM vw_conferencia_saldo
GROUP BY conferencia;
