package com.clovellytech.shortcode

import cats.effect._
import cats.data.NonEmptyVector
import cats.data.OptionT
import cats.implicits._
import fs2._
import java.nio.file.Paths
import scala.math.BigInt
import scala.util.Random

/**
  * Use shortcode to generate sequences of words from a dictionary. Words are encoded and decoded
  * to byte arrays for easy storage and retrieval from a database.
  *
  * @param blocker
  */
class ShortCode[F[_]: Sync: ContextShift](blocker: Blocker) {

  def uri = getClass.getResource("/wordlist.txt").toURI()
  def path = Paths.get(uri)

  def words: Stream[F, (String, Long)] =
    io.file
      .readAll[F](path, blocker, 1024)
      .through(text.utf8Decode)
      .through(text.lines)
      .zipWithIndex

  def all: OptionT[F, NonEmptyVector[(String, Long)]] =
    OptionT(words.compile.toVector.map(_.toNev))

  def toByteArray(words: NonEmptyVector[Long]): Array[Byte] =
    words.map(n => BigInt(n).toByteArray.toList).reduce.toArray

  def toByteArray(words: NonEmptyVector[String]): OptionT[F, Array[Byte]] =
    for {
      ws <- all
      wsMap <- OptionT.pure[F](ws.toIterable.toMap)
      longs <- OptionT.fromOption[F](words.traverse(w => wsMap.get(w)))
    } yield toByteArray(longs)

  def bytesToWords(bytes: Array[Byte]): OptionT[F, NonEmptyVector[String]] =
    for {
      ws <- all
      byteGroups <- OptionT.pure[F](bytes.grouped(8).toVector)
      indices <- OptionT.pure[F](byteGroups.map(v => BigInt(v).toLong))
      wsMap <- OptionT.pure[F](ws.toIterable.map(_.swap).toMap)
      words <- OptionT.fromOption[F](indices.traverse(i => wsMap.get(i)).flatMap(_.toNev))
    } yield words

  def getRandom(n: Int): OptionT[F, (NonEmptyVector[String], Array[Byte])] =
    for {
      ws <- all
      indices <- OptionT.fromOption[F](
        0.to(n).map(_ => math.abs(Random.nextLong()) % ws.length).toVector.toNev,
      )
      rs <- OptionT.fromOption[F](indices.traverse(ws.get(_)))
    } yield (rs.map(_._1), toByteArray(rs.map(_._2)))
}
