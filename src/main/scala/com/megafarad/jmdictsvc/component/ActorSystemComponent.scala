package com.megafarad.jmdictsvc.component

import org.apache.pekko.actor.ActorSystem

import scala.concurrent.ExecutionContext

trait ActorSystemComponent {
  implicit def actorSystem: ActorSystem
  implicit def executionContext: ExecutionContext
}
