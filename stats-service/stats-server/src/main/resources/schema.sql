DROP TABLE IF EXISTS apps, hits;

CREATE TABLE IF NOT EXISTS apps
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255)  NOT NULL
);

CREATE TABLE IF NOT EXISTS hits
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app_id    BIGINT        NOT NULL,
    ip        VARCHAR(46)   NOT NULL,
    uri       VARCHAR(2048) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_hits_to_apps FOREIGN KEY (app_id) REFERENCES apps (id) ON DELETE CASCADE
);