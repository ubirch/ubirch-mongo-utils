package com.ubirch.util.mongo.format

import org.joda.time.{ DateTime, DateTimeZone }
import reactivemongo.api.bson.{
  BSONBinary,
  BSONDateTime,
  BSONDocument,
  BSONDocumentHandler,
  BSONDocumentReader,
  BSONDocumentWriter,
  BSONHandler,
  BSONValue,
  Subtype
}

import java.nio.ByteBuffer
import java.util.UUID
import scala.util.Try

/**
  * author: cvandrei
  * since: 2017-04-04
  */
trait MongoFormats {

  implicit protected object uuidHandler extends BSONHandler[UUID] {
    override def writeTry(id: UUID): Try[BSONValue] =
      Try {
        val bb = ByteBuffer.wrap(new Array[Byte](16))
        bb.putLong(id.getMostSignificantBits)
        bb.putLong(id.getLeastSignificantBits)
        BSONBinary(bb.array(), Subtype.UuidSubtype)
      }

    override def readTry(bson: BSONValue): Try[UUID] = {
      bson.asTry[BSONBinary].map { binary =>
        val byteBuffer = ByteBuffer.wrap(binary.byteArray)
        val high = byteBuffer.getLong()
        val low = byteBuffer.getLong()
        new UUID(high, low)
      }
    }
  }

  implicit protected object BSONDateTimeHandler extends BSONHandler[DateTime] {

    override def readTry(bson: BSONValue): Try[DateTime] =
      bson
        .asTry[BSONDateTime]
        .map { bsonDateTime =>
          new DateTime(bsonDateTime.value).withZone(DateTimeZone.UTC)
        }

    override def writeTry(dateTime: DateTime): Try[BSONDateTime] =
      Try(BSONDateTime(dateTime.withZone(DateTimeZone.UTC).getMillis))
  }

  implicit object BigIntHandler extends BSONDocumentReader[BigInt] with BSONDocumentWriter[BigInt] {

    def writeTry(bigInt: BigInt): Try[BSONDocument] = Try(BSONDocument(
      "signum" -> bigInt.signum,
      "value" -> BSONBinary(bigInt.toByteArray, Subtype.UserDefinedSubtype)))

    def readDocument(doc: BSONDocument): Try[BigInt] =
      for {
        sig <- doc.getAsTry[Int]("signum")
        bin <- doc.getAsTry[BSONBinary]("value")
      } yield BigInt(sig, bin.byteArray)
  }

  implicit object BigDecimalHandler extends BSONDocumentHandler[BigDecimal] {

    override def writeTry(bd: BigDecimal): Try[BSONDocument] = Try {
      BSONDocument(
        "scale" -> bd.scale,
        "precision" -> bd.precision,
        "value" -> BigInt(bd.underlying.unscaledValue())
      )
    }

    override def readDocument(doc: BSONDocument): Try[BigDecimal] =
      for {
        value <- doc.getAsTry[BigInt]("value")
        scale <- doc.getAsTry[Int]("scale")
        precision <- doc.getAsTry[Int]("precision")
      } yield {
        val mc = new java.math.MathContext(precision)
        BigDecimal.apply(value, scale, mc)
      }
  }
}
