CREATE TABLE department (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    name  	            VARCHAR(80) NOT NULL,
    acronym             VARCHAR(5) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE platform_user (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    first_name  	    VARCHAR(20),
    surname			    VARCHAR(20),
    email      		    VARCHAR(40) NOT NULL UNIQUE,
    department_id       BIGINT,
    CONSTRAINT fk_department
        FOREIGN KEY(department_id)
            REFERENCES department(id),
    PRIMARY KEY (id)
);

CREATE TABLE platform_user_department (
    department_id       BIGINT NOT NULL,
    platform_user_id    BIGINT NOT NULL,
    CONSTRAINT fk_department
        FOREIGN KEY(department_id)
            REFERENCES department(id),
    CONSTRAINT fk_platform_user
        FOREIGN KEY(platform_user_id)
            REFERENCES platform_user(id),
    PRIMARY KEY (department_id, platform_user_id)
);

CREATE TABLE user_permission (
    id			        BIGINT GENERATED ALWAYS AS IDENTITY,
    platform_user_id    BIGINT,
    permissions         VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_platform_user
     FOREIGN KEY(platform_user_id)
         REFERENCES platform_user(id)
);

INSERT INTO platform_user (email) VALUES ('${application_user_email}');

INSERT INTO user_permission (platform_user_id, permissions) VALUES (
   (SELECT id FROM platform_user WHERE email = '${application_user_email}'),
    'MANAGE_SYSTEM_PERM,MANAGE_DEPARTMENTS'
);