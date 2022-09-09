package com.ubirch.util.mongo.connection

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.util.mongo.connection.Exceptions.{
  CollectionException,
  DatabaseConnectionException,
  NoDBNameFoundException
}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{ DB, FailoverStrategy, MongoConnection }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DatabaseTrait {

  val connection: Connection
  val failoverStrategy: FailoverStrategy

  def db: Future[DB]

  def collection(name: String): Future[BSONCollection]

  def getNameFromURI: Future[String] = {
    connection.parsedUri.map { x =>
      x.db.filter(_.nonEmpty)
        .getOrElse(throw NoDBNameFoundException("No DB Found in URI."))
    }

  }

}

class Database(val connection: Connection, val failoverStrategy: FailoverStrategy)
  extends DatabaseTrait
  with LazyLogging {

  def futureConnection: Future[MongoConnection] = connection.conn

  def db(name: String): Future[DB] = {
    val _db = for {
      conn <- futureConnection
      database <- conn.database(name, failoverStrategy)
    } yield {
      database
    }

    _db.recover {
      case e: Exception =>
        val errorMessage = s"Something went wrong when getting Database Connection (db($name)) {}"
        logger.error(errorMessage, e.getMessage)
        throw DatabaseConnectionException(e.getMessage)
    }
  }

  def db: Future[DB] = {

    getNameFromURI
      .flatMap(db)
      .recover {
        case e: Exception =>
          val errorMessage = "Something went wrong when getting Database Connection (db) {}"
          logger.error(errorMessage, e.getMessage)
          throw DatabaseConnectionException(e.getMessage)
      }

  }

  def collection(name: String): Future[BSONCollection] = {

    db.map { db =>
      db.collection[BSONCollection](name)
    }.recover {
      case e: Exception =>
        val errorMessage = "Something went wrong when running Collection. Got this: {}"
        logger.error(errorMessage, e.getMessage)
        throw CollectionException(e.getMessage)
    }

  }

  def this(connection: Connection) = this(connection, FailoverStrategy.remote)

}
