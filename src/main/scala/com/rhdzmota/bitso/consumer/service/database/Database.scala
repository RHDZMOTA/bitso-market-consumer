package com.rhdzmota.bitso.consumer.service.database

import akka.Done
import akka.stream.scaladsl.Sink
import com.rhdzmota.bitso.consumer.model.book.Book.BookRow

import scala.concurrent.Future

trait Database {
  def sink: Sink[BookRow, Future[Done]]
}
