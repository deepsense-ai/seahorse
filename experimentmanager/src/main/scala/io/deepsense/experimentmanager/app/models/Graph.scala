/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.models

import io.deepsense.experimentmanager.app.models.{Id => IdModel}

/**
 * A Graph in an experiment. Yet to be implemented.
 */
case class Graph() // TODO Implement and write docs.

object Graph {

  /**
   * A node in a graph.
   * @param id
   */
  case class Node(id: Node.Id)

  object Node {
    type Id = IdModel
  }
}
