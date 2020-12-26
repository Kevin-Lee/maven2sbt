package maven2sbt.core

import cats.syntax.all._

import scala.annotation.tailrec

/**
 * @author Kevin Lee
 * @since 2020-12-26
 */
object TestUtils {

  def findRange(ns: List[Int]): List[(Int, Int)] = {
    @tailrec
    def collectRange(ns: List[Int], rangeFound: (Int, Int), acc: List[(Int, Int)]): List[(Int, Int)] =
      rangeFound match {
        case (start, end) =>
          ns match {
            case x :: xs =>
              if ((end + 1) === x)
                collectRange(xs, start -> x, acc)
              else
                collectRange(xs, x -> x, rangeFound :: acc)
            case Nil =>
              acc
          }
      }
    ns.headOption.toList.flatMap(start => collectRange(ns.drop(1), start -> start, Nil)).reverse
  }

  lazy val ExpectedLetters: List[(Int, Int)] =
    findRange(
      (0.toChar to Char.MaxValue)
        .filter(c => c.isUpper || c.isLower || c.isDigit || c === '_')
        .map(_.toInt)
        .toList
    )

  lazy val ExpectedNonDigitLetters: List[(Int, Int)] =
    findRange(
      (0.toChar to Char.MaxValue)
        .filter(c => c.isUpper || c.isLower || c === '_')
        .map(_.toInt)
        .toList
    )

  lazy val ExpectedNonLetters: List[(Int, Int)] =
    findRange(
      (0.toChar to Char.MaxValue)
        .filterNot(c => c.isUpper || c.isLower || c.isDigit || c === '_')
        .map(_.toInt)
        .toList
    )

}
