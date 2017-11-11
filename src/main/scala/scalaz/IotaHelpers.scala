// Copyright: 2017 Sam Halliday
// License: http://www.gnu.org/licenses/lgpl-3.0.en.html

package scalaz

import scala.collection.immutable.{ Seq, Vector }

import iotaz._
import iotaz.TList._
import iotaz.TList.Compute.{ Aux => ↦ }
import iotaz.TList.Op.{ Map => ƒ }

// unintentional joke about the state of northern irish politics...
object LazyProd {
  def apply[A1](a1: => A1): Prod[Name[A1] :: TNil] = Prod(Name(a1))
  def apply[A1, A2](a1: => A1, a2: => A2): Prod[Name[A1] :: Name[A2] :: TNil] =
    Prod(Name(a1), Name(a2))
  def apply[A1, A2, A3](
    a1: => A1,
    a2: => A2,
    a3: => A3
  ): Prod[Name[A1] :: Name[A2] :: Name[A3] :: TNil] =
    Prod(Name(a1), Name(a2), Name(a3))
  def apply[A1, A2, A3, A4](
    a1: => A1,
    a2: => A2,
    a3: => A3,
    a4: => A4
  ): Prod[Name[A1] :: Name[A2] :: Name[A3] :: Name[A4] :: TNil] =
    Prod(Name(a1), Name(a2), Name(a3), Name(a4))
}

object Prods {
  def map[T[_], Y, L <: TList, TL <: TList](
    tcs: Prod[TL]
  )(f: T[Y] => Y)(
    implicit
    ev1: λ[a => Name[T[a]]] ƒ L ↦ TL
    // although scala is unable to infer an Cop.Inject[Y, L], we can
    // mathematically prove one exists because L is aligned with TL.
    // ev2: Cop.InjectL[Y, L]
  ): Prod[L] = Prod.unsafeApply { // allowed by evidence of Y
    tcs.values
      .asInstanceOf[Seq[Name[T[Y]]]] // from TMap
      .map(nty => f(nty.value))
  }

  val empty: Prod[TNil] = Prod()

  def from1T[A1](a: A1): Prod[A1 :: TNil] =
    Prod.unsafeApply(Vector(a))
  def from2T[A1, A2](e: (A1, A2)): Prod[A1 :: A2 :: TNil] =
    Prod.unsafeApply(Vector(e._1, e._2))
  def from3T[A1, A2, A3](e: (A1, A2, A3)): Prod[A1 :: A2 :: A3 :: TNil] =
    Prod.unsafeApply(Vector(e._1, e._2, e._3))
  def from4T[A1, A2, A3, A4](
    e: (A1, A2, A3, A4)
  ): Prod[A1 :: A2 :: A3 :: A4 :: TNil] =
    Prod.unsafeApply(Vector(e._1, e._2, e._3, e._4))

  def to1T[A1](a: Prod[A1 :: TNil]): A1 = a.values(0).asInstanceOf[A1]
  def to2T[A1, A2](a: Prod[A1 :: A2 :: TNil]): (A1, A2) = (
    a.values(0).asInstanceOf[A1],
    a.values(1).asInstanceOf[A2]
  )
  def to3T[A1, A2, A3](a: Prod[A1 :: A2 :: A3 :: TNil]): (A1, A2, A3) = (
    a.values(0).asInstanceOf[A1],
    a.values(1).asInstanceOf[A2],
    a.values(2).asInstanceOf[A3]
  )
  def to4T[A1, A2, A3, A4](
    a: Prod[A1 :: A2 :: A3 :: A4 :: TNil]
  ): (A1, A2, A3, A4) = (
    a.values(0).asInstanceOf[A1],
    a.values(1).asInstanceOf[A2],
    a.values(2).asInstanceOf[A3],
    a.values(3).asInstanceOf[A4]
  )

}

