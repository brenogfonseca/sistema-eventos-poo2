ALTER TABLE certificados
    ALTER COLUMN usuario_id DROP NOT NULL,
    ADD COLUMN pessoa_programacao_id INTEGER
        REFERENCES pessoas_programacao(id) ON DELETE RESTRICT;

ALTER TABLE certificados
    ADD CONSTRAINT ck_certificado_destinatario
        CHECK (num_nonnulls(usuario_id, pessoa_programacao_id) = 1);

ALTER TABLE certificados
    DROP CONSTRAINT uq_certificado_evento_usuario_tipo;

CREATE UNIQUE INDEX uq_certificado_evento_usuario_tipo
    ON certificados(evento_id, usuario_id, tipo)
    WHERE usuario_id IS NOT NULL;

CREATE UNIQUE INDEX uq_certificado_evento_pessoa_tipo
    ON certificados(evento_id, pessoa_programacao_id, tipo)
    WHERE pessoa_programacao_id IS NOT NULL;
