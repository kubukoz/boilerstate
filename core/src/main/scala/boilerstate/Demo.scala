package boilerstate

import boilerstate.IMSProgram.ProgState
import cats.{Applicative, Monad}
import cats.effect.concurrent.Ref
import cats.effect._
import cats.implicits._
import cats.mtl.implicits._

object Demo extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    type S[A] = IO[A]

    implicit val cons = SyncConsole.stdio[S]

    Ref[IO].of(Map.empty[Int, String]).flatMap { ref =>
      import com.olegpy.meow.effects._
      implicit val MS = ref.stateInstance

      val md: MyProgram[S] = new IMSProgram[S]

      md.execute
    }
  }.as(ExitCode.Success)
}

trait MyProgram[F[_]] {
  def execute: F[Unit]
}

class IMSProgram[F[_]: Monad: ConsoleOut](implicit read: ProgState.read[F], create: ProgState.create[F])
    extends MyProgram[F] {

  override val execute: F[Unit] = create.withCalcKey(_ + 1)("A").replicateA(5) *>
    (read.all product read.byKey(3)).map(_.toString).flatMap(ConsoleOut[F].putStrLn(_))
}

object IMSProgram {
  object ProgState extends InMemoryState.Aliases[Int, String]
}
