package org.scoalaonline.api.exception;

/**
 * Exception class for when the Lecture's title is invalid.
 */
public class LectureInvalidTitleException extends Exception {

  public LectureInvalidTitleException() {
  }

  public LectureInvalidTitleException(String message) {
    super(message);
  }
}
