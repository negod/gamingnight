CREATE TABLE items (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
