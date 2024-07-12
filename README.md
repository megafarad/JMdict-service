# JMdict-service

A RESTful interface for [JMdict](https://www.edrdg.org/jmdict/j_jmdict.html) - a Japanese dictionary. Built on Pekko HTTP.

* **Imports data into a Postgres database** on a configurable [Quartz cron](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html) schedule.
* Services search queries.

# Usage
## Running Locally
The easiest way to use this project is via Docker. To run locally with default settings, run the following:
```
docker pull sirhc1977/jmdict-service
docker run --name jmdict-service -d -p 9000:9000 sirhc1977/jmdict-service
```

You will need to have a Postgres database running locally as well.

## Environment Variables

There are a number of environment variables available for configuring behavior:

* `DB_SERVER_NAME`
* `DB_SERVER_PORT`
* `DB_NAME`
* `DB_USER`
* `DB_PASSWORD`

These are available for configuring the Postgres database connection.

* `AUTH0_ENABLED`
* `AUTH0_DOMAIN`
* `AUTH0_AUDIENCE`

These are for enabling [Auth0](https://auth0.com) authentication control. By default, `AUTH0_ENABLED` is false, and the 
other two environment variables are ignored.

`IMPORT_ON_STARTUP`

Schedules an import upon start up. By default, this is false.

`IMPORTER_SCHEDULE`

Sets the quartz cron schedule. By default, this is set to `0 0 0 1/1 * ? *`.

## Copyright Notice

If you deploy this service, you _must_ include the following copyright notice:

> Copyright (C) 2022 The Electronic Dictionary Research and Development Group. Creative Commons Attribution-ShareAlike Licence (V3.0)

