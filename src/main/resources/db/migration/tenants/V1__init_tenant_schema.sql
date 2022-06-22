CREATE TABLE category (
    id			BIGINT GENERATED ALWAYS AS IDENTITY,
    name       	VARCHAR(40) NOT NULL,
    parent_id   BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_parent
        FOREIGN KEY(parent_id)
            REFERENCES category(id)
);

CREATE TABLE user_permission (
    id			        BIGINT GENERATED ALWAYS AS IDENTITY,
    platform_user_id    BIGINT,
    permissions         VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_platform_user
      FOREIGN KEY(platform_user_id)
          REFERENCES public.platform_user(id)
);
