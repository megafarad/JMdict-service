# JMdict-service

A RESTful interface for [JMdict](https://www.edrdg.org/jmdict/j_jmdict.html) - a Japanese dictionary. Built on Akka HTTP.

* **Imports data into a Postgres database** on a configurable [Quartz cron](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html) schedule.
* Services search queries.

# Usage

The easiest way to use this project is via Docker. 

`docker pull sirhc1977/jmdict-service`


