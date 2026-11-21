ALTER TABLE eventos ADD COLUMN prazo_cancelamento TEXT;

CREATE INDEX idx_eventos_prazo_cancelamento
    ON eventos(prazo_cancelamento)
    WHERE prazo_cancelamento IS NOT NULL;
