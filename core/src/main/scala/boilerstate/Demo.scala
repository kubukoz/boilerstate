package boilerstate

import boilerstate.IMSProgram.ProgState
import cats.Applicative
import cats.data.StateT
import cats.effect.{ExitCode, IO, IOApp, SyncConsole}
import cats.implicits._
import cats.mtl.implicits._

object Demo extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    type S[A] = StateT[IO, Map[Int, String], A]

    val cons             = new SyncConsole[S]
    val md: MyProgram[S] = new IMSProgram[S]

    md.execute.map(_.toString).flatMap(cons.putStrLn).runA(Map.empty)
  }.as(ExitCode.Success)
}

trait MyProgram[F[_]] {
  def execute: F[List[String]]
}

class IMSProgram[F[_]: Applicative](implicit read: ProgState.read[F], create: ProgState.create[F])
    extends MyProgram[F] {

  override def execute: F[List[String]] = create.withCalcKey(_ + 1)("A").replicateA(5) *> read.all
}

object IMSProgram {
  object ProgState extends InMemoryState.Aliases[Int, String]
}
