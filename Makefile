.PHONY: start stop build test it logs help

help:           ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*##' Makefile | awk 'BEGIN {FS = ":.*##"}; {printf "  %-10s %s\n", $$1, $$2}'

start:          ## Build JARs + start Docker Compose stack
	./healthcare-infra/scripts/deploy-all.sh

stop:           ## Stop Docker Compose stack
	cd docker && docker compose down

build:          ## Build JARs only (no Docker)
	cd services && mvn -pl auth-service,provider-service,ai-service,gateway -am clean package -DskipTests -q

test:           ## Run unit tests
	./dev.sh test

it:             ## Run integration tests (local stack must be up)
	./integration_tests/run-it.sh all

logs:           ## Tail Docker Compose logs
	cd docker && docker compose logs -f
