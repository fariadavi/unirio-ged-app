CREATE TABLE category (
    id			BIGINT GENERATED ALWAYS AS IDENTITY,
    name       	VARCHAR NOT NULL,
    parent_id   BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_parent
        FOREIGN KEY(parent_id)
            REFERENCES category(id)
);

CREATE TABLE user_permission (
    id			        BIGINT GENERATED ALWAYS AS IDENTITY,
    platform_user_id    BIGINT UNIQUE,
    permissions         VARCHAR NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_platform_user
      FOREIGN KEY(platform_user_id)
          REFERENCES public.platform_user(id)
);

INSERT INTO user_permission (platform_user_id, permissions) VALUES (
   '${user_id}',
   '${starting_dept_permissions}'
);
