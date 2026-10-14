CREATE TABLE tipos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE
);

CREATE TABLE trilhas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE
);

-- Liga a atividade a um tipo cadastrado.
ALTER TABLE atividades
ADD COLUMN tipo_id INTEGER REFERENCES tipos(id);
