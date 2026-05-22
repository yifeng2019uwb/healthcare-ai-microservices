.PHONY: start stop build test it logs help

help:           ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*##' Makefile | awk 'BEGIN {FS = ":.*##"}; {printf "  %-10s %s\n", $$1, $$2}'

start:          ## Build JARs + start Docker Compose stack
	./healthcare-infra/scripts/deploy-all.sh

stop:           ## Stop Docker Compose stack
	cd docker && docker compose down

build:          ## Build JARs only (no Docker)
	cd services/shared && mvn install -DskipTests -q
	for svc in auth-service provider-service gateway; do \
	  cd services/$$svc && mvn clean package -DskipTests -q && cd ../..; \
	done

test:           ## Run unit tests
	./dev.sh test

it:             ## Run integration tests (local stack must be up)
	./integration_tests/run-it.sh all

logs:           ## Tail Docker Compose logs
	cd docker && docker compose logs -f
