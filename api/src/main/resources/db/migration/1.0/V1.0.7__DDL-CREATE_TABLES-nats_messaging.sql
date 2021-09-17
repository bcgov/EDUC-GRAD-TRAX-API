--------------------------------------------------------
--  DDL for Table REPLICATION_EVENT
--------------------------------------------------------

  CREATE TABLE "REPLICATION_EVENT"
   (	"REPLICATION_EVENT_ID" RAW(16),
	"EVENT_ID" RAW(16),
	"EVENT_PAYLOAD" BLOB,
	"EVENT_STATUS" VARCHAR2(50 BYTE),
	"EVENT_TYPE" VARCHAR2(100 BYTE),
	"EVENT_OUTCOME" VARCHAR2(100 BYTE),
	"CREATE_USER" VARCHAR2(32 BYTE),
	"CREATE_DATE" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP,
	"UPDATE_USER" VARCHAR2(32 BYTE),
	"UPDATE_DATE" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP
   ) SEGMENT CREATION IMMEDIATE
 NOCOMPRESS LOGGING
  TABLESPACE "API_GRAD_DATA"   NO INMEMORY
 LOB ("EVENT_PAYLOAD") STORE AS SECUREFILE (
  TABLESPACE "API_GRAD_BLOB_DATA" ENABLE STORAGE IN ROW CHUNK 8192
  NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;

   COMMENT ON TABLE "REPLICATION_EVENT"  IS 'This table is used to store all the received choreographed events from different api via pub/sub model and process them.';
--------------------------------------------------------
--  DDL for Table REPLICATION_SHEDLOCK
--------------------------------------------------------

  CREATE TABLE "REPLICATION_SHEDLOCK"
   (	"NAME" VARCHAR2(64 BYTE),
	"LOCK_UNTIL" TIMESTAMP (3),
	"LOCKED_AT" TIMESTAMP (3),
	"LOCKED_BY" VARCHAR2(255 BYTE)
   ) SEGMENT CREATION IMMEDIATE
 NOCOMPRESS LOGGING
  TABLESPACE "API_GRAD_DATA"   NO INMEMORY ;

   COMMENT ON TABLE "REPLICATION_SHEDLOCK"  IS 'This table is used to achieve distributed lock between pods, for schedulers.';
--------------------------------------------------------
--  DDL for Index REPLICATION_EVENT_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "REPLICATION_EVENT_PK" ON "REPLICATION_EVENT" ("REPLICATION_EVENT_ID")
  TABLESPACE "API_GRAD_IDX" ;
--------------------------------------------------------
--  DDL for Index REPLICATION_EVENT_EVENT_STATUS_IDX
--------------------------------------------------------

  CREATE INDEX "REPLEVT_EVENT_STATUS_IDX" ON "REPLICATION_EVENT" ("EVENT_STATUS")
  TABLESPACE "API_GRAD_IDX" ;
--------------------------------------------------------
--  DDL for Index REPLICATION_EVENT_EVENT_TYPE_IDX
--------------------------------------------------------

  CREATE INDEX "REPLEVT_EVENT_TYPE_IDX" ON "REPLICATION_EVENT" ("EVENT_TYPE")
  TABLESPACE "API_GRAD_IDX" ;
--------------------------------------------------------
--  DDL for Index REPLICATION_EVENT_EVENT_ID_UK
--------------------------------------------------------

  CREATE UNIQUE INDEX "REPLEVT_EVENT_ID_UK" ON "REPLICATION_EVENT" ("EVENT_ID")
  TABLESPACE "API_GRAD_IDX" ;
--------------------------------------------------------
--  DDL for Index REPLICATION_SHEDLOCK_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "REPLICATION_SHEDLOCK_PK" ON "REPLICATION_SHEDLOCK" ("NAME")
  TABLESPACE "API_GRAD_IDX" ;
--------------------------------------------------------
--  Constraints for Table REPLICATION_EVENT
--------------------------------------------------------

  ALTER TABLE "REPLICATION_EVENT" MODIFY ("REPLICATION_EVENT_ID" NOT NULL ENABLE);
  ALTER TABLE "REPLICATION_EVENT" MODIFY ("EVENT_ID" NOT NULL ENABLE);
  ALTER TABLE "REPLICATION_EVENT" MODIFY ("EVENT_PAYLOAD" NOT NULL ENABLE);
  ALTER TABLE "REPLICATION_EVENT" MODIFY ("EVENT_STATUS" NOT NULL ENABLE);
  ALTER TABLE "REPLICATION_EVENT" MODIFY ("EVENT_TYPE" NOT NULL ENABLE);
  ALTER TABLE "REPLICATION_EVENT" MODIFY ("EVENT_OUTCOME" NOT NULL ENABLE);

 ALTER TABLE "REPLICATION_EVENT" ADD CONSTRAINT "REPLICATION_EVENT_PK" PRIMARY KEY ("REPLICATION_EVENT_ID")
  USING INDEX TABLESPACE "API_GRAD_IDX"  ENABLE;

 ALTER TABLE "REPLICATION_EVENT" ADD CONSTRAINT "REPLEVT_EVENT_ID_UK" UNIQUE ("EVENT_ID")
  USING INDEX TABLESPACE "API_GRAD_IDX"  ENABLE;
--------------------------------------------------------
--  Constraints for Table REPLICATION_SHEDLOCK
--------------------------------------------------------

  ALTER TABLE "REPLICATION_SHEDLOCK" ADD CONSTRAINT "REPLICATION_SHEDLOCK_PK" PRIMARY KEY ("NAME")
  USING INDEX TABLESPACE "API_GRAD_IDX"  ENABLE;
