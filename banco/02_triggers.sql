USE banco_alimentos;

DELIMITER $$

DROP TRIGGER IF EXISTS trg_lote_bi$$
CREATE TRIGGER trg_lote_bi BEFORE INSERT ON lote
FOR EACH ROW
BEGIN
    DECLARE v_data_doacao DATETIME;
    SELECT data_doacao INTO v_data_doacao FROM doacao WHERE id_doacao = NEW.id_doacao;
    IF NEW.validade < DATE(v_data_doacao) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lote vencido: a validade é anterior à data da doação.';
    END IF;
    SET NEW.saldo = NEW.quantidade_inicial;
END$$

DROP TRIGGER IF EXISTS trg_lote_bu$$
CREATE TRIGGER trg_lote_bu BEFORE UPDATE ON lote
FOR EACH ROW
BEGIN
    IF NEW.id_doacao <> OLD.id_doacao
       OR NEW.id_alimento <> OLD.id_alimento
       OR NEW.quantidade_inicial <> OLD.quantidade_inicial
       OR NEW.validade <> OLD.validade THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Os dados de origem do lote não podem ser alterados.';
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_lote_bd$$
CREATE TRIGGER trg_lote_bd BEFORE DELETE ON lote
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Lotes não podem ser excluídos: a origem dos alimentos deve ser preservada.';
END$$

DROP TRIGGER IF EXISTS trg_item_distribuicao_bi$$
CREATE TRIGGER trg_item_distribuicao_bi BEFORE INSERT ON item_distribuicao
FOR EACH ROW
BEGIN
    DECLARE v_saldo DECIMAL(12,3);
    DECLARE v_validade DATE;
    DECLARE v_data_saida DATETIME;
    DECLARE v_mensagem VARCHAR(128);
    SELECT saldo, validade INTO v_saldo, v_validade
    FROM lote WHERE id_lote = NEW.id_lote FOR UPDATE;
    SELECT data_distribuicao INTO v_data_saida
    FROM distribuicao WHERE id_distribuicao = NEW.id_distribuicao;
    IF v_validade < DATE(v_data_saida) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lote vencido: a validade é anterior à data da saída.';
    END IF;
    IF NEW.quantidade > v_saldo THEN
        SET v_mensagem = CONCAT('Estoque insuficiente no lote ', NEW.id_lote,
            ': saldo ', v_saldo, ', pedido ', NEW.quantidade);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_mensagem;
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_item_distribuicao_ai$$
CREATE TRIGGER trg_item_distribuicao_ai AFTER INSERT ON item_distribuicao
FOR EACH ROW
BEGIN
    UPDATE lote SET saldo = saldo - NEW.quantidade WHERE id_lote = NEW.id_lote;
END$$

DROP TRIGGER IF EXISTS trg_item_distribuicao_bu$$
CREATE TRIGGER trg_item_distribuicao_bu BEFORE UPDATE ON item_distribuicao
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Itens de distribuição não podem ser alterados.';
END$$

DROP TRIGGER IF EXISTS trg_item_distribuicao_bd$$
CREATE TRIGGER trg_item_distribuicao_bd BEFORE DELETE ON item_distribuicao
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Itens de distribuição não podem ser excluídos.';
END$$

DROP TRIGGER IF EXISTS trg_perda_bi$$
CREATE TRIGGER trg_perda_bi BEFORE INSERT ON perda
FOR EACH ROW
BEGIN
    DECLARE v_saldo DECIMAL(12,3);
    DECLARE v_mensagem VARCHAR(128);
    SELECT saldo INTO v_saldo FROM lote WHERE id_lote = NEW.id_lote FOR UPDATE;
    IF NEW.quantidade > v_saldo THEN
        SET v_mensagem = CONCAT('Estoque insuficiente no lote ', NEW.id_lote,
            ': saldo ', v_saldo, ', perda ', NEW.quantidade);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_mensagem;
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_perda_ai$$
CREATE TRIGGER trg_perda_ai AFTER INSERT ON perda
FOR EACH ROW
BEGIN
    UPDATE lote SET saldo = saldo - NEW.quantidade WHERE id_lote = NEW.id_lote;
END$$

DROP TRIGGER IF EXISTS trg_perda_bu$$
CREATE TRIGGER trg_perda_bu BEFORE UPDATE ON perda
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Perdas registradas não podem ser alteradas.';
END$$

DROP TRIGGER IF EXISTS trg_perda_bd$$
CREATE TRIGGER trg_perda_bd BEFORE DELETE ON perda
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Perdas registradas não podem ser excluídas.';
END$$

DELIMITER ;
