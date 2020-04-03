package com.clovellytech.shortcodes

import arbitraries._
import cats.data.OptionT
import cats.effect._
import org.scalacheck._
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import com.clovellytech.shortcode.ShortCode
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ShortCodeTestSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks {
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val b: Blocker = Blocker.liftExecutionContext(ec)

  val r = new ShortCode[IO](b)

  implicit val wordsArb: Arbitrary[WordList] = Arbitrary {
    wordListGen[IO](b).value.unsafeRunSync().get
  }

  "codes" should "be reversible" in forAll { (ws: WordList) =>
    val test: OptionT[IO, Assertion] = for {
      bytes <- r.toByteArray(ws)
      words <- r.bytesToWords(bytes)
    } yield {
      ws.toVector should not be empty
      ws.toVector should equal(words.toVector)
    }

    test.value.unsafeRunSync() should not be None
  }

  type TestInt = Int
  implicit val arbTestInt = Arbitrary(Gen.chooseNum(1, 20))

  "generated codes" should "be reversable" in forAll { (i: TestInt) =>
    val test: OptionT[IO, Assertion] = for {
      (code, bytes) <- r.getRandom(i)
      encoded <- r.toByteArray(code)
      decoded <- r.bytesToWords(bytes)
      deencoded <- r.bytesToWords(encoded)
      endecoded <- r.toByteArray(decoded)
    } yield {
      code should equal(decoded)
      bytes should equal(encoded)
      deencoded should equal(code)
      endecoded should equal(bytes)
      bytes.length % 2 should equal(0)
    }

    test.value.unsafeRunSync() should not be None
  }

}
