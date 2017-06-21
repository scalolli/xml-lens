package net.michalsitko.xml.optics

import monocle.Traversal
import net.michalsitko.xml.entities.Element
import net.michalsitko.xml.optics.Optics._
import net.michalsitko.xml.parsing.XmlParser
import org.scalatest.{Matchers, WordSpec}

class NamespaceSpec extends WordSpec with Matchers {
  "Optics" should {
    "should respect namespaces" in new Context {
      val xml = XmlParser.parse(input).right.get

      val ignoreNs = withCriteria(deeper("f"))
      val withNsA = {
        val ns = Namespace("a", "http://a.com")
        withCriteria(deeper(ns.name("f")))
      }
      val withNsB = {
        val ns = Namespace("b", "http://b.com")
        withCriteria(deeper(ns.name("f")))
      }

      ignoreNs.getAll(xml) should equal(List("a.com", "b.com", "c.com", "d.com"))
      withNsA.getAll(xml) should equal(List("a.com"))
      withNsB.getAll(xml) should equal(List("b.com"))
    }

    "should allow to use default namespace" in new Context {
      val xml = XmlParser.parse(input2).right.get

      val defaultNs = {
        val ns = Namespace.default
        withCriteria(deeper(ns.name("f")))
      }

      defaultNs.getAll(xml) should equal(List("here"))
    }

  }
}

trait Context {
  val input =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a xmlns="http://a.com" xmlns:b="http://b.com">
      |   <c1>
      |      <f>a.com</f>
      |      <b:f>b.com</b:f>
      |   </c1>
      |   <c1 xmlns="http://c.com" xmlns:b="http://d.com">
      |      <f>c.com</f>
      |      <b:f>d.com</b:f>
      |   </c1>
      |</a>""".stripMargin

  val input2 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a xmlns:b="http://b.com">
      |   <c1>
      |      <f>here</f>
      |      <b:f>b.com</b:f>
      |   </c1>
      |</a>""".stripMargin

  def withCriteria(criteria: Traversal[Element, Element]) = {
    deep("c1").composeTraversal(criteria).composeOptional(hasTextOnly)
  }
}