object Cops {
  def mapMaybe[T[_], Y, L <: TList, TL <: TList](
    tcs: Prod[TL]
  )(f: T[Y] => Maybe[Y])(
    implicit
    ev1: λ[a => Name[T[a]]] ƒ L ↦ TL
    // although scala is unable to infer an Cop.Inject[Y, L], we can
    // mathematically prove one exists because L is aligned with TL.
    // ev2: Cop.InjectL[Y, L]
  ): Maybe[Cop[L]] = Maybe.fromOption {
    tcs.values
      .asInstanceOf[Seq[Name[T[Y]]]] // from TMap
      .toStream
      .zipWithIndex
      .flatMap {
        case (v, i) =>
          f(v.value).toOption.map { y =>
            Cop.unsafeApply[L, Y](i, y) // from implied InjectL
          }
      }
      .headOption
  }

  // variant of covariantExtract that always succeeds
  // (Default could be a niche usecase to be fair...)
  def mapFirst[T[_], Y, L <: TList, TL <: TList](
    tcs: Prod[TL]
  )(f: T[Y] => Y)(
    implicit
    ev1: λ[a => Name[T[a]]] ƒ L ↦ TL
  ): Cop[L] = {
    val ty = tcs.values.asInstanceOf[Seq[Name[T[Y]]]] // from TMap
    Cop.unsafeApply[L, Y](0, f(ty.head.value)) // from implied InjectL
  }

  def from1[A1](e: A1): Cop[A1 :: TNil] = Cop.unsafeApply(0, e)
  def from2[A1, A2](e: A1 \/ A2): Cop[A1 :: A2 :: TNil] = e match {
    case -\/(a) => Cop.unsafeApply(0, a)
    case \/-(b) => Cop.unsafeApply(1, b)
  }
  def from3[A1, A2, A3](e: A1 \/ (A2 \/ A3)): Cop[A1 :: A2 :: A3 :: TNil] =
    e match {
      case -\/(a)      => Cop.unsafeApply(0, a)
      case \/-(-\/(b)) => Cop.unsafeApply(1, b)
      case \/-(\/-(c)) => Cop.unsafeApply(2, c)
    }
  def from4[A1, A2, A3, A4](
    e: A1 \/ (A2 \/ (A3 \/ A4))
  ): Cop[A1 :: A2 :: A3 :: A4 :: TNil] =
    e match {
      case -\/(a)           => Cop.unsafeApply(0, a)
      case \/-(-\/(b))      => Cop.unsafeApply(1, b)
      case \/-(\/-(-\/(c))) => Cop.unsafeApply(2, c)
      case \/-(\/-(\/-(d))) => Cop.unsafeApply(3, d)
    }

  def to1[A1](c: Cop[A1 :: TNil]): A1 = c.value.asInstanceOf[A1]
  def to2[A1, A2](c: Cop[A1 :: A2 :: TNil]): A1 \/ A2 = c.index match {
    case 0 => -\/(c.value.asInstanceOf[A1])
    case 1 => \/-(c.value.asInstanceOf[A2])
  }
  def to3[A1, A2, A3](c: Cop[A1 :: A2 :: A3 :: TNil]): A1 \/ (A2 \/ A3) =
    c.index match {
      case 0 => -\/(c.value.asInstanceOf[A1])
      case 1 => \/-(-\/(c.value.asInstanceOf[A2]))
      case 2 => \/-(\/-(c.value.asInstanceOf[A3]))
    }
  def to4[A1, A2, A3, A4](
    c: Cop[A1 :: A2 :: A3 :: A4 :: TNil]
  ): A1 \/ (A2 \/ (A3 \/ A4)) = c.index match {
    case 0 => -\/(c.value.asInstanceOf[A1])
    case 1 => \/-(-\/(c.value.asInstanceOf[A2]))
    case 2 => \/-(\/-(-\/(c.value.asInstanceOf[A3])))
    case 3 => \/-(\/-(\/-(c.value.asInstanceOf[A4])))
  }

}
