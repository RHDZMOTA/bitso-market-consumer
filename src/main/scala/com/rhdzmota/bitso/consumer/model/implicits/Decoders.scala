package com.rhdzmota.bitso.consumer.model.implicits

import com.rhdzmota.bitso.consumer.model.book.Book.{CurrentBook, Position}
import com.rhdzmota.bitso.consumer.model.book.Book.Position.{Ask, Bid}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import com.rhdzmota.bitso.consumer.model.trades._
import com.rhdzmota.bitso.consumer.model.orders.Order._

object Decoders {
  implicit val decodeSingleTrade: Decoder[SingleTrade]  = deriveDecoder[SingleTrade]
  implicit val decodeTrades: Decoder[Trades]            = deriveDecoder[Trades]

  // SingleOrder ADT
  implicit val decodeOpen: Decoder[SingleOrder.Open] = deriveDecoder[SingleOrder.Open]
  implicit val decodeChangeStatus: Decoder[SingleOrder.ChangeStatus] = deriveDecoder[SingleOrder.ChangeStatus]
  implicit val decodeSingleOrder: Decoder[SingleOrder] =
    Decoder[SingleOrder.Open].map[SingleOrder](identity).or(Decoder[SingleOrder.ChangeStatus].map[SingleOrder](identity))

  // DiffOrders
  implicit val decodeDiffOrders: Decoder[DiffOrders] = deriveDecoder[DiffOrders]

  // Position ADT
  implicit val decodeAsk: Decoder[Ask] = deriveDecoder[Ask]
  implicit val decodeBid: Decoder[Bid] = deriveDecoder[Bid]
  implicit val decodePosition: Decoder[Position] =
    Decoder[Bid].map[Position](identity).or(Decoder[Ask].map[Position](identity))

  // CurrentBook
  implicit val decodePayload: Decoder[CurrentBook.Payload] = deriveDecoder[CurrentBook.Payload]
  implicit val decodeCurrentBook: Decoder[CurrentBook] = deriveDecoder[CurrentBook]
}
