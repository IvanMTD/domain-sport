include .env-dc
export

all:
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

build:  ##Build
	docker build -t domensport-app .

pre-build: create-network 
	export COMPOSE_FILE="$(DC_CONFIG_BUILD)"; docker compose pull
	export COMPOSE_FILE="$(DC_CONFIG_BUILD)"; docker compose --env-file=.env-dc up --build -d

run: create-network build ## Run
	export COMPOSE_FILE="$(DC_CONFIG_ALL)"; docker compose pull
	export COMPOSE_FILE="$(DC_CONFIG_ALL)"; docker compose --env-file=.env-dc up --build -d

pull: ## Pull
	export COMPOSE_FILE="$(DC_CONFIG_ALL)"; docker compose --env-file=.env-dc pull

stop: ## Stop
	export COMPOSE_FILE="$(DC_CONFIG_ALL)"; docker compose --env-file=.env-dc stop

clean: stop-all-purge ## Clean

stop-all-purge:
	export COMPOSE_FILE="$(DC_CONFIG_ALL)"; docker compose --env-file=.env-dc down -v
	docker network rm $(DEPLOY_NETWORK_NAME)

create-network:
	docker network inspect $(DEPLOY_NETWORK_NAME) >/dev/null 2>&1 || docker network create $(DEPLOY_NETWORK_NAME)

remove-network:
	docker network rm $(DEPLOY_NETWORK_NAME)

sleep10:
	sleep 10
