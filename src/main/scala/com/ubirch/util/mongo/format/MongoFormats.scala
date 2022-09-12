package com.ubirch.util.mongo.format

import org.joda.time.{ DateTime, DateTimeZone }
import reactivemongo.api.bson.{
  BSONBinary,
  BSONDateTime,
  BSONDocument,
  BSONDocumentReader,
  BSONDocumentWriter,
  BSONDouble,
  BSONHandler,
  BSONValue,
  Subtype
}

import scala.util.{ Failure, Success, Try }

/**
  * author: cvandrei
  * since: 2017-04-04
  */
trait MongoFormats {

  implicit protected object BSONDateTimeHandler extends BSONHandler[DateTime] {

    override def readTry(bson: BSONValue): Try[DateTime] = bson match {
      case BSONDateTime(long) => Success(new DateTime(long).withZone(DateTimeZone.UTC))
      case _                  => Failure(new IllegalArgumentException())
    }

    override def writeTry(t: DateTime): Try[BSONDateTime] = Try(BSONDateTime(t.withZone(DateTimeZone.UTC).getMillis))
  }

  implicit object BigDecimalHandler extends BSONHandler[BigDecimal] {
    def readTry(v: BSONValue): Try[BigDecimal] = v match {
      case BSONDouble(double) => Success(BigDecimal(double))
      case _                  => Failure(new IllegalArgumentException())
    }

    def writeTry(bd: BigDecimal): Success[BSONDouble] = Success(BSONDouble(bd.toDouble))
  }

  implicit object BigIntHandler extends BSONDocumentReader[BigInt] with BSONDocumentWriter[BigInt] {

    def writeTry(bigInt: BigInt): Success[BSONDocument] = Success(BSONDocument(
      "signum" -> bigInt.signum,
      "value" -> BSONBinary(bigInt.toByteArray, Subtype.UserDefinedSubtype)))

    def readDocument(doc: BSONDocument): Try[BigInt] = for {
      sig <- doc.getAsTry[Int]("signum")
      bin <- doc.getAsTry[BSONBinary]("value")
    } yield BigInt(sig, bin.byteArray)
  }

}
