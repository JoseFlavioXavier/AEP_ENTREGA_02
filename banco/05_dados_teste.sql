USE banco_alimentos;

DELIMITER $$

DROP FUNCTION IF EXISTS fn_id_lote$$
CREATE FUNCTION fn_id_lote(p_id_doacao INT UNSIGNED, p_id_alimento INT UNSIGNED)
RETURNS INT UNSIGNED
NOT DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_id INT UNSIGNED;
    SELECT id_lote INTO v_id
    FROM lote
    WHERE id_doacao = p_id_doacao AND id_alimento = p_id_alimento
    LIMIT 1;
    RETURN v_id;
END$$

DELIMITER ;

INSERT INTO doador (id_doador, nome, tipo, documento, contato) VALUES
    (1, 'Supermercado Esperança', 'PESSOA_JURIDICA', '12345678000199', '(43) 3322-1100'),
    (2, 'Atacadão Boa Safra', 'PESSOA_JURIDICA', '11222333000144', '(43) 3344-5500'),
    (3, 'Padaria Pão Quente', 'PESSOA_JURIDICA', '22333444000155', '(43) 3355-7700'),
    (4, 'Cooperativa Hortifruti Norte do Paraná', 'PESSOA_JURIDICA', '33444555000166', '(43) 3377-9900'),
    (5, 'Maria Aparecida Souza', 'PESSOA_FISICA', '12345678909', '(43) 99888-1234');

INSERT INTO instituicao (id_instituicao, nome, documento, contato, endereco, capacidade) VALUES
    (1, 'Creche Sol Nascente', '98765432000110', '(43) 3333-0000', 'Rua das Flores, 100', 80),
    (2, 'Casa de Acolhimento Esperança Viva', '87654321000121', '(43) 3344-1111', 'Av. Higienópolis, 850', 35),
    (3, 'Cozinha Solidária Prato Cheio', '76543210000132', '(43) 3355-2222', 'Rua Pará, 420', 150),
    (4, 'Creche Pequeno Aprendiz', '65432109000143', '(43) 3366-3333', 'Rua Goiás, 77', 60);

INSERT INTO alimento (id_alimento, nome, id_categoria, unidade) VALUES
    (1, 'Arroz Tipo 1', 1, 'kg'),
    (2, 'Feijão Carioca', 1, 'kg'),
    (3, 'Macarrão Espaguete', 1, 'kg'),
    (4, 'Sardinha em Lata', 2, 'un'),
    (5, 'Leite Integral', 3, 'l'),
    (6, 'Iogurte Natural', 3, 'un'),
    (7, 'Banana Prata', 4, 'kg'),
    (8, 'Tomate', 4, 'kg'),
    (9, 'Frango Congelado', 5, 'kg'),
    (10, 'Óleo de Soja', 7, 'l');

INSERT INTO doacao (id_doador, data_doacao) VALUES (1, NOW() - INTERVAL 65 DAY);
SET @h1 = LAST_INSERT_ID();
INSERT INTO lote (id_doacao, id_alimento, quantidade_inicial, saldo, validade) VALUES
    (@h1, 1, 200, 200, CURDATE() + INTERVAL 300 DAY),
    (@h1, 2, 120, 120, CURDATE() + INTERVAL 250 DAY),
    (@h1, 10, 60, 60, CURDATE() + INTERVAL 200 DAY);

INSERT INTO doacao (id_doador, data_doacao) VALUES (2, NOW() - INTERVAL 40 DAY);
SET @h2 = LAST_INSERT_ID();
INSERT INTO lote (id_doacao, id_alimento, quantidade_inicial, saldo, validade) VALUES
    (@h2, 1, 300, 300, CURDATE() + INTERVAL 280 DAY),
    (@h2, 3, 150, 150, CURDATE() + INTERVAL 320 DAY),
    (@h2, 4, 400, 400, CURDATE() + INTERVAL 500 DAY);

INSERT INTO doacao (id_doador, data_doacao) VALUES (4, NOW() - INTERVAL 35 DAY);
SET @h3 = LAST_INSERT_ID();
INSERT INTO lote (id_doacao, id_alimento, quantidade_inicial, saldo, validade) VALUES
    (@h3, 7, 80, 80, CURDATE() - INTERVAL 30 DAY),
    (@h3, 8, 50, 50, CURDATE() - INTERVAL 20 DAY);

