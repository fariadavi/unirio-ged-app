CREATE TABLE department (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    name  	            VARCHAR(80) NOT NULL,
    acronym             VARCHAR(5) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE platform_user (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY,
    first_name  	    VARCHAR(20) NOT NULL,
    surname			    VARCHAR(20) NOT NULL,
    email      		    VARCHAR(40) NOT NULL UNIQUE,
    permissions         VARCHAR(160),
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