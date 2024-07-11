package com.megafarad.jmdictsvc.http

import org.apache.pekko.http.scaladsl.marshalling.ToEntityMarshaller
import org.apache.pekko.http.scaladsl.model.{StatusCode, StatusCodes}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route

import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

trait HttpBaseService {
  this: HttpRoute =>

  private val routes = mutable.ArrayDeque[Route]()

  override lazy val route: Route = {
    if (routes.nonEmpty) {
      routes.reduceLeft((next, prev) => next ~ prev)
    } else {
      pathPrefix(RemainingPath) { _ =>
        complete(StatusCodes.NotImplemented)
      }
    }
  }

  protected def registerRoute(route: Route): Unit = routes += route

  protected def defaultStatusCode[T]: T => StatusCode = _ => StatusCodes.OK

  protected def handleResponse[T: ClassTag](future: Future[_],
                                            statusCode: T => StatusCode = defaultStatusCode)(implicit m: ToEntityMarshaller[T]): Route =
    onComplete(future.mapTo[T]) {
      case Success(value) => complete(statusCode(value), value)
      case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
    }
}