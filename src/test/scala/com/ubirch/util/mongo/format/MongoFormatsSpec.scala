package com.ubirch.util.mongo.format

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-04-05
  */
class MongoFormatsSpec extends AnyFeatureSpec
  with Matchers {

  Feature("UUIDWriter.read()") {

    Scenario("simple BSONDocument w/o UUID") {

      // prepare
      val id = java.util.UUID.randomUUID().toString
      val bson: BSONDocument = document("id" -> id)

      // test
      val model = bson.as[ModelString]

      // verify
      model.id shouldBe id

    }

    Scenario("simple BSONDocument w/ UUID") {

      // prepare
      val id = java.util.UUID.randomUUID()
      val model = ModelUUID(id)

      // test
      val bson = ModelUUID.modelUUIDWriter.write(model)

      // verify
      val modelResult = bson.as[ModelUUID]
      modelResult shouldBe model

    }

  }

}

case class ModelString(id: String)

object ModelString {
  implicit def modelStringWriter: BSONDocumentWriter[ModelString] = Macros.writer[ModelString]
  implicit def modelStringReader: BSONDocumentReader[ModelString] = Macros.reader[ModelString]
}

case class ModelUUID(id: UUID)

object ModelUUID extends MongoFormats {
  implicit def modelUUIDWriter: BSONDocumentWriter[ModelUUID] = Macros.writer[ModelUUID]
  implicit def modelUUIDReader: BSONDocumentReader[ModelUUID] = Macros.reader[ModelUUID]
}
