package com.github.andyglow.scalacheck

import java.time._
import java.util.{Calendar, Date}

import com.github.andyglow.util.Result
import com.github.andyglow.util.Result._
import org.scalacheck.Gen

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.control.NonFatal


trait ForConst[T] { self =>

  def crack(x: String): T

  def apply(x: String): Result[Gen[T]]

  def map[R](f: T => R): ForConst[R] = new ForConst[R] {

    def crack(x: String): R = f(self crack x)

    def apply(x: String): Result[Gen[R]] = for {
      g <- self(x)
    } yield g map f
  }
}

object ForConst {

  implicit val stringFromString: ForConst[String] = create(_.trim)

  implicit val charFromString: ForConst[Char] = create(_.head)

  implicit val byteFromString: ForConst[Byte] = create(_.toByte)

  implicit val shortFromString: ForConst[Short] = create(_.toShort)

  implicit val intFromString: ForConst[Int] = create(_.toInt)

  implicit val longFromString: ForConst[Long] = create(_.toLong)

  implicit val floatFromString: ForConst[Float] = create(_.toFloat)

  implicit val doubleFromString: ForConst[Double] = create(_.toDouble)

  implicit val booleanFromString: ForConst[Boolean] = create(_.toBoolean)

  implicit val localDateTimeFromString: ForConst[LocalDateTime] = create(Tempo.parseLocalDateTime)

  implicit val localDateFromString: ForConst[LocalDate] = create(Tempo.parseLocalDate)

  implicit val localTimeFromString: ForConst[LocalTime] = create(Tempo.parseLocalTime)

  implicit val instantFromString: ForConst[Instant] = create(Tempo.parseInstant)

  implicit val zonedDateTimeFromString: ForConst[ZonedDateTime] = create(Tempo.parseZonedDateTime)

  implicit val offsetDateTimeFromString: ForConst[OffsetDateTime] = create(Tempo.parseOffsetDateTime)

  implicit val calendarFromString: ForConst[Calendar] = create(Tempo.parseCalendar)

  implicit val dateFromString: ForConst[Date] = localDateTimeFromString map { x => Date.from(x.toInstant(Tempo.systemZoneOffset)) }

  implicit val sqlDateFromString: ForConst[java.sql.Date] = localDateFromString map { java.sql.Date.valueOf }

  implicit val sqlTimeFromString: ForConst[java.sql.Time] = localTimeFromString map { java.sql.Time.valueOf }

  implicit val finiteDurationFromString: ForConst[FiniteDuration] = create(Tempo.parseFiniteDuration)

  implicit val durationFromString: ForConst[Duration] = create {
    case "inf" | "∞"    => Duration.Inf
    case "-inf" | "-∞"  => Duration.MinusInf
    case str            => Tempo.parseFiniteDuration(str)
  }

  private def create[T](fn: String => T): ForConst[T] = new ForConst[T] {

    def crack(x: String): T = fn(x)

    def apply(x: String): Result[Gen[T]] =
      try Ok(Gen.const(fn(x))) catch {
        case NonFatal(err) => Error(err.getMessage)
      }
  }

  def parse[T](dfn: String)(implicit fc: ForConst[T]): Result[Gen[T]] = fc(dfn)
}