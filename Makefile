db:
	docker-compose up -d db

docker-build:
	docker build -t eu.gcr.io/wire-bot/don-bot .

docker-run: db
	docker-compose up app

docker-rerun: docker-build db
	docker-compose rm -f app && docker-compose up app
