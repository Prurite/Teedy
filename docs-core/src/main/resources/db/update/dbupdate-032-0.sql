create cached table T_USER_REGISTRATION_REQUEST (
    URR_ID_C varchar(36) not null,
    URR_NAME_C varchar(100),
    URR_EMAIL_C varchar(255) not null,
    URR_STATUS_C varchar(20) not null default 'PENDING',
    URR_CREATEDATE_D datetime not null,
    URR_UPDATEDATE_D datetime,
    primary key (URR_ID_C)
);

create index IDX_URR_EMAIL_C on T_USER_REGISTRATION_REQUEST (URR_EMAIL_C);
create index IDX_URR_STATUS_C on T_USER_REGISTRATION_REQUEST (URR_STATUS_C);

insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('USER_REGISTRATION_ENABLED', 'true');

update T_CONFIG set CFG_VALUE_C = '32' where CFG_ID_C = 'DB_VERSION';