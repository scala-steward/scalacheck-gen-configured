package com.github.andyglow.util

object ScalaVersionSpecificPackage {

  implicit def doubleOrdering: Ordering[Double] = Ordering.Double

  implicit def floatOrdering: Ordering[Float] = Ordering.Float
}
