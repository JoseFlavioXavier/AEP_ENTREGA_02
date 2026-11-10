USE banco_alimentos;
SET @id_doador = (SELECT id_doador FROM doador WHERE nome = 'Supermercado Esperança');
SET @id_instituicao = (SELECT id_instituicao FROM instituicao WHERE nome = 'Creche Sol Nascente');
SET @lote_arroz = (SELECT l.id_lote FROM lote l JOIN alimento a ON a.id_alimento = l.id_alimento WHERE a.nome = 'Arroz Tipo 1' ORDER BY l.id_lote LIMIT 1);
SET @lote_banana = (SELECT l.id_lote FROM lote l JOIN alimento a ON a.id_alimento = l.id_alimento WHERE a.nome = 'Banana Prata' ORDER BY l.id_lote LIMIT 1);
SET @saldo_antes = (SELECT saldo FROM lote WHERE id_lote = @lote_arroz);
SET @doacoes_antes = (SELECT COUNT(*) FROM doacao);
SET @distribuicoes_antes = (SELECT COUNT(*) FROM distribuicao);
SELECT 'TESTES DE ERRO: cada cenário abaixo deve ser BLOQUEADO' AS titulo;
DELIMITER $$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '01. Doador com documento de tamanho incompatível com o tipo' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    INSERT INTO doador (nome, tipo, documento, contato) VALUES ('Teste', 'PESSOA_FISICA', '12345678000199', '(43) 0000-0000');
    SELECT '01. Doador com documento de tamanho incompatível com o tipo' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '02. Doador com documento duplicado' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    INSERT INTO doador (nome, tipo, documento, contato) VALUES ('Teste', 'PESSOA_JURIDICA', '12345678000199', '(43) 0000-0000');
    SELECT '02. Doador com documento duplicado' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '03. Doador com nome em branco' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    INSERT INTO doador (nome, tipo, documento, contato) VALUES ('   ', 'PESSOA_FISICA', '98765432100', '(43) 0000-0000');
    SELECT '03. Doador com nome em branco' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '04. Instituição com capacidade zero' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    INSERT INTO instituicao (nome, documento, contato, endereco, capacidade) VALUES ('Teste', '11111111000111', '(43) 0000-0000', 'Rua A', 0);
    SELECT '04. Instituição com capacidade zero' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '05. Alimento com nome repetido (maiúsculas diferentes)' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    INSERT INTO alimento (nome, id_categoria, unidade) VALUES ('ARROZ TIPO 1', 1, 'kg');
    SELECT '05. Alimento com nome repetido (maiúsculas diferentes)' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '06. Alimento com categoria inexistente' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    INSERT INTO alimento (nome, id_categoria, unidade) VALUES ('Teste', 99, 'kg');
    SELECT '06. Alimento com categoria inexistente' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '07. Doação sem itens' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_doacao(@id_doador, '[]', @d);
    SELECT '07. Doação sem itens' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '08. Doação com lote vencido' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_doacao(@id_doador, JSON_ARRAY(JSON_OBJECT('id_alimento', 1, 'quantidade', 5, 'validade', DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y-%m-%d'))), @d);
    SELECT '08. Doação com lote vencido' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '09. Distribuição com estoque insuficiente' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_distribuicao(@id_instituicao, JSON_ARRAY(JSON_OBJECT('id_lote', @lote_arroz, 'quantidade', 99999)), @s);
    SELECT '09. Distribuição com estoque insuficiente' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '10. Distribuição de lote vencido' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_distribuicao(@id_instituicao, JSON_ARRAY(JSON_OBJECT('id_lote', @lote_banana, 'quantidade', 1)), @s);
    SELECT '10. Distribuição de lote vencido' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '11. Distribuição sem itens' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_distribuicao(@id_instituicao, '[]', @s);
    SELECT '11. Distribuição sem itens' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '12. Distribuição para instituição inexistente' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_distribuicao(9999, JSON_ARRAY(JSON_OBJECT('id_lote', @lote_arroz, 'quantidade', 1)), @s);
    SELECT '12. Distribuição para instituição inexistente' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '13. Perda maior que o saldo' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_perda(@lote_arroz, 99999, 'Teste');
    SELECT '13. Perda maior que o saldo' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '14. Perda sem motivo' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_perda(@lote_arroz, 1, '  ');
    SELECT '14. Perda sem motivo' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '15. Alterar a validade de um lote' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    UPDATE lote SET validade = '2031-01-01' WHERE id_lote = @lote_arroz;
    SELECT '15. Alterar a validade de um lote' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '16. Excluir um lote' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    DELETE FROM lote WHERE id_lote = @lote_arroz;
    SELECT '16. Excluir um lote' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '17. Alterar um item de distribuição' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    UPDATE item_distribuicao SET quantidade = 1 LIMIT 1;
    SELECT '17. Alterar um item de distribuição' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '18. Excluir uma perda' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    DELETE FROM perda LIMIT 1;
    SELECT '18. Excluir uma perda' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '19. Excluir doador que já fez doação' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    DELETE FROM doador WHERE id_doador = @id_doador;
    SELECT '19. Excluir doador que já fez doação' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '20. Atomicidade: segundo item inválido desfaz o primeiro' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    CALL sp_registrar_distribuicao(@id_instituicao, JSON_ARRAY(
        JSON_OBJECT('id_lote', @lote_arroz, 'quantidade', 1),
        JSON_OBJECT('id_lote', @lote_arroz, 'quantidade', 99999)), @s);
    SELECT '20. Atomicidade' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
BEGIN NOT ATOMIC
    DECLARE v_mensagem TEXT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensagem = MESSAGE_TEXT;
        SELECT '21. Alterar a unidade de um alimento que já tem lote' AS cenario, 'BLOQUEADO' AS resultado, v_mensagem AS mensagem;
    END;
    UPDATE alimento SET unidade = 'g' WHERE nome = 'Arroz Tipo 1';
    SELECT '21. Alterar a unidade de um alimento que já tem lote' AS cenario, 'FALHOU: nenhum erro foi gerado' AS resultado;
END$$
DELIMITER ;
SELECT 'Conferência após os testes' AS titulo;
SELECT 'saldo do lote de arroz' AS item, @saldo_antes AS antes, saldo AS depois,
       IF(saldo = @saldo_antes, 'PRESERVADO', 'ALTERADO') AS resultado FROM lote WHERE id_lote = @lote_arroz;
SELECT 'quantidade de doações' AS item, @doacoes_antes AS antes, COUNT(*) AS depois,
       IF(COUNT(*) = @doacoes_antes, 'PRESERVADO', 'ALTERADO') AS resultado FROM doacao;
SELECT 'quantidade de distribuições' AS item, @distribuicoes_antes AS antes, COUNT(*) AS depois,
       IF(COUNT(*) = @distribuicoes_antes, 'PRESERVADO', 'ALTERADO') AS resultado FROM distribuicao;
SELECT 'conferência de saldo dos lotes' AS item, conferencia AS resultado, COUNT(*) AS lotes FROM vw_conferencia_saldo GROUP BY conferencia;
