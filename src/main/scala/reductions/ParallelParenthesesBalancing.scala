package reductions

import scala.annotation._
import org.scalameter._
import common._

object ParallelParenthesesBalancingRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 120,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)
  @volatile var seqResult = false
  @volatile var parResult = false

  def main(args: Array[String]): Unit = {
    val length = 100000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime ms")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime ms")
    println(s"speedup: ${seqtime / fjtime}")
  }
}

object ParallelParenthesesBalancing {

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def balance(chars: Array[Char]): Boolean = {
    def balanced(c: List[Char], open: Int, close: Int): Boolean = {
      if (c.isEmpty)
        if (open == 0 && close == 0) true else false
      else {
        if (c.head == '(')
          balanced(c.tail, open + 1, close)
        else {
          if (c.head == ')') {
            if (open > 0) {
              balanced(c.tail, open - 1, close)
            } else {
              balanced(c.tail, open, close + 1)
            }
          } else {
            balanced(c.tail, open, close)
          }
        }
      }
    }
    balanced(chars.toList, 0, 0)
  }

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    def traverse(idx: Int, until: Int, arg1: Int, arg2: Int): (Int, Int) = {
      //      if (idx > until - 1) {
      //        (arg2, arg2)
      //      } else {
      //        if (chars(idx) == '(')
      //          traverse(idx + 1, until, arg1 + 1, arg2)
      //        else {
      //          if (chars(idx) == ')') {
      //            if (arg1 > 0) {
      //              traverse(idx + 1, until, arg1 - 1, arg2)
      //            } else {
      //              traverse(idx + 1, until, arg1, arg2 + 1)
      //            }
      //          } else {
      //            traverse(idx + 1, until, arg1, arg2)
      //          }
      //        }
      //      }
      //    }
      if (idx > until - 1) {
        (arg1, arg2)
      } else {
        chars(idx) match {
          case '(' =>
            traverse(idx + 1, until, arg1 + 1, arg2)
          case ')' => {
            if (arg1 >= 1)
              traverse(idx + 1, until, arg1 - 1, arg2)
            else
              traverse(idx + 1, until, arg1, arg2 + 1)
          }
          case _ => {
            traverse(idx + 1, until, arg1, arg2)
          }
        }
      }
    }

    def reduce(from: Int, until: Int) : (Int, Int) = {
      val length = until - from
      if (length <= threshold) {
//        println("Just going to Traverse no Parallel flashiness.")
        traverse(from, until, 0, 0)
      } else {
//        println("Lets get flash and do this in parallel")
        val mid = (from + until) / 2
        val ((l1, r1),(l2,r2)) = parallel(reduce(from, mid), reduce(mid, until))
//        println(s"open, close = (l1, r2)")
        val balanced = Math.min(l1, r2)
        (l1 + l2 - balanced, r1 + r2 - balanced)
      }
    }

    reduce(0, chars.length) == (0, 0)
  }

  // For those who want more:
  // Prove that your reduction operator is associative!

}
