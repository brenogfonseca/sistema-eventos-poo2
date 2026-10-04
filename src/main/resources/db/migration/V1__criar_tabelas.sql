CREATE TABLE eventos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo TEXT NOT NULL,
    descricao TEXT,
    inicio TEXT NOT NULL,
    fim TEXT NOT NULL,
    capacidade_maxima INTEGER NOT NULL CHECK (capacidade_maxima > 0),
    inscricoes_abertas INTEGER NOT NULL DEFAULT 0
        CHECK (inscricoes_abertas IN (0, 1)),
    deletado INTEGER NOT NULL DEFAULT 0
        CHECK (deletado IN (0, 1)),
    certificado INTEGER NOT NULL DEFAULT 0
        CHECK (certificado IN (0, 1)),
    frequencia_minima INTEGER NOT NULL DEFAULT 75
        CHECK (frequencia_minima BETWEEN 0 AND 100),
    CHECK (inicio <= fim)
);

CREATE TABLE atividades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    evento_id INTEGER NOT NULL,
    local_id INTEGER,
    local_nome TEXT,
    hora_inicio TEXT NOT NULL,
    hora_fim TEXT NOT NULL,
    data TEXT NOT NULL,
    controla_vagas INTEGER NOT NULL
        CHECK (controla_vagas IN (0, 1)),
    vagas INTEGER NOT NULL CHECK (vagas >= 0),
    tipo TEXT,
    tipo_frequencia TEXT,
    trilha_id INTEGER,
    trilha_nome TEXT,
    CONSTRAINT fk_atividade_evento
        FOREIGN KEY (evento_id)
        REFERENCES eventos(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_atividades_evento
    ON atividades(evento_id);
