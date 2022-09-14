package com.ubirch.util.mongo.format

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import reactivemongo.api.bson.{ document, BSONDocument, BSONDocumentHandler, Macros }

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-04-05
  */

class MongoFormatsSpec extends AnyFeatureSpec with MongoFormats with Matchers {

  Feature("UUIDWriter.read()") {

    Scenario("simple BSONDocument w/o UUID") {
      // prepare
      val id = java.util.UUID.randomUUID().toString
      val bson: BSONDocument = document("id" -> id)

      // test
      val model = ModelString.modelStringHandler.readTry(bson)

      // verify
      model.isSuccess shouldBe true
      model.get.id shouldBe id
    }

    Scenario("simple BSONDocument w/ UUID") {
      // prepare
      val id = java.util.UUID.randomUUID()
      val model = ModelUUID(id)
      // test
      val bson = ModelUUID.modelUUIDHandler.writeTry(model)

      // verify
      val modelResult = ModelUUID.modelUUIDHandler.readTry(bson.get)
      assert(modelResult.isSuccess)
      modelResult.get shouldBe model
    }
  }

}

case class ModelString(id: String)

object ModelString {
  implicit val modelStringHandler: BSONDocumentHandler[ModelString] = Macros.handler[ModelString]
}

case class ModelUUID(id: UUID)

object ModelUUID extends MongoFormats {
  implicit val modelUUIDHandler: BSONDocumentHandler[ModelUUID] = Macros.handler[ModelUUID]
}
