package com.rhdzmota.bitso.consumer.service.database.impl

import com.rhdzmota.bitso.consumer.service.database.Database
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.Done
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
import com.rhdzmota.bitso.consumer.conf.{Context, Settings}
import com.rhdzmota.bitso.consumer.model.book.Book.BookRow

import scala.concurrent.Future


case object Cassandra extends Database with Context {

  implicit private val session: Session = Cluster.builder
    .addContactPoint(Settings.Cassandra.address)
    .withPort(Settings.Cassandra.port)
    .build.connect()

  private val query: String =
    s"""
       |INSERT INTO ${Settings.Cassandra.keyspaceName}.${Settings.Cassandra.market.table}(book_id, received, arrival, book, order_id, rate, amount, value, position, milliseconds, status)
       |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
     """.stripMargin

  private val preparedStatement: PreparedStatement = session.prepare(query)

  private val statementBinder: (BookRow, PreparedStatement) => BoundStatement =
    (bookRow: BookRow, statement: PreparedStatement) => statement.bind(
      bookRow.bookId,
      bookRow.received,
      bookRow.arrival.asInstanceOf[java.lang.Integer],
      bookRow.book,
      bookRow.orderId,
      bookRow.rate.asInstanceOf[java.lang.Double],
      bookRow.amount.asInstanceOf[java.lang.Double],
      bookRow.value.asInstanceOf[java.lang.Double],
      bookRow.position,
      bookRow.milliseconds.asInstanceOf[java.lang.Long],
      bookRow.status
    )


  private val cassandraSink: Sink[BookRow, Future[Done]] = CassandraSink[BookRow](
    Settings.Cassandra.orders.parallelism,
    preparedStatement,
    statementBinder
  )

  override def sink: Sink[BookRow, Future[Done]] = Flow[BookRow].toMat(cassandraSink)(Keep.right)

}
