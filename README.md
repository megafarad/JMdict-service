# JMdict-service

A RESTful interface for [JMdict](https://www.edrdg.org/jmdict/j_jmdict.html) - a Japanese dictionary. Built on Akka HTTP.

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


