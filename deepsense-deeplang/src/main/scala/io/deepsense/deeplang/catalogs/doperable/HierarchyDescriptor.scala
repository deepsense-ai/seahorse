/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

/**
 * Describes hierarchy of traits and classes.
 * @param traits descriptors of all registered traits
 * @param classes descriptors of all registered classes
 */
case class HierarchyDescriptor(
    traits: Iterable[TypeDescriptor],
    classes: Iterable[TypeDescriptor])
