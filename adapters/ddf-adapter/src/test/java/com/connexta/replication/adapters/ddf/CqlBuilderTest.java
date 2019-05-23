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
package com.connexta.replication.adapters.ddf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Date;
import org.junit.Test;

public class CqlBuilderTest {

  @Test
  public void equalTo() {
    assertThat(CqlBuilder.equalTo("att", "val"), is("[ att = 'val' ]"));
    assertThat(CqlBuilder.equalTo("att.sub", "val"), is("[ \"att.sub\" = 'val' ]"));
    assertThat(CqlBuilder.equalTo("att-sub", "val"), is("[ \"att-sub\" = 'val' ]"));
    assertThat(CqlBuilder.equalTo("id", "val"), is("[ \"id\" = 'val' ]"));
  }

  @Test
  public void like() {
    assertThat(CqlBuilder.like("att", "val"), is("[ att like 'val' ]"));
  }

  @Test
  public void negate() {
    assertThat(CqlBuilder.negate(CqlBuilder.equalTo("att", "val")), is("[ NOT [ att = 'val' ] ]"));
  }

  @Test
  public void after() {
    assertThat(CqlBuilder.after("att", new Date(1)), is("[ att after 1970-01-01T00:00:00.001Z ]"));
  }

  @Test
  public void anyOf() {
    String exp1 = CqlBuilder.equalTo("att1", "val1");
    String exp2 = CqlBuilder.equalTo("att2", "val2");
    assertThat(CqlBuilder.anyOf(exp1, exp2), is("[ [ att1 = 'val1' ] OR [ att2 = 'val2' ] ]"));
  }

  @Test
  public void allOf() {
    String exp1 = CqlBuilder.equalTo("att1", "val1");
    String exp2 = CqlBuilder.equalTo("att2", "val2");
    assertThat(CqlBuilder.allOf(exp1, exp2), is("[ [ att1 = 'val1' ] AND [ att2 = 'val2' ] ]"));
  }
}
