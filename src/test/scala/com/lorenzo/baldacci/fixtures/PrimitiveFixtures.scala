package com.lorenzo.baldacci.fixtures

import com.lorenzo.baldacci.search.Summary
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, LocalDate, Months}

import scala.util.Random

trait PrimitiveFixtures {

  def someSmallString: String = Random.alphanumeric.take(8).mkString

  def someString: String = Random.alphanumeric.take(20).mkString

  def someBigNumber: Int = Random.nextInt(Int.MaxValue - 1)

  def someNumber: Int = Random.nextInt(99999)

  def someSmallNumber: Int = Random.nextInt(999)

  def someAge: Int = Random.nextInt(99)

  def someTinyNumber: Int = Random.nextInt(10) + 1

  def someNumberUnder(max:Int): Int = Random.nextInt(max) + 1
  def someNumberBetween(min:Int, max:Int): Int = min + someNumberUnder(max - min)

  def pickOneOf[T](seq: Seq[T]): T = seq(Random.nextInt(seq.size))

  def pickSomeOf[T](seq: Seq[T]): Seq[T] = {
    val size = Random.nextInt(seq.size)
    Random.shuffle(seq).take(size)
  }

  def pickSomeOf[T <: Enumeration](enum: T): Seq[enum.Value] = pickSomeOf(enum.values.toSeq)

  def someSmallSequenceOf[T](gen: => T): Seq[T] = (1 to someTinyNumber).map(_ => gen)

  def allExcept[T](seq: Seq[T], exception: T): Seq[T] = seq.filterNot(_.equals(exception))

  def someDateInTheNextYear: LocalDate = LocalDate.now(UTC).plusDays(Random.nextInt(365) + 1)

  def someDateInThePastYear: LocalDate = LocalDate.now(UTC).minusDays(Random.nextInt(365) + 1)

  def someDateWithMonthBetween(initialDate:LocalDate, finalDate:LocalDate):LocalDate = {
    val monthsDiff = Months.monthsBetween(initialDate, finalDate).getMonths
    initialDate.plusMonths(someNumberUnder(monthsDiff))
  }

  def somePastDateTime: DateTime = someDateInThePastYear.toDateTimeAtCurrentTime

  def someFutureDateTime: DateTime = someDateInTheNextYear.toDateTimeAtCurrentTime

  def someBoolean: Boolean = Random.nextBoolean()

  def someEmailString: String = Random.alphanumeric.take(8).mkString + "@mail.com"

  def someSmallText: String = someSmallSequenceOf(someSmallString).mkString(" ")

  def somePaper: Summary = Summary(Option(someSmallString), Option(someSmallText))
}
