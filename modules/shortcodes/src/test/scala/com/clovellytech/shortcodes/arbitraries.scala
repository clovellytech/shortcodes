package com.clovellytech.shortcodes

import cats.data.OptionT
import cats.data.NonEmptyVector
import org.scalacheck._
import cats.effect._
import cats.implicits._
import com.clovellytech.shortcode.ShortCode

object arbitraries {

  type WordList = NonEmptyVector[String]
  type ByteEncodedWord = Array[Byte]

  implicit val byteEncodedArb: Arbitrary[ByteEncodedWord] = Arbitrary {
    val byteGen =
      Gen.nonEmptyListOf[Byte](Gen.Choose.chooseByte.choose(Byte.MinValue, Byte.MaxValue))
    // just doing it this way because our implementation requires byte pairs, so this
    // ensures we return an array of at least length two.
    for {
      l1 <- byteGen
      l2 <- byteGen
    } yield {
      (l1 ++ l2).toArray
    }
  }

  def wordListGen[F[_]: Sync: ContextShift](blocker: Blocker): OptionT[F, Gen[WordList]] = {
    val r = new ShortCode[F](blocker)

    r.getRandom(100000).map(_._1).map { ws =>
      Gen.nonEmptyListOf(Gen.oneOf(ws.toIterable)).map(_.toVector.toNev.get)
    }
  }

}
