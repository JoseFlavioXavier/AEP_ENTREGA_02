USE banco_alimentos;

CREATE OR REPLACE VIEW vw_estoque_validade AS
SELECT l.id_lote,
       a.nome AS alimento,
       c.nome AS categoria,
       a.unidade,
       d.nome AS doador_origem,
       l.quantidade_inicial,
       l.saldo,
       l.validade,
       fn_dias_para_vencer(l.validade) AS dias_para_vencer,
       fn_situacao_lote(l.saldo, l.validade) AS situacao
FROM lote l
JOIN alimento a ON a.id_alimento = l.id_alimento
JOIN categoria c ON c.id_categoria = a.id_categoria
JOIN doacao o ON o.id_doacao = l.id_doacao
JOIN doador d ON d.id_doador = o.id_doador
ORDER BY l.validade, a.nome;

CREATE OR REPLACE VIEW vw_doacoes_periodo AS
SELECT DATE_FORMAT(o.data_doacao, '%Y-%m') AS ano_mes,
       a.unidade,
       COUNT(DISTINCT o.id_doacao) AS qtd_doacoes,
       COUNT(l.id_lote) AS qtd_lotes,
       SUM(l.quantidade_inicial) AS quantidade_total
FROM doacao o
JOIN lote l ON l.id_doacao = o.id_doacao
JOIN alimento a ON a.id_alimento = l.id_alimento
GROUP BY DATE_FORMAT(o.data_doacao, '%Y-%m'), a.unidade
ORDER BY ano_mes DESC, a.unidade;

CREATE OR REPLACE VIEW vw_distribuicoes_trimestre AS
SELECT YEAR(d.data_distribuicao) AS ano,
       QUARTER(d.data_distribuicao) AS trimestre,
       i.nome AS instituicao,
       a.unidade,
       COUNT(DISTINCT d.id_distribuicao) AS qtd_distribuicoes,
       SUM(it.quantidade) AS quantidade_total
FROM distribuicao d
JOIN instituicao i ON i.id_instituicao = d.id_instituicao
JOIN item_distribuicao it ON it.id_distribuicao = d.id_distribuicao
JOIN lote l ON l.id_lote = it.id_lote
JOIN alimento a ON a.id_alimento = l.id_alimento
GROUP BY YEAR(d.data_distribuicao), QUARTER(d.data_distribuicao), i.nome, a.unidade
ORDER BY ano DESC, trimestre DESC, i.nome, a.unidade;

CREATE OR REPLACE VIEW vw_indicadores_categoria AS
SELECT c.nome AS categoria,
       a.unidade,
       SUM(l.quantidade_inicial) AS doado,
       SUM(COALESCE(s.total, 0)) AS distribuido,
       SUM(COALESCE(p.total, 0)) AS perdido,
       SUM(l.saldo) AS saldo_atual,
       ROUND(SUM(COALESCE(s.total, 0)) / SUM(l.quantidade_inicial) * 100, 1) AS giro_percentual,
       ROUND(SUM(COALESCE(p.total, 0)) / SUM(l.quantidade_inicial) * 100, 1) AS perda_percentual
FROM lote l
JOIN alimento a ON a.id_alimento = l.id_alimento
JOIN categoria c ON c.id_categoria = a.id_categoria
LEFT JOIN (SELECT id_lote, SUM(quantidade) AS total FROM item_distribuicao GROUP BY id_lote) s
       ON s.id_lote = l.id_lote
LEFT JOIN (SELECT id_lote, SUM(quantidade) AS total FROM perda GROUP BY id_lote) p
       ON p.id_lote = l.id_lote
GROUP BY c.nome, a.unidade
ORDER BY c.nome, a.unidade;

CREATE OR REPLACE VIEW vw_rastreabilidade_lote AS
SELECT l.id_lote,
       a.nome AS alimento,
       d.nome AS doador_origem,
       o.data_doacao,
       l.validade,
       'DISTRIBUIÇÃO' AS movimento,
       i.nome AS destino_ou_motivo,
       dist.data_distribuicao AS data_movimento,
       it.quantidade
FROM item_distribuicao it
JOIN distribuicao dist ON dist.id_distribuicao = it.id_distribuicao
JOIN instituicao i ON i.id_instituicao = dist.id_instituicao
JOIN lote l ON l.id_lote = it.id_lote
JOIN alimento a ON a.id_alimento = l.id_alimento
JOIN doacao o ON o.id_doacao = l.id_doacao
JOIN doador d ON d.id_doador = o.id_doador
UNION ALL
SELECT l.id_lote,
       a.nome,
       d.nome,
       o.data_doacao,
       l.validade,
       'PERDA',
       p.motivo,
       p.data_perda,
       p.quantidade
FROM perda p
JOIN lote l ON l.id_lote = p.id_lote
JOIN alimento a ON a.id_alimento = l.id_alimento
JOIN doacao o ON o.id_doacao = l.id_doacao
JOIN doador d ON d.id_doador = o.id_doador;

CREATE OR REPLACE VIEW vw_conferencia_saldo AS
SELECT l.id_lote,
       l.quantidade_inicial,
       COALESCE(s.total, 0) AS distribuido,
       COALESCE(p.total, 0) AS perdido,
       l.saldo AS saldo_gravado,
       l.quantidade_inicial - COALESCE(s.total, 0) - COALESCE(p.total, 0) AS saldo_recalculado,
       CASE
           WHEN l.saldo = l.quantidade_inicial - COALESCE(s.total, 0) - COALESCE(p.total, 0) THEN 'OK'
           ELSE 'DIVERGENTE'
       END AS conferencia
FROM lote l
LEFT JOIN (SELECT id_lote, SUM(quantidade) AS total FROM item_distribuicao GROUP BY id_lote) s
       ON s.id_lote = l.id_lote
LEFT JOIN (SELECT id_lote, SUM(quantidade) AS total FROM perda GROUP BY id_lote) p
       ON p.id_lote = l.id_lote;
