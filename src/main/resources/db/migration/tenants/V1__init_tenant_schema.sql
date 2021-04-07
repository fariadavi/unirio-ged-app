CREATE TABLE category (
    id			BIGINT GENERATED ALWAYS AS IDENTITY,
    name       	VARCHAR(40) NOT NULL,
    parent_id      BIGINT,
    PRIMARY KEY  (id),
    CONSTRAINT fk_parent
        FOREIGN KEY(parent_id)
            REFERENCES category(id)
);

