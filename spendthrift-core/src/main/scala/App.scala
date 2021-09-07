package spendthrift.core

import cats.effect._

object App extends IOApp.Simple {

  override def run: IO[Unit] =
    IO.delay(println("Hello World!"))

}
