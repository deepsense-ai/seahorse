/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import scala.concurrent.Future

import io.deepsense.commons.models.Id

trait NotebookStorage {

  /**
   * Returns a notebook with the specified id.
   *
   * @param id Id of the notebook.
   * @return Notebook with the id as an object or None if the notebook does not exist.
   */
  def get(id: Id): Future[Option[String]]

  /**
   * Saves a notebook.
   *
   * @param id Id of the notebook.
   * @param notebook Notebook to be saved.
   */
  def save(id: Id, notebook: String): Future[Unit]
}
