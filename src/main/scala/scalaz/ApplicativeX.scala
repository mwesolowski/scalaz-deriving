// Copyright: 2017 Sam Halliday
// License: http://www.gnu.org/licenses/lgpl-3.0.en.html

package scalaz

import scala.{ inline, Unit }

import iotaz.{ Prod, TList, TNil }
import iotaz.TList.::
import iotaz.TList.Compute.{ Aux => ↦ }
import iotaz.TList.Op.{ Map => ƒ }

/** Implementation of Applicative in terms of a single, generic, method. */
trait ApplicativeX[F[_]]
    extends LazyApplicative[F]
    with DangerousApplicative[F] {
  import Prods._

  def applyX[A, Z, L <: TList, FL <: TList](
    tcs: Prod[FL]
  )(
    f: Prod[L] => Z
  )(
    implicit
    ev: λ[a => Name[F[a]]] ƒ L ↦ FL
  ): F[Z]

  override def point[Z](z: => Z): F[Z] =
    applyX[Unit, Z, TNil, TNil](empty)(_ => z)

  override def map[A1, Z](a1: F[A1])(f: A1 => Z): F[Z] = {
    type L = A1 :: TNil
    applyX(Prod(Value(a1)))((a: Prod[L]) => f(to1T(a)))
  }

  override def apply2[A1, A2, Z](a1: => F[A1], a2: => F[A2])(
    f: (A1, A2) => Z
  ): F[Z] = {
    type L = A1 :: A2 :: TNil
    applyX(LazyProd(a1, a2))((as: Prod[L]) => f tupled to2T(as))
  }
  override def apply3[A1, A2, A3, Z](a1: => F[A1], a2: => F[A2], a3: => F[A3])(
    f: (A1, A2, A3) => Z
  ): F[Z] = {
    type L = A1 :: A2 :: A3 :: TNil
    applyX(LazyProd(a1, a2, a3))((as: Prod[L]) => f tupled to3T(as))
  }
  override def apply4[A1, A2, A3, A4, Z](a1: => F[A1],
                                         a2: => F[A2],
                                         a3: => F[A3],
                                         a4: => F[A4])(
    f: (A1, A2, A3, A4) => Z
  ): F[Z] = {
    type L = A1 :: A2 :: A3 :: A4 :: TNil
    applyX(LazyProd(a1, a2, a3, a4))((as: Prod[L]) => f tupled to4T(as))
  }
  // scalaz goes all the way to apply12, but we give up here for brevity

}
object ApplicativeX {
  @inline def apply[F[_]](implicit i: ApplicativeX[F]): ApplicativeX[F] = i
}