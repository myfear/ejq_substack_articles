-- Note: This script runs after Hibernate creates the table
-- Add search_vector column (may fail if column exists, but that's ok)
ALTER TABLE articles ADD COLUMN search_vector tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('english', coalesce(title, '')), 'A')
    ||
    setweight(to_tsvector('english', coalesce(content, '')), 'B')
) STORED;

CREATE INDEX IF NOT EXISTS idx_articles_search_vector ON articles USING GIN (search_vector);