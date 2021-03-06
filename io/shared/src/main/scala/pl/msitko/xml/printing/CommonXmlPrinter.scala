package pl.msitko.xml.printing

import pl.msitko.xml.entities._
import pl.msitko.xml.printing.Indent.IndentWith
import Syntax._

private [printing] trait CommonXmlPrinter {
  def print(doc: XmlDocument)(implicit cfg: PrinterConfig = PrinterConfig.Default): String
}

private [printing] object CommonXmlWriter extends Resolver {
  val systemEol = System.getProperty("line.separator")

  def writeProlog[M : InternalMonoid](prolog: Prolog, eolAfterXmlDecl: Boolean)(writer: M): M = {
    def xmlDeclStr(decl: XmlDeclaration): String = {
      val encStr = decl.encoding.map(enc => s""" encoding="$enc"""").getOrElse("")
      val eol = if(eolAfterXmlDecl) systemEol else ""
      s"""<?xml version="${decl.version}"$encStr?>$eol"""
    }

    def doctypeDeclStr(doctypeDeclaration: DoctypeDeclaration): String = {
      doctypeDeclaration.text
    }

    val declarationStr = prolog.xmlDeclaration.map { decl =>
      xmlDeclStr(decl)
    }.getOrElse("")

    val writer1 = writer.combine(declarationStr)

    val writer2 = prolog.miscs.foldLeft(writer1) { (acc, misc) =>
      writeMisc(misc)(acc)
    }
    prolog.doctypeDeclaration.map { decl =>
      val tmp = writer2.combine(doctypeDeclStr(decl._1))
      prolog.miscs.foldLeft(tmp) { (acc, misc) =>
        writeMisc(misc)(acc)
      }
    }.getOrElse(writer2)
  }

  def writeMisc[M : InternalMonoid](misc: Misc)(writer: M): M = misc match {
    case c: Comment =>
      writeComment(c)(writer)
    case pi: ProcessingInstruction =>
      writeProcessingInstruction(pi)(writer)
  }

  def writeText[M : InternalMonoid](text: Text)(writer: M, cfg: PrinterConfig): M = {
    cfg.indent match {
      // TODO: we should instead of this assume that it's minimized, but for now minimize is defined only in optics
      case _: IndentWith if text.text.forall(_.isWhitespace) => writer
      case _ => Escaper.escapeText(text.text)(writer)
    }
  }

  def writeProcessingInstruction[M : InternalMonoid](node: ProcessingInstruction)(writer: M): M =
    writer
      .combine("<?")
      .combine(node.target)
      .combine(" ")
      .combine(node.data)
      .combine("?>")

  // TDSect according to https://www.w3.org/TR/xml/#NT-CDSect
  def writeCData[M : InternalMonoid](node: CData)(writer: M): M = {
    val cdataStr = s"<![CDATA[${node.text}]]>"
    writer.combine(cdataStr)
  }

  def writeEntityReference[M : InternalMonoid](node: EntityReference)(writer: M): M = {
    val entityStr = s"&${node.name};"
    writer.combine(entityStr)
  }

  // Comment according to https://www.w3.org/TR/xml/#NT-Comment
  def writeComment[M : InternalMonoid](node: Comment)(writer: M): M =
    writer
      .combine("<!--")
      .combine(node.comment)
      .combine("-->")

}
