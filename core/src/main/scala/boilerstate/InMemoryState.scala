package boilerstate

import cats.implicits._
import cats.kernel.Monoid
import cats.mtl.{ApplicativeAsk, MonadState}
import cats.{Functor, Order}

//lots of todos
trait InMemoryState[F[_], K, V] {
  def create: CreateState[F, K, V]
  def read: ReadState[F, K, V]
  def update: UpdateState[F, K, V]
  def delete: DeleteState[F, K, V]
}

object InMemoryState {

  class Aliases[K, V] {
    type read[F[_]]   = ReadState[F, K, V]
    type create[F[_]] = CreateState[F, K, V]
  }

  implicit def fromComponents[F[_], K, V](implicit _create: CreateState[F, K, V],
                                          _read: ReadState[F, K, V],
                                          _update: UpdateState[F, K, V],
                                          _delete: DeleteState[F, K, V]): InMemoryState[F, K, V] = {
    new InMemoryState[F, K, V] {
      override def create: CreateState[F, K, V] = _create
      override def read: ReadState[F, K, V]     = _read
      override def update: UpdateState[F, K, V] = _update
      override def delete: DeleteState[F, K, V] = _delete
    }
  }
}

trait CreateState[F[_], K, V] {
  def withKey(key: K, value: V): F[Unit]
  def withCalcKey(calcKey: K => K)(value: V)(implicit K: Monoid[K], O: Order[K]): F[K]
}

object CreateState {
  implicit def createStateForMonadState[F[_], K, V](implicit F: MonadState[F, Map[K, V]]): CreateState[F, K, V] =
    new CreateState[F, K, V] {
      private def insert(key: K, value: V): Map[K, V] => Map[K, V] = _ + (key -> value)

      override def withKey(key: K, value: V): F[Unit] = F.modify(insert(key, value))

      override def withCalcKey(calcKey: K => K)(value: V)(implicit K: Monoid[K], O: Order[K]): F[K] = {
        F.modify { old =>
          val nextKey = old.keys.toStream.maximumOption.foldMap(calcKey)
          insert(nextKey, value)(old)
        }
      }
    }
}

trait ReadState[F[_], K, V] {
  def all: F[List[V]]
  def byKey(k: K): F[Option[V]]
}

object ReadState {
  implicit def readStateForApplicativeAsk[F[_]: Functor, K, V](
    implicit F: ApplicativeAsk[F, Map[K, V]]): ReadState[F, K, V] =
    new ReadState[F, K, V] {
      override val all: F[List[V]]           = F.reader(_.values.toList)
      override def byKey(k: K): F[Option[V]] = F.reader(_.get(k))
    }
}

trait UpdateState[F[_], K, V] {}

//deletos
trait DeleteState[F[_], K, V] {}
