-- This ensures the vector extension is available in the database.
CREATE EXTENSION IF NOT EXISTS vector;

-- Create an IVFFlat index on the embedding column of the verse table.
-- We use 'vector_cosine_ops' because we want to measure similarity using cosine distance.
-- CREATE INDEX ON verse USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

CREATE INDEX ON verse USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);