INSERT INTO doacao (id_doador, data_doacao) VALUES (3, NOW() - INTERVAL 12 DAY);
SET @h4 = LAST_INSERT_ID();
INSERT INTO lote (id_doacao, id_alimento, quantidade_inicial, saldo, validade) VALUES
    (@h4, 5, 100, 100, CURDATE() + INTERVAL 4 DAY),
    (@h4, 6, 80, 80, CURDATE() + INTERVAL 6 DAY);

SET @l_arroz_h1 = fn_id_lote(@h1, 1);
SET @l_feijao_h1 = fn_id_lote(@h1, 2);
SET @l_arroz_h2 = fn_id_lote(@h2, 1);
SET @l_macarrao_h2 = fn_id_lote(@h2, 3);
SET @l_sardinha_h2 = fn_id_lote(@h2, 4);
SET @l_banana_h3 = fn_id_lote(@h3, 7);
SET @l_tomate_h3 = fn_id_lote(@h3, 8);
SET @l_leite_h4 = fn_id_lote(@h4, 5);
SET @l_iogurte_h4 = fn_id_lote(@h4, 6);

INSERT INTO distribuicao (id_instituicao, data_distribuicao) VALUES (1, NOW() - INTERVAL 50 DAY);
SET @s1 = LAST_INSERT_ID();
INSERT INTO item_distribuicao (id_distribuicao, id_lote, quantidade) VALUES
    (@s1, @l_arroz_h1, 60),
    (@s1, @l_feijao_h1, 30);

INSERT INTO distribuicao (id_instituicao, data_distribuicao) VALUES (3, NOW() - INTERVAL 30 DAY);
SET @s2 = LAST_INSERT_ID();
INSERT INTO item_distribuicao (id_distribuicao, id_lote, quantidade) VALUES
    (@s2, @l_arroz_h2, 100),
    (@s2, @l_sardinha_h2, 150),
    (@s2, @l_macarrao_h2, 40);

INSERT INTO distribuicao (id_instituicao, data_distribuicao) VALUES (2, NOW() - INTERVAL 8 DAY);
SET @s3 = LAST_INSERT_ID();
INSERT INTO item_distribuicao (id_distribuicao, id_lote, quantidade) VALUES
    (@s3, @l_arroz_h1, 40),
    (@s3, @l_leite_h4, 30);

INSERT INTO perda (id_lote, quantidade, motivo, data_perda) VALUES
    (@l_banana_h3, 30, 'Fruta madura demais', CURDATE() - INTERVAL 20 DAY),
    (@l_tomate_h3, 20, 'Lote vencido', CURDATE() - INTERVAL 15 DAY);

CALL sp_registrar_doacao(1, JSON_ARRAY(
    JSON_OBJECT('id_alimento', 9, 'quantidade', 90,
                'validade', DATE_FORMAT(CURDATE() + INTERVAL 45 DAY, '%Y-%m-%d')),
    JSON_OBJECT('id_alimento', 5, 'quantidade', 150,
                'validade', DATE_FORMAT(CURDATE() + INTERVAL 25 DAY, '%Y-%m-%d'))), @r1);

CALL sp_registrar_doacao(5, JSON_ARRAY(
    JSON_OBJECT('id_alimento', 2, 'quantidade', 30,
                'validade', DATE_FORMAT(CURDATE() + INTERVAL 180 DAY, '%Y-%m-%d'))), @r2);

SET @l_frango_r1 = fn_id_lote(@r1, 9);

CALL sp_registrar_distribuicao(4, JSON_ARRAY(
    JSON_OBJECT('id_lote', @l_arroz_h2, 'quantidade', 80),
    JSON_OBJECT('id_lote', @l_feijao_h1, 'quantidade', 25)), @s4);

CALL sp_registrar_distribuicao(3, JSON_ARRAY(
    JSON_OBJECT('id_lote', @l_leite_h4, 'quantidade', 40),
    JSON_OBJECT('id_lote', @l_iogurte_h4, 'quantidade', 30),
    JSON_OBJECT('id_lote', @l_frango_r1, 'quantidade', 20)), @s5);

CALL sp_registrar_perda(@l_leite_h4, 5, 'Embalagem danificada');

DROP FUNCTION IF EXISTS fn_id_lote;
