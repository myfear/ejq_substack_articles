CREATE TABLE post (
    id          BIGSERIAL PRIMARY KEY,
    uri         TEXT    NOT NULL,  -- AT Protocol URI of the post
    text        TEXT    NOT NULL,  -- Post content text
    createdat  TIMESTAMP WITH TIME ZONE NOT NULL,
    hourofday INT     NOT NULL,  -- Hour (0-23) the post was created (UTC)
    hashtags    TEXT,              -- Comma-separated hashtags in the post
    links       TEXT,              -- Comma-separated links in the post
    frameworks  TEXT,              -- Comma-separated tech libraries mentioned
    language    VARCHAR(8),        -- Detected language code (e.g. 'en')
    indexedat  TIMESTAMP WITH TIME ZONE DEFAULT now() -- (indexedat is when we saved the post; could help with pagination or TTL policies)

);

create sequence Post_SEQ start with 1 increment by 50;