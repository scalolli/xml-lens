package net.michalsitko.xml.optics

import monocle.function.Index
import monocle.{Lens, Optional, Traversal}
import net.michalsitko.xml.entities.{Element, LabeledElement, Node, ResolvedName}

trait LabeledElementOptics {
  def deep(elementMatcher: NameMatcher): Traversal[LabeledElement, Element] =
    element.composeTraversal(ElementOptics.deeper(elementMatcher))

  def deep(label: String): Traversal[LabeledElement, Element] =
    deep(NameMatcher.fromString(label))

  def isLabeled(elementMatcher: NameMatcher): Optional[LabeledElement, Element] =
    Optional[LabeledElement, Element]{ labeled =>
      if (elementMatcher.matches(labeled.label)) {
        Some(labeled.element)
      } else {
        None
      }
    }{ newElem => labeled =>
      if(elementMatcher.matches(labeled.label)) {
        labeled.copy(element = newElem)
      } else {
        labeled
      }
    }

  def isLabeled(label: String): Optional[LabeledElement, Element] =
    isLabeled(NameMatcher.fromString(label))

  val element: Lens[LabeledElement, Element] = Lens[LabeledElement, Element](_.element){ newElement =>from =>
    from.copy(element = newElement)
  }

  val children = element.composeLens(ElementOptics.children)

  val label: Lens[LabeledElement, ResolvedName] =
    Lens[LabeledElement, ResolvedName](_.label)(newLabel => from => from.copy(label = newLabel))

  val localName = label.composeLens(ResolvedNameOptics.localName)

  val index: Index[LabeledElement, Int, Node] = new Index[LabeledElement, Int, Node] {
    override def index(i: Int): Optional[LabeledElement, Node] =
      element.composeOptional(ElementOptics.indexOptional(i))
  }
}

object LabeledElementOptics extends LabeledElementOptics
