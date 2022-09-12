package com.ubirch.util.mongo.connection

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.mongo.config.MongoConfigKeys
import com.ubirch.util.mongo.connection.Exceptions.{ CollectionException, DatabaseConnectionException }
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{ document, BSONDocumentReader, BSONDocumentWriter }
import reactivemongo.api.{ DB, FailoverStrategy }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future, TimeoutException }
import scala.language.postfixOps
import scala.util.Try

/**
  * author: cvandrei
  * since: 2017-03-31
  */
class MongoUtil(configPrefix: String = MongoConfigKeys.PREFIX) extends StrictLogging {

  def conn: Connection = Connection.get(configPrefix)

  /**
    * Connects us to a collection.
    *
    * @param name collection names
    * @return collection connection
    */
  def collection(name: String): Future[BSONCollection] = {
    (for {
      dbName <- conn.databaseName
      connection <- conn.conn
      database <- connection.database(dbName)
      collection = database.collection[BSONCollection](name)
    } yield {
      collection
    }).recover {
      case e: Exception =>
        val errorMessage = "Something went wrong when retrieving Collection. Got this: {}"
        logger.error(errorMessage, e.getMessage, e)
        throw CollectionException(e.getMessage)
    }
  }

  def db(): Future[DB] = {
    (for {
      dbName <- conn.databaseName
      connection <- conn.conn
      database <- connection.database(dbName)
    } yield {
      database
    }).recover {
      case e: Exception =>
        val errorMessage = "Something went wrong when retrieving Database. Got this: {}"
        logger.error(errorMessage, e.getMessage, e)
        throw DatabaseConnectionException(e.getMessage)
    }
  }

  /**
    * Close the connection and other related connection related resources.
    */
  def close(): Unit = conn.close()

  /**
    * Check database connectivity by querying the given collection
    *
    * @param collectionName name of collection to query
    * @tparam T type of resulting objects
    * @return deep check response with _status:OK_ if ok; otherwise with _status:NOK_
    */
  def connectivityCheck[T <: Any](collectionName: String)(
    implicit writer: BSONDocumentWriter[T],
    reader: BSONDocumentReader[T]): Future[DeepCheckResponse] = {

    if (checkConnection()) {

      logger.debug("DB Connection exists.")

      collection(collectionName)
        .flatMap {
          _.find(document(), None)
            .one[T]
            .map(_ => DeepCheckResponse())
        }.recover {

          case e: Exception =>
            DeepCheckResponse(
              status = false,
              messages = Seq(e.getMessage)
            )

        }
    } else {

      logger.debug("DB Connection does not exist.")

      Future.successful(
        DeepCheckResponse(
          status = false,
          messages = Seq("No Mongo Connection")
        )
      )

    }

  }

  def checkConnection(atMost: FiniteDuration = 2 seconds): Boolean = {

    val futureChecks: Future[Boolean] =
      (for {
        dbName <- conn.databaseName
        connection <- conn.conn
        database <- connection.database(dbName, FailoverStrategy.remote)
        collectionNames <- database.collectionNames
      } yield {
        database.connection.active && collectionNames.nonEmpty
      }).recover {
        case _: NoSuchElementException =>
          logger.error("Check Predicates are not Satisfied")
          false
        case e: Exception =>
          logger.error("No DB Connection: {} ", e.getMessage)
          false
      }

    Try(Await.result(futureChecks, atMost)).recover {
      case e: TimeoutException =>
        logger.error(s"(1) It is taking more than $atMost to retrieve checks. Got this error ${e.getMessage}", e)
        false

      case e =>
        logger.error(s"Something went wrong when running checks. Got this: ${e.getMessage} ", e)
        false

    }.getOrElse {
      false
    }

  }

}
