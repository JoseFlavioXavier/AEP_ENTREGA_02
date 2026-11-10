DROP DATABASE IF EXISTS banco_alimentos;
CREATE DATABASE banco_alimentos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE banco_alimentos;

CREATE TABLE doador (
    id_doador INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    tipo VARCHAR(16) NOT NULL,
    documento VARCHAR(14) NOT NULL,
    contato VARCHAR(80) NOT NULL,
    CONSTRAINT pk_doador PRIMARY KEY (id_doador),
    CONSTRAINT uq_doador_documento UNIQUE (documento),
    CONSTRAINT ck_doador_nome CHECK (CHAR_LENGTH(TRIM(nome)) > 0),
    CONSTRAINT ck_doador_contato CHECK (CHAR_LENGTH(TRIM(contato)) > 0),
    CONSTRAINT ck_doador_tipo CHECK (tipo IN ('PESSOA_FISICA', 'PESSOA_JURIDICA')),
    CONSTRAINT ck_doador_documento CHECK (
        documento REGEXP '^[0-9]+$'
        AND ((tipo = 'PESSOA_FISICA' AND CHAR_LENGTH(documento) = 11)
          OR (tipo = 'PESSOA_JURIDICA' AND CHAR_LENGTH(documento) = 14))
    )
) ENGINE = InnoDB;

CREATE TABLE instituicao (
    id_instituicao INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    documento CHAR(14) NOT NULL,
    contato VARCHAR(80) NOT NULL,
    endereco VARCHAR(160) NOT NULL,
    capacidade INT UNSIGNED NOT NULL,
    CONSTRAINT pk_instituicao PRIMARY KEY (id_instituicao),
    CONSTRAINT uq_instituicao_documento UNIQUE (documento),
    CONSTRAINT ck_instituicao_nome CHECK (CHAR_LENGTH(TRIM(nome)) > 0),
    CONSTRAINT ck_instituicao_contato CHECK (CHAR_LENGTH(TRIM(contato)) > 0),
    CONSTRAINT ck_instituicao_endereco CHECK (CHAR_LENGTH(TRIM(endereco)) > 0),
    CONSTRAINT ck_instituicao_documento CHECK (documento REGEXP '^[0-9]{14}$'),
    CONSTRAINT ck_instituicao_capacidade CHECK (capacidade > 0)
) ENGINE = InnoDB;

CREATE TABLE categoria (
    id_categoria TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(40) NOT NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nome UNIQUE (nome)
) ENGINE = InnoDB;

CREATE TABLE alimento (
    id_alimento INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL,
    id_categoria TINYINT UNSIGNED NOT NULL,
    unidade VARCHAR(10) NOT NULL,
    CONSTRAINT pk_alimento PRIMARY KEY (id_alimento),
    CONSTRAINT uq_alimento_nome UNIQUE (nome),
    CONSTRAINT fk_alimento_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_alimento_nome CHECK (CHAR_LENGTH(TRIM(nome)) > 0),
    CONSTRAINT ck_alimento_unidade CHECK (unidade IN ('kg', 'g', 'l', 'ml', 'un'))
) ENGINE = InnoDB;

CREATE TABLE doacao (
    id_doacao INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_doador INT UNSIGNED NOT NULL,
    data_doacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_doacao PRIMARY KEY (id_doacao),
    CONSTRAINT fk_doacao_doador FOREIGN KEY (id_doador)
        REFERENCES doador (id_doador) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE lote (
    id_lote INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_doacao INT UNSIGNED NOT NULL,
    id_alimento INT UNSIGNED NOT NULL,
    quantidade_inicial DECIMAL(12,3) NOT NULL,
    saldo DECIMAL(12,3) NOT NULL,
    validade DATE NOT NULL,
    CONSTRAINT pk_lote PRIMARY KEY (id_lote),
    CONSTRAINT fk_lote_doacao FOREIGN KEY (id_doacao)
        REFERENCES doacao (id_doacao) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_lote_alimento FOREIGN KEY (id_alimento)
        REFERENCES alimento (id_alimento) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_lote_quantidade_inicial CHECK (quantidade_inicial > 0),
    CONSTRAINT ck_lote_saldo CHECK (saldo >= 0 AND saldo <= quantidade_inicial)
) ENGINE = InnoDB;

CREATE TABLE distribuicao (
    id_distribuicao INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_instituicao INT UNSIGNED NOT NULL,
    data_distribuicao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_distribuicao PRIMARY KEY (id_distribuicao),
    CONSTRAINT fk_distribuicao_instituicao FOREIGN KEY (id_instituicao)
        REFERENCES instituicao (id_instituicao) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE item_distribuicao (
    id_item_distribuicao INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_distribuicao INT UNSIGNED NOT NULL,
    id_lote INT UNSIGNED NOT NULL,
    quantidade DECIMAL(12,3) NOT NULL,
    CONSTRAINT pk_item_distribuicao PRIMARY KEY (id_item_distribuicao),
    CONSTRAINT fk_item_distribuicao_distribuicao FOREIGN KEY (id_distribuicao)
        REFERENCES distribuicao (id_distribuicao) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_item_distribuicao_lote FOREIGN KEY (id_lote)
        REFERENCES lote (id_lote) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_item_distribuicao_quantidade CHECK (quantidade > 0)
) ENGINE = InnoDB;

CREATE TABLE perda (
    id_perda INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_lote INT UNSIGNED NOT NULL,
    quantidade DECIMAL(12,3) NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    data_perda DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT pk_perda PRIMARY KEY (id_perda),
    CONSTRAINT fk_perda_lote FOREIGN KEY (id_lote)
        REFERENCES lote (id_lote) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_perda_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_perda_motivo CHECK (CHAR_LENGTH(TRIM(motivo)) > 0)
) ENGINE = InnoDB;

CREATE INDEX idx_doacao_data ON doacao (data_doacao);
CREATE INDEX idx_doacao_doador_data ON doacao (id_doador, data_doacao);
CREATE INDEX idx_lote_validade ON lote (validade);
CREATE INDEX idx_lote_alimento_validade ON lote (id_alimento, validade);
CREATE INDEX idx_lote_doacao ON lote (id_doacao);
CREATE INDEX idx_distribuicao_data ON distribuicao (data_distribuicao);
CREATE INDEX idx_distribuicao_instituicao_data ON distribuicao (id_instituicao, data_distribuicao);
CREATE INDEX idx_item_distribuicao_lote ON item_distribuicao (id_lote);
CREATE INDEX idx_perda_lote ON perda (id_lote);
CREATE INDEX idx_perda_data ON perda (data_perda);

INSERT INTO categoria (nome) VALUES
    ('Grãos'), ('Enlatados'), ('Laticínios'), ('Hortifrúti'),
    ('Proteínas'), ('Bebidas'), ('Outros');
