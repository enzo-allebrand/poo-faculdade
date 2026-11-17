CREATE TABLE IF NOT EXISTS "usuario" (
	"id" serial NOT NULL UNIQUE,
	"nome" varchar(120) NOT NULL,
	"email" varchar(180) NOT NULL UNIQUE,
	"senha_hash" varchar(255) NOT NULL,
	PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "conta" (
	"id" serial NOT NULL UNIQUE,
	"descricao" varchar(150) NOT NULL,
	"saldo_inicial" numeric(12,2) NOT NULL DEFAULT 0,
	"usuario_id" integer NOT NULL,
	PRIMARY KEY ("id"),
	CONSTRAINT "conta_usuario_fk" FOREIGN KEY ("usuario_id") REFERENCES "usuario"("id")
);

CREATE TABLE IF NOT EXISTS "categoria" (
	"id" serial NOT NULL UNIQUE,
	"descricao" varchar(150) NOT NULL,
	"tipo" varchar(1) NOT NULL DEFAULT 'A',
	"usuario_id" integer NOT NULL,
	PRIMARY KEY ("id"),
	CONSTRAINT "categoria_usuario_fk" FOREIGN KEY ("usuario_id") REFERENCES "usuario"("id")
);

CREATE TABLE IF NOT EXISTS "transacao" (
	"id" serial NOT NULL UNIQUE,
	"descricao" varchar(500) NOT NULL,
	"valor" numeric(12,2) NOT NULL,
	"tipo" varchar(1) NOT NULL,
	"datatransacao" timestamp NOT NULL,
	"transferencia_id" varchar(64),
	"correlation_id" varchar(64),
	"conta_id" integer NOT NULL,
	"categoria_id" integer NOT NULL,
	"usuario_id" integer NOT NULL,
	PRIMARY KEY ("id"),
	CONSTRAINT "transacao_conta_fk" FOREIGN KEY ("conta_id") REFERENCES "conta"("id"),
	CONSTRAINT "transacao_categoria_fk" FOREIGN KEY ("categoria_id") REFERENCES "categoria"("id"),
	CONSTRAINT "transacao_usuario_fk" FOREIGN KEY ("usuario_id") REFERENCES "usuario"("id")
);

CREATE INDEX IF NOT EXISTS "idx_conta_usuario" ON "conta" ("usuario_id");
CREATE INDEX IF NOT EXISTS "idx_categoria_usuario" ON "categoria" ("usuario_id");
CREATE INDEX IF NOT EXISTS "idx_transacao_usuario_data" ON "transacao" ("usuario_id", "datatransacao");
CREATE INDEX IF NOT EXISTS "idx_transacao_conta" ON "transacao" ("conta_id");
CREATE INDEX IF NOT EXISTS "idx_transacao_categoria" ON "transacao" ("categoria_id");
CREATE INDEX IF NOT EXISTS "idx_transacao_correlation" ON "transacao" ("correlation_id");
