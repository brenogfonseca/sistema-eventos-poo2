ALTER TABLE eventos
    ADD COLUMN criterio_frequencia_certificado TEXT NOT NULL DEFAULT 'PERCENTUAL'
        CHECK (criterio_frequencia_certificado IN ('PERCENTUAL', 'HORAS')),
    ADD COLUMN frequencia_minima_minutos INTEGER NOT NULL DEFAULT 0
        CHECK (frequencia_minima_minutos >= 0),
    ADD COLUMN modo_carga_horaria_certificado TEXT NOT NULL DEFAULT 'DINAMICA'
        CHECK (modo_carga_horaria_certificado IN ('FIXA', 'DINAMICA')),
    ADD COLUMN carga_horaria_fixa_minutos INTEGER NOT NULL DEFAULT 0
        CHECK (carga_horaria_fixa_minutos >= 0);
