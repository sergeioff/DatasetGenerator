package com.pogorelovs.data

import java.io.{BufferedWriter, FileWriter}

import com.github.javafaker.Faker
import me.tongfei.progressbar.ProgressBar

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object DatasetGenerator extends App {

  val numberOfRecords = Try {
    args(0).toInt
  } match {
    case Success(value) => value
    case Failure(e) => 0
  }

  if (numberOfRecords < 1) {
    println("Provide number of records as first argument")
    System.exit(-1)
  }

  val printer = new BufferedWriter(new FileWriter(s"$numberOfRecords.csv"))
  val faker = new Faker()

  val progress = new ProgressBar("Generating", numberOfRecords)

  val ranges = getRanges

  implicit val ec: ExecutionContext = ExecutionContext.global

  val futuresToWait: Seq[Future[Unit]] = for (range <- ranges) yield Future {
    (range._1 to range._2).foreach {
      _ => {
        printer.write(makeCSVLine(faker.name().fullName(), faker.address().fullAddress()))
        printer.flush()

        progress.step()
      }
    }
  }

  Await.result(Future.sequence(futuresToWait), 15 minutes)

  printer.close()
  progress.close()

  private def makeCSVLine(parts: Any*): String = {
    parts.map {
      case str: String => s""""$str""""
      case any => any
    }.mkString(sep = ",", end = "\n", start = "")
  }

  private def getRanges: Seq[(Int, Int)] = {
    val parallelism = Runtime.getRuntime.availableProcessors()
    val part = numberOfRecords / parallelism
    val ranges = (1 to parallelism).map { i => (part * (i - 1) + 1, part * i) }
    val lastRange = ranges.last
    ranges.take(parallelism - 1) :+ (lastRange._1, numberOfRecords) // Fix last range to match total number of records
  }
}

