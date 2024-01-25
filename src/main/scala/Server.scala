import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.util.Random

object Server extends App {
  implicit val system: ActorSystem = ActorSystem("SimpleApp")
  implicit val executionContext = system.dispatcher
  val config = ConfigFactory.load()
  val simulatedDelayLowebound = config.getDuration("delay-lower-bound").toMillis
  val simulatedDelayUpperbound =
    config.getDuration("delay-upper-bound").toMillis

  val route: Route = path("api" / "endpoint") {
    post {
      entity(as[ByteString]) { requestData =>
        val requestJsonString = requestData.utf8String
        val simluatedDelay =
          Random.between(simulatedDelayLowebound, simulatedDelayUpperbound)
        println(
          s"Received Request and simulated delay $simluatedDelay"
        )
        Thread.sleep(simluatedDelay)

        complete(
          HttpResponse(entity =
            HttpEntity(ContentTypes.`application/json`, requestJsonString)
          )
        )
      }
    }
  }

  val bindingFuture =
    Http().newServerAt("0.0.0.0", 8081).bind(route)

  bindingFuture.foreach { binding =>
    {
      val address =
        s"http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/"
      println(s"Server running at $address")
    }
  }

}
