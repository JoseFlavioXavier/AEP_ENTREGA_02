USE banco_alimentos;

DELIMITER $$

DROP FUNCTION IF EXISTS fn_dias_para_vencer$$
CREATE FUNCTION fn_dias_para_vencer(p_validade DATE)
RETURNS INT
NOT DETERMINISTIC
NO SQL
BEGIN
    RETURN DATEDIFF(p_validade, CURDATE());
END$$

DROP FUNCTION IF EXISTS fn_situacao_lote$$
CREATE FUNCTION fn_situacao_lote(p_saldo DECIMAL(12,3), p_validade DATE)
RETURNS VARCHAR(20)
NOT DETERMINISTIC
NO SQL
BEGIN
    RETURN CASE
        WHEN p_saldo = 0 THEN 'Esgotado'
        WHEN p_validade < CURDATE() THEN 'Vencido'
        WHEN DATEDIFF(p_validade, CURDATE()) <= 7 THEN 'Vence em até 7 dias'
        ELSE 'OK'
    END;
END$$

DROP PROCEDURE IF EXISTS sp_registrar_doacao$$
CREATE PROCEDURE sp_registrar_doacao(
    IN p_id_doador INT UNSIGNED,
    IN p_itens LONGTEXT,
    OUT p_id_doacao INT UNSIGNED
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_itens IS NULL OR NOT JSON_VALID(p_itens) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Itens da doação inválidos.';
    END IF;
    IF JSON_TYPE(p_itens) <> 'ARRAY' OR JSON_LENGTH(p_itens) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A doação precisa ter pelo menos um item.';
    END IF;

    START TRANSACTION;

    INSERT INTO doacao (id_doador) VALUES (p_id_doador);
    SET p_id_doacao = LAST_INSERT_ID();

    INSERT INTO lote (id_doacao, id_alimento, quantidade_inicial, saldo, validade)
    SELECT p_id_doacao, t.id_alimento, t.quantidade, t.quantidade, t.validade
    FROM JSON_TABLE(p_itens, '$[*]' COLUMNS (
        id_alimento INT UNSIGNED PATH '$.id_alimento',
        quantidade DECIMAL(12,3) PATH '$.quantidade',
        validade DATE PATH '$.validade'
    )) AS t;

    COMMIT;
END$$

DROP PROCEDURE IF EXISTS sp_registrar_distribuicao$$
CREATE PROCEDURE sp_registrar_distribuicao(
    IN p_id_instituicao INT UNSIGNED,
    IN p_itens LONGTEXT,
    OUT p_id_distribuicao INT UNSIGNED
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_itens IS NULL OR NOT JSON_VALID(p_itens) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Itens da distribuição inválidos.';
    END IF;
    IF JSON_TYPE(p_itens) <> 'ARRAY' OR JSON_LENGTH(p_itens) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A distribuição precisa ter pelo menos um item.';
    END IF;

    START TRANSACTION;

    INSERT INTO distribuicao (id_instituicao) VALUES (p_id_instituicao);
    SET p_id_distribuicao = LAST_INSERT_ID();

    INSERT INTO item_distribuicao (id_distribuicao, id_lote, quantidade)
    SELECT p_id_distribuicao, t.id_lote, t.quantidade
    FROM JSON_TABLE(p_itens, '$[*]' COLUMNS (
        id_lote INT UNSIGNED PATH '$.id_lote',
        quantidade DECIMAL(12,3) PATH '$.quantidade'
    )) AS t;

    COMMIT;
END$$

DROP PROCEDURE IF EXISTS sp_registrar_perda$$
CREATE PROCEDURE sp_registrar_perda(
    IN p_id_lote INT UNSIGNED,
    IN p_quantidade DECIMAL(12,3),
    IN p_motivo VARCHAR(200)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    INSERT INTO perda (id_lote, quantidade, motivo) VALUES (p_id_lote, p_quantidade, p_motivo);
    COMMIT;
END$$

DELIMITER ;
