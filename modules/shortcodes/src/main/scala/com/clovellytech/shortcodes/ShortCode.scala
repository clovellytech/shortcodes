package com.clovellytech.shortcode

import cats.effect._
import cats.data.NonEmptyVector
import cats.data.OptionT
import cats.implicits._
import fs2._
import java.net.URI
import java.nio.file._
import scala.math.BigInt
import scala.util.{Random, Try}

/**
  * Use shortcode to generate sequences of words from a dictionary. Words are encoded and decoded
  * to byte arrays for easy storage and retrieval from a database.
  *
  * @param blocker
  */
class ShortCode[F[_]: Sync: ContextShift](blocker: Blocker) {

  def fileLoc = "/com/clovellytech/shortcode/wordlist.txt"
  def uri = new URI(getClass.getResource(fileLoc).toExternalForm())

  def path =
    if (uri.toString.contains(".jar!")) {
      val zipfs = Try {
        FileSystems.newFileSystem(uri, new java.util.HashMap[String, String]())
      }.getOrElse {
        FileSystems.getFileSystem(uri)
      }
      zipfs.getPath(fileLoc)
    } else
      Paths.get(uri)

  def words: Stream[F, (String, Long)] =
    io.file
      .readAll[F](path, blocker, 1024)
      .through(text.utf8Decode)
      .through(text.lines)
      .zipWithIndex

  def all: OptionT[F, NonEmptyVector[(String, Long)]] =
    OptionT(words.compile.toVector.map(_.toNev))

  def toByteArray(words: NonEmptyVector[Long]): Array[Byte] =
    words
      .map { n =>
        val bs = BigInt(n).toByteArray.toList
        if (bs.length < 2)
          0.toByte :: bs
        else
          bs
      }
      .reduce
      .toArray

  def toByteArray(words: NonEmptyVector[String]): OptionT[F, Array[Byte]] =
    for {
      ws <- all
      wsMap <- OptionT.pure[F](ws.toIterable.toMap)
      longs <- OptionT.fromOption[F](words.traverse(w => wsMap.get(w)))
    } yield toByteArray(longs)

  def bytesToWords(bytes: Array[Byte]): OptionT[F, NonEmptyVector[String]] =
    for {
      ws <- all
      byteGroups <- OptionT.pure[F](bytes.grouped(2).toVector)
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
