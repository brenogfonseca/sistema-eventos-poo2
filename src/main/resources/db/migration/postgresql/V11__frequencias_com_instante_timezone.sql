-- Horários anteriores eram LocalDateTime; converte cada registro usando o fuso do evento.
ALTER TABLE frequencias ADD COLUMN data_hora_instant TIMESTAMPTZ;

UPDATE frequencias f
SET data_hora_instant = CASE
    WHEN f.data_hora LIKE '%+__:%' OR upper(f.data_hora) LIKE '%Z'
        THEN f.data_hora::TIMESTAMPTZ
    ELSE replace(f.data_hora, 'T', ' ')::TIMESTAMP
         AT TIME ZONE COALESCE(e.fuso_horario, 'America/Sao_Paulo')
END
FROM inscricoes i
JOIN inscricoes_evento ie ON ie.id = i.inscricao_evento_id
JOIN eventos e ON e.id = ie.evento_id
WHERE f.inscricao_id = i.id;

ALTER TABLE frequencias DROP COLUMN data_hora;
ALTER TABLE frequencias RENAME COLUMN data_hora_instant TO data_hora;
ALTER TABLE frequencias ALTER COLUMN data_hora SET NOT NULL;

CREATE INDEX idx_frequencias_inscricao_instante
    ON frequencias(inscricao_id, data_hora, id);
