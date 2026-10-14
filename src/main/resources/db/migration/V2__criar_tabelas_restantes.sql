CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    email TEXT NOT NULL COLLATE NOCASE UNIQUE,
    senha_hash TEXT NOT NULL,
    perfil TEXT NOT NULL
        CHECK (perfil IN ('ADMINISTRADOR', 'USUARIO', 'VISITANTE'))
);

CREATE TABLE locais (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL
);

CREATE TABLE inscricoes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    atividade_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    cancelada INTEGER NOT NULL DEFAULT 0
        CHECK (cancelada IN (0, 1)),
    CONSTRAINT fk_inscricao_atividade
        FOREIGN KEY (atividade_id)
        REFERENCES atividades(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_inscricao_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_inscricoes_atividade
    ON inscricoes(atividade_id);

CREATE INDEX idx_inscricoes_usuario
    ON inscricoes(usuario_id);

CREATE TABLE frequencias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data_hora TEXT NOT NULL,
    origem TEXT NOT NULL,
    responsavel_id INTEGER,
    presente INTEGER NOT NULL CHECK (presente IN (0, 1)),
    inscricao_id INTEGER NOT NULL,
    tipo TEXT NOT NULL
        CHECK (tipo IN ('CHECKIN', 'ENTRADA_SAIDA', 'MANUAL')),
    CONSTRAINT fk_frequencia_inscricao
        FOREIGN KEY (inscricao_id)
        REFERENCES inscricoes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_frequencia_responsavel
        FOREIGN KEY (responsavel_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_frequencias_inscricao
    ON frequencias(inscricao_id);

CREATE TABLE questionarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    atividade_id INTEGER NOT NULL UNIQUE,
    titulo TEXT NOT NULL,
    CONSTRAINT fk_questionario_atividade
        FOREIGN KEY (atividade_id)
        REFERENCES atividades(id)
        ON DELETE CASCADE
);

CREATE TABLE questoes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    enunciado TEXT NOT NULL,
    tipo TEXT NOT NULL
        CHECK (tipo IN ('TEXTUAL', 'ESCOLHA_UNICA', 'ESCALA_NUMERICA')),
    questionario_id INTEGER NOT NULL,
    CONSTRAINT fk_questao_questionario
        FOREIGN KEY (questionario_id)
        REFERENCES questionarios(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_questoes_questionario
    ON questoes(questionario_id);

CREATE TABLE respostas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    questao_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    valor TEXT NOT NULL,
    CONSTRAINT fk_resposta_questao
        FOREIGN KEY (questao_id)
        REFERENCES questoes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_resposta_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_respostas_questao
    ON respostas(questao_id);

CREATE INDEX idx_respostas_usuario
    ON respostas(usuario_id);
