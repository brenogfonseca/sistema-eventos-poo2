ALTER TABLE atividades
    ADD CONSTRAINT fk_atividade_local
        FOREIGN KEY (local_id)
        REFERENCES locais(id)
        ON DELETE SET NULL;

CREATE INDEX idx_atividades_local
    ON atividades(local_id)
    WHERE local_id IS NOT NULL;
