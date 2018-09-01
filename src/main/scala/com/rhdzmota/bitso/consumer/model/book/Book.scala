package com.rhdzmota.bitso.consumer.model.book

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import com.rhdzmota.bitso.consumer.model.implicits.Decoders
import io.circe.parser.decode


sealed trait Book

object Book {

  // Single Position ADT
  sealed trait Position extends Book
  object Position {
    final case class Bid(o: String, r: Double, a: Double, v: Double, t: Int, d: Long, s: String) extends Position
    final case class Ask(o: String, r: Double, a: Double, v: Double, t: Int, d: Long, s: String) extends Position
  }

  final case class CurrentBook(`type`: String, book: String, payload: CurrentBook.Payload) extends Book {
    def toBookRow: List[BookRow] = payload.bidsToBookRow(book) ++ payload.asksToBookRow(book)
  }
  object CurrentBook {
    import Decoders._

    object Errors {
      sealed trait CurrentBookErrors
      final case class CirceError(e: io.circe.Error, string: String) extends CurrentBookErrors
      final case class NotTextMessage(string: String) extends CurrentBookErrors
    }

    final case class Payload(bids: List[Position.Bid], asks: List[Position.Ask]) {
      val time: Timestamp = Timestamp.valueOf(LocalDateTime.now())
      val bookId: UUID = UUID.randomUUID()
      def bidsToBookRow(book: String): List[BookRow] = bids.zipWithIndex.map({
        case (Position.Bid(o, r, a, v, t, d, s), index) => BookRow(
          bookId = bookId,
          received = time,
          arrival = index,
          book = book,
          orderId = o,
          rate = r,
          amount = a,
          value = v,
          position = "bid",
          milliseconds = d,
          status = s
        )
      })
      def asksToBookRow(book: String): List[BookRow] = asks.zipWithIndex.map({
        case (Position.Ask(o, r, a, v, t, d, s), index) => BookRow(
          bookId = bookId,
          received = time,
          arrival = index,
          book = book,
          orderId = o,
          rate = r,
          amount = a,
          value = v,
          position = "ask",
          milliseconds = d,
          status = s
        )
      })
    }
    def fromString(string: String): Either[Errors.CurrentBookErrors, CurrentBook] = decode[CurrentBook](string) match {
      case Left(e)      => Left(Errors.CirceError(e, string))
      case Right(value) => Right(value)
    }
  }

  final case class BookRow(
                            bookId: UUID,
                            received: Timestamp,
                            arrival: Int,
                            book: String,
                            orderId: String,
                            rate: Double,
                            amount: Double,
                            value: Double,
                            position: String,
                            milliseconds: Long,
                            status: String
                          )

}
