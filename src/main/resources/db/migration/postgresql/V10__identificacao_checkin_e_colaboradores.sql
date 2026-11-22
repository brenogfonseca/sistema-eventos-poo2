-- Organização da portaria e autorização restrita a cada atividade.
ALTER TABLE atividades
    ADD COLUMN metodo_identificacao_checkin TEXT NOT NULL DEFAULT 'QR'
        CHECK (metodo_identificacao_checkin IN ('QR', 'NOME_CPF'));

-- CPF é opcional para contas antigas e pode ser usado junto com o nome na busca.
ALTER TABLE usuarios
    ADD COLUMN cpf TEXT,
    ADD CONSTRAINT ck_usuario_cpf_formato
        CHECK (cpf IS NULL OR cpf ~ '^[0-9]{11}$');

CREATE UNIQUE INDEX uq_usuario_cpf
    ON usuarios(cpf)
    WHERE cpf IS NOT NULL;

-- Um colaborador só recebe permissão para atividades explicitamente atribuídas.
CREATE TABLE vinculos_colaboradores (
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    atividade_id INTEGER NOT NULL REFERENCES atividades(id) ON DELETE CASCADE,
    vinculado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, atividade_id)
);

CREATE INDEX idx_vinculos_colaboradores_atividade
    ON vinculos_colaboradores(atividade_id, usuario_id);

-- Protege check-in/entrada/saída contra leituras duplicadas concorrentes.
CREATE UNIQUE INDEX uq_frequencia_marcacao_portaria
    ON frequencias(inscricao_id, tipo, origem)
    WHERE origem IN ('QR_CHECKIN', 'QR_ENTRADA', 'QR_SAIDA',
                     'BUSCA_CHECKIN', 'BUSCA_ENTRADA', 'BUSCA_SAIDA',
                     'QR_MANUAL', 'BUSCA_MANUAL');
