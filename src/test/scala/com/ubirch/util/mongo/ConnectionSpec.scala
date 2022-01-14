package com.ubirch.util.mongo

import com.ubirch.util.mongo.connection.Connection
import com.ubirch.util.mongo.connection.Exceptions.GettingConnectionException
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar

import scala.language.postfixOps

class ConnectionSpec
  extends AnyFeatureSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with Matchers
    with MockitoSugar {

  Feature("A ConnectionSpec") {

    Scenario("Connection.get fails when prefix key is empty or non-existent") {

      assertThrows[GettingConnectionException](Connection.get(""))

      assertThrows[GettingConnectionException](Connection.get("this.is.my.imaginary.path"))


    }

    Scenario("Connection.connIsActive checks if the logical connection has been created (POOL)") {

      val connection = Connection.get()

      Thread.sleep(3000)

      assert(connection.connIsActive)

      Thread.sleep(3000)

      connection.closeLogical()

      Thread.sleep(3000)


    }


  }

}
