package com.rhdzmota.bitso.consumer.service.bitso

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.Done
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.rhdzmota.bitso.consumer.conf.{Context, Settings}
import com.rhdzmota.bitso.consumer.model.book.Book.{BookRow, CurrentBook}

import scala.concurrent.{Future, Promise}

case object Bitso extends Context {

  val bookSource: Source[Message, Promise[Option[Message]]] = Source(List(
    TextMessage(Settings.Bitso.Coins.btcmxn.exchange.orders),
    TextMessage(Settings.Bitso.Coins.ethmxn.exchange.orders),
    TextMessage(Settings.Bitso.Coins.xrpmxn.exchange.orders),
    TextMessage(Settings.Bitso.Coins.ltcmxn.exchange.orders),
    TextMessage(Settings.Bitso.Coins.bchmxn.exchange.orders),
    TextMessage(Settings.Bitso.Coins.tusdmxn.exchange.orders),
  )).concatMat(Source.maybe[Message])(Keep.right)

  def printSink: Sink[Message, Future[Done]] =
    Sink.foreach(println)

  def bookSink(databaseSink: Sink[BookRow, Future[Done]]): Sink[Message, Future[Done]] =
    Flow[Message]
      .map({
        case message: TextMessage.Strict  => CurrentBook.fromString(message.text)
        case message                      => Left(CurrentBook.Errors.NotTextMessage(message.toString))})
      .mapConcat({
        case Right(book)  => book.toBookRow
        case Left(error)    => error match {
          case CurrentBook.Errors.NotTextMessage(string)   =>
            val t = Timestamp.valueOf(LocalDateTime.now()).toString
            println(s"[Error][NotTextMessage] $t The object received is not a TextMessage.String : $string")
          case CurrentBook.Errors.CirceError(_, s) =>
            val t = Timestamp.valueOf(LocalDateTime.now()).toString
            println(s"[Error][Circe] $t $s")
        }
          List[BookRow]()
      })
      .toMat(databaseSink)(Keep.right)
}
