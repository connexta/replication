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
package com.connexta.replication.adapters.ddf.csw;

import com.connexta.replication.api.data.Metadata;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Converts CSW Record to a Metadata.
 */
public class CswRecordConverter implements Converter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CswRecordConverter.class);
  private static final HierarchicalStreamCopier COPIER = new HierarchicalStreamCopier();

  private static final List<String> COMPLEX_TYPE = Arrays.asList("geometry", "stringxml");

  @Override
  public boolean canConvert(Class clazz) {
    return Metadata.class.isAssignableFrom(clazz);
  }

  @Override
  public void marshal(
      Object o,
      HierarchicalStreamWriter hierarchicalStreamWriter,
      MarshallingContext marshallingContext) {
    throw new UnsupportedOperationException("Marshaling not supported by CswRecordConverter");
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

    Map<String, String> namespaceMap = null;
    Object namespaceObj = context.get(Constants.NAMESPACE_DECLARATIONS);

    if (namespaceObj instanceof Map<?, ?>) {
      namespaceMap = (Map<String, String>) namespaceObj;
    }

    return MetacardMarshaller.unmarshal(reader, namespaceMap);
  }
}
