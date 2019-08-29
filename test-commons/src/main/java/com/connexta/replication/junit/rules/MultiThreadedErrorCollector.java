/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.replication.junit.rules;

import static org.junit.Assert.assertThat;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.hamcrest.Matcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * This class provides a JUnit rule implementation capable of recording errors while executing and
 * reporting them when the test complete successfully or not. This is an enhancement over the {@link
 * org.junit.rules.ErrorCollector} class which doesn't properly support multi-threaded test cases
 * and do not report accumulated failures when the test fails for whatever reasons.
 */
public class MultiThreadedErrorCollector implements TestRule {
  private final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
          verify();
        } catch (AssertionError e) {
          addError(e);
          verify();
        } catch (Throwable t) {
          verify();
          throw t;
        }
      }
    };
  }

  /**
   * Verifies that no error were reported.
   *
   * @throws Throwable A throwable of all the errors contained in the collector
   */
  protected void verify() throws Throwable {
    MultipleFailureException.assertEmpty(errors.stream().collect(Collectors.toList()));
  }

  /**
   * Adds a Throwable to the table. Execution continues, but the test will fail at the end.
   *
   * @param error the error to be recorded
   */
  public void addError(Throwable error) {
    errors.add(error);
  }

  /**
   * Adds a failure to the table if {@code matcher} does not match {@code value}. Execution
   * continues, but the test will fail at the end if the match fails.
   *
   * @param <T> the type of the value
   * @param value the value to be asserted
   * @param matcher the matcher to use for validating the value
   */
  public <T> void checkThat(T value, Matcher<T> matcher) {
    checkThat("", value, matcher);
  }

  /**
   * Adds a failure with the given {@code reason} to the table if {@code matcher} does not match
   * {@code value}. Execution continues, but the test will fail at the end if the match fails.
   *
   * @param <T> the type of the value
   * @param reason the reason to associate with the validation message
   * @param value the value to be asserted
   * @param matcher the matcher to use for validating the value
   */
  public <T> void checkThat(String reason, T value, Matcher<T> matcher) {
    checkSucceeds(
        () -> {
          assertThat(reason, value, matcher);
          return value;
        });
  }

  /**
   * Adds to the table the exception, if any, thrown from {@code callable}. Execution continues, but
   * the test will fail at the end if {@code callable} threw an exception.
   *
   * @param <T> the type of the return value
   * @param callable a callable object to call and record any error from
   * @return the value from the callable
   */
  public <T> T checkSucceeds(Callable<T> callable) {
    try {
      return callable.call();
    } catch (Throwable e) {
      addError(e);
      return null;
    }
  }
}
