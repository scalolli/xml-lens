package net.michalsitko.xml.printing

import net.michalsitko.xml.entities.ResolvedName

private [printing] trait Resolver {
  def resolve(name: ResolvedName): String =
    if (name.hasPrefix) {
      name.prefix + ":" + name.localName
    } else {
      name.localName
    }
}
