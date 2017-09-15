/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor.util

/**
 * Binary semaphore maintains only one permit.
 * Releasing already released binary semaphore has no effect.
 * There is no requirement that a thread that releases a permit must have acquired that permit.
 * @param initial if initial is 0, then semaphore is initially locked and unlocked otherwise
 */
class BinarySemaphore(initial: Int) {
  private var locked: Boolean = initial == 0

  /**
   * Acquires a permit from this semaphore, blocking until permit is available,
   * or the thread is interrupted.
   */
  def acquire(): Unit = {
    this.synchronized {
      while (locked) {
        wait()
      }
      locked = true
    }
  }

  /**
   * Acquires a permit from this semaphore, blocking until permit is available,
   * the thread is interrupted or timeout has expired.
   * @param timeout the maximum time to wait for a permit
   * @return true if permit has been acquired and false otherwise
   */
  def tryAcquire(timeout: Long): Boolean = {
    this.synchronized {
      if (locked) {
        wait(timeout)
      }
      val acquired = !locked
      locked = true
      acquired
    }
  }

  /**
   * Releases a permit, returning it to the semaphore.
   * If any threads are trying to acquire a permit, then one is selected and given the permit that
   * was just released.
   */
  def release(): Unit = {
    this.synchronized {
      if (locked) {
        notify()
      }
      locked = false
    }
  }
}
