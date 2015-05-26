CREATE MEMORY TABLE SBI_CACHE_ITEM (SIGNATURE VARCHAR(100) NOT NULL, NAME VARCHAR(100) NULL, TABLE_NAME VARCHAR(100) NOT NULL, DIMENSION NUMERIC NULL, CREATION_DATE DATE NULL, LAST_USED_DATE DATE NULL, PROPERTIES VARCHAR NULL, USER_IN VARCHAR(100) NOT NULL, USER_UP VARCHAR(100), USER_DE VARCHAR(100), TIME_IN TIMESTAMP NOT NULL, TIME_UP TIMESTAMP NULL DEFAULT NULL, TIME_DE TIMESTAMP NULL DEFAULT NULL, SBI_VERSION_IN VARCHAR(10), SBI_VERSION_UP VARCHAR(10), SBI_VERSION_DE VARCHAR(10), META_VERSION VARCHAR(100), ORGANIZATION VARCHAR(20), UNIQUE XAK1SBI_CACHE_ITEM (SIGNATURE), PRIMARY KEY (TABLE_NAME))
commit;
CREATE MEMORY TABLE SBI_CACHE_JOINED_ITEM (ID INTEGER  NOT NULL, SIGNATURE	VARCHAR(100) NOT NULL, JOINED_SIGNATURE	VARCHAR(100) NOT NULL, USER_IN VARCHAR(100) NOT NULL, USER_UP VARCHAR(100), USER_DE VARCHAR(100), TIME_IN TIMESTAMP NOT NULL, TIME_UP TIMESTAMP NULL DEFAULT NULL, TIME_DE TIMESTAMP NULL DEFAULT NULL, SBI_VERSION_IN VARCHAR(10), SBI_VERSION_UP VARCHAR(10), SBI_VERSION_DE VARCHAR(10), META_VERSION VARCHAR(100), ORGANIZATION VARCHAR(20), UNIQUE XAK1SBI_CACHE_JOINED_ITEM (SIGNATURE, JOINED_SIGNATURE), PRIMARY KEY (ID))
commit;
ALTER TABLE SBI_CACHE_JOINED_ITEM  ADD CONSTRAINT FK_SBI_CACHE_JOINED_ITEM_1 FOREIGN KEY ( SIGNATURE ) REFERENCES  SBI_CACHE_ITEM  ( SIGNATURE ) ON DELETE NO ACTION ON UPDATE CASCADE
commit;
ALTER TABLE SBI_CACHE_JOINED_ITEM  ADD CONSTRAINT FK_SBI_CACHE_JOINED_ITEM_2 FOREIGN KEY ( JOINED_SIGNATURE ) REFERENCES  SBI_CACHE_ITEM  ( SIGNATURE ) ON DELETE CASCADE ON UPDATE CASCADE
commit;