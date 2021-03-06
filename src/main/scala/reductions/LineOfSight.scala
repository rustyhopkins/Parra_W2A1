package reductions

import org.scalameter._
import common._

object LineOfSightRunner {
  
  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 100,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]) {
    val length = 10000000
    val input = (0 until length).map(_ % 100 * 1.0f).toArray
    val output = new Array[Float](length + 1)
    val seqtime = standardConfig measure {
      LineOfSight.lineOfSight(input, output)
    }
    println(s"sequential time: $seqtime ms")

    val partime = standardConfig measure {
      LineOfSight.parLineOfSight(input, output, 10000)
    }
    println(s"parallel time: $partime ms")
    println(s"speedup: ${seqtime / partime}")
  }
}

object LineOfSight {

  def max(a: Float, b: Float): Float = if (a > b) a else b

  def lineOfSight(input: Array[Float], output: Array[Float]): Unit = {
    //TODO Come back and implement this with a more elegant solution eg list.zipwithindex and match statement
    output(0) = 0
    for (i <- 1 to input.length - 1) {
      output(i) = Math.max(input(i)/ i, output(i - 1))
    }
  }

  sealed abstract class Tree {
    def maxPrevious: Float
  }

  case class Node(left: Tree, right: Tree) extends Tree {
    val maxPrevious = max(left.maxPrevious, right.maxPrevious)
  }

  case class Leaf(from: Int, until: Int, maxPrevious: Float) extends Tree

  /** Traverses the specified part of the array and returns the maximum angle.
   */
  def upsweepSequential(input: Array[Float], from: Int, until: Int): Float = {
    // TODO Improve this muck.  Surely foldleft should work
    var max = 0f
    for (i <- from until until) {
      max = math.max(max, input(i) / i)
    }
    max
  }

//    https://alvinalexander.com/scala/how-to-use-zipwithindex-create-for-loop-counters-scala-cookbook
//    val result = input.zipWithIndex.foreach {
//      case (xs, 0) => 0f
//      case (xs, i) => math.max(xs / i, input(i))
//    }
//
//  input.zipWithIndex.foldLeft(0f){ (e, max) => math.max(e/i, max)
//
//    val max = math.max(max, input(i - 1)/i)
//    if (now > 10) return x._2
//    else now
//  }
//    0f
//  }

  /** Traverses the part of the array starting at `from` and until `end`, and
   *  returns the reduction tree for that part of the array.
   *
   *  The reduction tree is a `Leaf` if the length of the specified part of the
   *  array is smaller or equal to `threshold`, and a `Node` otherwise.
   *  If the specified part of the array is longer than `threshold`, then the
   *  work is divided and done recursively in parallel.
   */
  def upsweep(input: Array[Float], from: Int, end: Int,
    threshold: Int): Tree = {
    val length = end - from
    if (length <= threshold) {
      Leaf(from, end, upsweepSequential(input, from, end))
    } else {
      val mid = (from + end) / 2
      val (l, r) = parallel(upsweep(input, from, mid, threshold), upsweep(input, mid, end, threshold))
      Node(l, r)
    }
  }

  /** Traverses the part of the `input` array starting at `from` and until
   *  `until`, and computes the maximum angle for each entry of the output array,
   *  given the `startingAngle`.
   */
  def downsweepSequential(input: Array[Float], output: Array[Float],
    startingAngle: Float, from: Int, until: Int): Unit = {
    var maxAngle = startingAngle
    for (i <- from until until) {
      output(i) = math.max(input(i)/i, startingAngle)

    }

//    input.foldLeft(startingAngle) { (elem1, elem2) =>
//      math.max(elem1/i, elem2)
//    }

  }

  /** Pushes the maximum angle in the prefix of the array to each leaf of the
   *  reduction `tree` in parallel, and then calls `downsweepSequential` to write
   *  the `output` angles.
   */
  def downsweep(input: Array[Float], output: Array[Float], startingAngle: Float,
    tree: Tree): Unit = {

    tree match {
      case Leaf(from, until, maxPrevious) => downsweepSequential(input, output, startingAngle, from, until)
      case Node(l, r) => parallel(downsweep(input, output, startingAngle, l), downsweep(input, output, startingAngle, r))
    }
  }

  /** Compute the line-of-sight in parallel. */
  def parLineOfSight(input: Array[Float], output: Array[Float],
    threshold: Int): Unit = {
    // Calculate the Tree containing maximum angles
    val theTree = upsweep(input, 0, input.length, 10)
    // Now just downsweep ... maybe???
    downsweep(input, output, 0f, theTree)
  }
}
