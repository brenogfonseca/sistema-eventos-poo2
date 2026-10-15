CREATE TABLE vinculos_organizadores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    evento_id INTEGER NOT NULL,
    criador_principal INTEGER NOT NULL DEFAULT 0
        CHECK (criador_principal IN (0, 1)),
    CONSTRAINT uq_vinculo_usuario_evento
        UNIQUE (usuario_id, evento_id),
    CONSTRAINT fk_vinculo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vinculo_evento
        FOREIGN KEY (evento_id)
        REFERENCES eventos(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_vinculos_organizadores_evento
    ON vinculos_organizadores(evento_id);

CREATE INDEX idx_vinculos_organizadores_usuario
    ON vinculos_organizadores(usuario_id);
