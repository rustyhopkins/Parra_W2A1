package reductions

import java.util.concurrent._
import scala.collection._
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import common._

import ParallelParenthesesBalancing._

@RunWith(classOf[JUnitRunner])
class ParallelParenthesesBalancingSuite extends FunSuite {

  test("balance should work for empty string") {
    def check(input: String, expected: Boolean) =
      assert(balance(input.toArray) == expected,
        s"balance($input) should be $expected")

    check("", true)
  }

  test("balance should work for string of length 1") {
    def check(input: String, expected: Boolean) =
      assert(balance(input.toArray) == expected,
        s"balance($input) should be $expected")

    check("(", false)
    check(")", false)
    check(".", true)
  }

  test("balance should work for string of length 2") {
    def check(input: String, expected: Boolean) =
      assert(balance(input.toArray) == expected,
        s"balance($input) should be $expected")

    check("()", true)
    check(")(", false)
    check("((", false)
    check("))", false)
    check(".)", false)
    check(".(", false)
    check("(.", false)
    check(").", false)
  }

  test("parBalance should work for string of length 200") {
    def check(input: String, expected: Boolean) =
      assert(parBalance(input.toArray, 50) == expected,
        s"balance($input) should be $expected")
//
//    check("(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But," +
//      "why start now. 4 +(8 - (5/4))", true)
//    check("(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But," +
//      "why start now. 4 +(8 - (5/4)))", false) // One extra bracket at the end
//    check("((", false)
//    check("))", false)
//    check(".)", false)
//    check(".(", false)
//    check("(.", false)
//    check(").", false)
    check(longBalanacedString+longBalanacedString+longBalanacedString+longBalanacedString, true)
  }

  val longBalanacedString = "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))" +
    "(first)brackets now the second(might be worth testing). Should add some tests (TDD eat your heart out). But,why start now. 4 +(8 - (5/4))"



}