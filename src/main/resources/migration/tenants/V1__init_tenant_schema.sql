CREATE TABLE category (
    id			BIGINT GENERATED ALWAYS AS IDENTITY,
    name       	VARCHAR(40) NOT NULL,
    parent      BIGINT,
    PRIMARY KEY  (id),
    CONSTRAINT fk_parent
        FOREIGN KEY(parent)
            REFERENCES category(id)
);

