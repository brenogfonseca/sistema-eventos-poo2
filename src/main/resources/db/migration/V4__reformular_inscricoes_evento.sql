CREATE TABLE inscricoes_evento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    evento_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    cancelada INTEGER NOT NULL DEFAULT 0 CHECK (cancelada IN (0, 1)),
    CONSTRAINT uq_inscricao_evento_usuario UNIQUE (evento_id, usuario_id),
    FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

INSERT INTO inscricoes_evento (evento_id, usuario_id, cancelada)
SELECT a.evento_id, i.usuario_id, MIN(i.cancelada)
FROM inscricoes i
JOIN atividades a ON a.id = i.atividade_id
GROUP BY a.evento_id, i.usuario_id;

ALTER TABLE inscricoes
ADD COLUMN inscricao_evento_id INTEGER REFERENCES inscricoes_evento(id) ON DELETE CASCADE;

UPDATE inscricoes
SET inscricao_evento_id = (
    SELECT ie.id
    FROM inscricoes_evento ie
    JOIN atividades a ON a.evento_id = ie.evento_id
    WHERE a.id = inscricoes.atividade_id
      AND ie.usuario_id = inscricoes.usuario_id
);

CREATE INDEX idx_inscricoes_evento_id ON inscricoes(inscricao_evento_id);
CREATE INDEX idx_inscricoes_evento_evento ON inscricoes_evento(evento_id);
CREATE INDEX idx_inscricoes_evento_usuario ON inscricoes_evento(usuario_id);

-- Atividades antigas sem política definida passam a usar check-in único.
UPDATE atividades
SET tipo_frequencia = 'CHECKIN'
WHERE tipo_frequencia IS NULL
   OR tipo_frequencia NOT IN ('CHECKIN', 'ENTRADA_SAIDA', 'MANUAL');
