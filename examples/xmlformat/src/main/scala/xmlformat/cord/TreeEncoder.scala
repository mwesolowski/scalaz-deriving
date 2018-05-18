// Copyright: 2017 - 2018 Sam Halliday
// License: http://www.gnu.org/licenses/lgpl-3.0.en.html

package xmlformat
package cord

import scalaz._, Scalaz._

sealed abstract class TCord {
  override def toString: String = {
    val sb = new StringBuilder
    appendTo(sb)
    sb.toString
  }

  private def appendTo(sb: StringBuilder): Unit = this match {
    case TCord.TBranch(a, b) =>
      a.appendTo(sb): Unit
      b.appendTo(sb): Unit
    case TCord.TLeaf(s) =>
      val _ = sb.append(s)
  }
}
object TCord {
  private final case class TBranch(a: TCord, b: TCord) extends TCord
  private final case class TLeaf(s: String)            extends TCord

  def apply(s: String): TCord = TLeaf(s)

  val empty: TCord = TLeaf("")

  implicit val monoid: Monoid[TCord] = new Monoid[TCord] {
    def zero: TCord                           = empty
    def append(f1: TCord, f2: =>TCord): TCord = TBranch(f1, f2)
  }
}

object TreeEncoder {
  def encode(t: XTag): String = toTCord(t).toString

  def toTCord(t: XTag): TCord = preamble |+| xtag(t, 0)

  private[this] val preamble: TCord = TCord(
    "<?xml version='1.0' encoding='UTF-8'?>"
  )
  private[this] val space: TCord = TCord(" ")
  private[this] val gt: TCord    = TCord(">")
  private[this] val egt: TCord   = TCord("/>")
  private[this] val lt: TCord    = TCord("<")
  private[this] val elt: TCord   = TCord("</")

  private[this] def xtag(t: XTag, level: Int): TCord = {
    val name = TCord(t.name)
    val start = {
      val open = pad(level) |+| lt |+| name
      if (t.attrs.isEmpty)
        open
      else {
        val attrs = t.attrs.map(xattr).intersperse(space).fold
        open |+| space |+| attrs
      }
    }

    if (t.children.isEmpty && t.body.isEmpty)
      start |+| egt
    else
      t.children.toNel match {
        case None =>
          val body = t.body.map(xstring(_)).orZero
          val end  = elt |+| name |+| gt
          start |+| gt |+| body |+| end

        case Some(cs) =>
          val children = cs.map(xtag(_, level + 1)).fold
          val body     = t.body.map(s => pad(level + 1) |+| xstring(s)).orZero
          val end      = elt |+| name |+| gt
          start |+| gt |+| children |+| body |+| pad(level) |+| end
      }
  }

  private[this] val pad: Int => TCord = Memo.arrayMemo[TCord](16).apply(pad0(_))
  private[this] def pad0(level: Int): TCord =
    TCord("\n" + (" " * 2 * level))

  private[this] def xattr(a: XAttr): TCord =
    TCord(
      s"""${a.name}="${CordEncoder.replaceXmlEntities(a.value.text)}""""
    )

  private[this] def xstring(s: XString): TCord =
    if (!CordEncoder.containsXmlEntities(s.text))
      TCord(s.text)
    else {
      val matcher = CordEncoder.cdata.matcher(s.text)
      val clean =
        if (!matcher.find()) s.text
        else matcher.replaceAll(CordEncoder.nested)
      TCord(s"<![CDATA[$clean]]>")
    }
}