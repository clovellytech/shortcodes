package com.clovellytech.shortcodes

import arbitraries._
import cats.effect._
import org.scalacheck._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import com.clovellytech.shortcode.ShortCode

class ShortCodeTestSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks {
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val b: Blocker = Blocker.liftExecutionContext(ec)

  val r = new ShortCode[IO](b)

  implicit val wordsArb: Arbitrary[WordList] = Arbitrary {
    wordListGen[IO](b).value.unsafeRunSync().get
  }


  "codes" should "be reversible" in forAll { (ws: WordList) =>
    for {
      bytes <- r.toByteArray(ws)
      words <- r.bytesToWords(bytes)
    } yield {
      ws.toVector should not be empty
      ws.toVector should equal (words.toVector)
    }
  }

  "bytes" should "be decodable" in forAll { (bs: ByteEncodedWord) =>
    for {
      words <- r.bytesToWords(bs)
      decoded <- r.toByteArray(words)
    } yield {
      bs should equal (decoded)
    }
  }

}