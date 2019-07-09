package org.codice.ditto.replication.admin.query.queries;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.Callable;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.common.fields.base.BaseListField;
import org.codice.ddf.admin.common.fields.base.BaseObjectField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;

/** A data structure representing a saved query, created for use with the graphql endpoint. */
public class QueryField extends BaseObjectField {
  private static final String DEFAULT_FIELD_NAME = "query";

  private static final String FIELD_TYPE_NAME = "Query";

  private static final String DESCRIPTION =
      "A query which contains a filter that can be extracted and used in a replication.";

  private StringField title;

  private StringField cql;

  /**
   * Creates a new QueryField used to make query information available through the graphql endpoint.
   */
  public QueryField() {
    super(DEFAULT_FIELD_NAME, FIELD_TYPE_NAME, DESCRIPTION);
    this.title = new StringField("title");
    this.cql = new StringField("cql");
  }

  /**
   * Sets the title of this QueryField to the given title and returns the entire field.
   *
   * @param title The title to give to this QueryField
   * @return this QueryField
   */
  public QueryField title(String title) {
    this.title.setValue(title);
    return this;
  }

  /**
   * Sets the cql of this QueryField to the given cql and returns the entire field.
   *
   * @param cql The cql to set for this QueryField
   * @return this QueryField
   */
  public QueryField cql(String cql) {
    this.cql.setValue(cql);
    return this;
  }

  /**
   * Returns the title of the query as a {@link String}
   *
   * @return the title of the query as a {@link String}
   */
  public String title() {
    return title.getValue();
  }

  /**
   * Returns the cql of the query as a {@link String}
   *
   * @return the cql of the query as a {@link String}
   */
  public String cql() {
    return cql.getValue();
  }

  /**
   * Returns the title of the query as a {@link StringField}
   *
   * @return the title of the query as a {@link StringField}
   */
  public StringField titleField() {
    return title;
  }

  /**
   * Returns the cql of the query as a {@link StringField}
   *
   * @return the cql of the query as a {@link StringField}
   */
  public StringField cqlField() {
    return cql;
  }

  /**
   * Returns a list of what other fields this field contains so that the graphql endpoint can
   * display them.
   *
   * @return A list of the fields contained in this field.
   */
  @Override
  public List<Field> getFields() {
    return ImmutableList.of(title, cql);
  }

  /**
   * A list implementation for this Field. We use this class to return a list of fields from the
   * graphql endpoint.
   */
  public static class ListImpl extends BaseListField<QueryField> {

    public static final String DEFAULT_FIELD_NAME = "replications";

    /** Creates a list implementation for {@link QueryField}s */
    public ListImpl() {
      super(DEFAULT_FIELD_NAME);
    }

    /**
     * returns a function to create a new {@link QueryField}
     *
     * @return a function to create a new {@link QueryField}
     */
    @Override
    public Callable<QueryField> getCreateListEntryCallable() {
      return QueryField::new;
    }
  }
}
