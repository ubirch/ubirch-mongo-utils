package com.ubirch.util.mongo.connection

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.util.mongo.config.{ MongoConfig, MongoConfigKeys }
import com.ubirch.util.mongo.connection.Exceptions._
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.{ AsyncDriver, MongoConnection }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{ Failure, Success, Try }

trait ConnectionTrait {

  def driver: AsyncDriver

  def uri: String

  def parsedUri: Future[ParsedURI]

  def conn: Future[MongoConnection]

  def close(): Unit = driver.close()

  def closeLogical(): Unit = {
    conn.map(_.close()(2.seconds))
  }

  def connIsActive: Future[Boolean] = conn.map(_.active)

}

class Connection private (configPrefix: String) extends ConnectionTrait {

  val driver: AsyncDriver = new reactivemongo.api.AsyncDriver
  val uri: String = MongoConfig.hosts(configPrefix)
  val parsedUri: Future[ParsedURI] = MongoConnection.fromString(uri)
  val conn: Future[MongoConnection] = parsedUri.flatMap((uri: ParsedURI) => driver.connect(uri))

}

object Connection extends LazyLogging {

  private var connection: Option[Connection] = None

  def get(configPrefix: String = MongoConfigKeys.PREFIX): Connection = synchronized {

    connection.orElse {
      Try(new Connection(configPrefix)) match {
        case Success(conn) =>
          connection = Some(conn)
          connection

        case Failure(e) =>
          val errorMessage = "(1) Something went wrong when getting Connection: " + e.getMessage
          logger.error(errorMessage)
          throw GettingConnectionException(errorMessage)
      }
    }.getOrElse {
      val errorMessage = "(2) Something went wrong when getting Connection."
      logger.error(errorMessage)
      throw GettingConnectionException(errorMessage)
    }
  }

}
