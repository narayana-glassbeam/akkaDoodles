import akka.actor.{ActorSystem, ActorRef, Props, Actor}
import com.typesafe.config.{Config, ConfigFactory}

/** @author Derek Wyatt */
class Echo extends Actor {
  def receive = {
    case m =>
      println("%s: %s".format(m, sender.path))
      sender ! m
  }
}

object Main {
  def makeSystem(port: Int) = ActorSystem("MySystem", ConfigFactory.parseString("""
     akka {
       actor {
         provider = "akka.remote.RemoteActorRefProvider"
       }
       remote {
         transport = "akka.remote.netty.NettyRemoteTransport"
         netty {
           hostname = ""
           port = %d
         }
       }
     }
    """.format(port)))

  def main(args: Array[String]) {
    val system = makeSystem(args(0).toInt)
    val a = system.actorOf(Props[Echo], "EchoActor")
    println("Running")
    Thread.sleep(2000)
    println("Sending")
    val b = system.actorFor("akka://MySystem@localhost:%s/user/EchoActor".format(args(1)))
    Thread.sleep(2000)
    b ! "Whatever"
    Thread.sleep(5000)
    system.shutdown()
  }
}

// Run one instance as "Main 2020 2021" and the other as "Main 2021 2020"