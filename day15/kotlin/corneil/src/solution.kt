package day15

import main.utils.measureAndPrint
import main.utils.scanInts
import utils.*
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.max

fun main() {

  val test = readLines(
    """
    Sensor at x=2, y=18: closest beacon is at x=-2, y=15
    Sensor at x=9, y=16: closest beacon is at x=10, y=16
    Sensor at x=13, y=2: closest beacon is at x=15, y=3
    Sensor at x=12, y=14: closest beacon is at x=10, y=16
    Sensor at x=10, y=20: closest beacon is at x=10, y=16
    Sensor at x=14, y=17: closest beacon is at x=10, y=16
    Sensor at x=8, y=7: closest beacon is at x=2, y=10
    Sensor at x=2, y=0: closest beacon is at x=2, y=10
    Sensor at x=0, y=11: closest beacon is at x=2, y=10
    Sensor at x=20, y=14: closest beacon is at x=25, y=17
    Sensor at x=17, y=20: closest beacon is at x=21, y=22
    Sensor at x=16, y=7: closest beacon is at x=15, y=3
    Sensor at x=14, y=3: closest beacon is at x=15, y=3
    Sensor at x=20, y=1: closest beacon is at x=15, y=3
  """.trimIndent()
  )
  val input = readFile("day15")

  data class Sensor(val pos: Coord, val beacon: Coord) {
    val distance = pos.chebyshevDistance(beacon)
    fun isInRange(loc: Coord) = this.pos.chebyshevDistance(loc) <= distance
    fun deadSpots(row: Int): IntRange? {
      val distanceToRow = abs(row - pos.y)
      return if (distanceToRow <= distance) {
        val diff = distance - distanceToRow
        (pos.x - diff)..(pos.x + diff)
      } else {
        null
      }
    }
  }

  fun loadSensors(input: List<String>): List<Sensor> {
    return input.map { line ->
      line.scanInts().let { (a,b,c,d) ->
        Sensor(Coord(a,b), Coord(c,d))
      }
    }
  }

  fun calcDeadSpots(sensors: List<Sensor>, row: Int): Int {
    val beacons = sensors.map { it.beacon }
      .filter { it.y == row }
      .map { it.x }
      .toSet()

    val deadSpots = sensors.filter {
      it.pos.y <= row + it.distance && it.pos.y >= row - it.distance
    }.mapNotNull { it.deadSpots(row) }
      .flatMap { r ->
        beacons.flatMap { beacon ->
          r.exclude(beacon)
        }
      }
    return joinRanges(deadSpots).sumOf { it.last - it.first + 1 }
  }


  fun calcBeaconFrequency(sensors: List<Sensor>, size: Int): Long {
    val range = 0..size
    val beacons = sensors.map { it.beacon }.toSet()
    val minRow = sensors.minOfOrNull { min(it.pos.y, it.beacon.y) - it.distance } ?: 0
    val maxRow = sensors.maxOfOrNull { max(it.pos.y, it.beacon.y) + it.distance } ?: size
    val rows = max(minRow, 0)..min(maxRow, size)
    println("rows = $rows")
    for (row in rows) {
      val deadSpots = sensors
        .mapNotNull { it.deadSpots(row) }
        .filter { it.first in range || it.last in range }
        .sortedBy { it.first }
      val combined = joinRanges(deadSpots)
        .map { max(it.first, 0)..min(it.last, size) }
      for (index in 1..combined.lastIndex) {
        val a = combined[index - 1]
        val b = combined[index]
        val searchRange = (a.last + 1) until b.first
        for (x in searchRange) {
          if (x in range) {
            val loc = Coord(x, row)
            val found = !beacons.contains(loc) && sensors.filter {
              it.pos.y <= row + it.distance && it.pos.y >= row - it.distance
            }.none { it.isInRange(loc) }
            if (found) {
              return loc.x.toLong() * size.toLong() + loc.y.toLong()
            }
          }
        }
      }
    }
    return error("Hidden Beacon not found")
  }

  fun calcSolution1(input: List<String>, row: Int): Int {
    return calcDeadSpots(loadSensors(input), row)
  }


  fun calcSolution2(input: List<String>, size: Int): Long {
    return calcBeaconFrequency(loadSensors(input), size)
  }

  fun part1() {
    val testResult = calcSolution1(test, 10, true)
    println("Part 1 Test Answer = $testResult")
    check(testResult == 26) { "Expected 26 not $testResult" }
    val result = measureAndPrint("Part 1 Time:") {
      calcSolution1(input, 2000000, false)
    }
    println("Part 1 Answer = $result")
    check(result == 4502208) { "Expected 4502208 not $result" }
  }

  fun part2() {
    val testResult = measureAndPrint("Part 2 Test Time:") {
      calcSolution2(test, 4000000, true)
    }
    println("Part 2 Test Answer = $testResult")
    check(testResult == 56000011L) { "Expected 56000011 not $testResult" }
    val result = measureAndPrint("Part 2 Time:") {
      calcSolution2(input, 4000000, false)
    }
    println("Part 2 Answer = $result")
    check(result == 13784551204480L) { "Expected 13784551204480 not $result" }
  }
  println("Day - 15")
  part1()
  part2()
}
