package org.codice.ditto.replication.admin.query.queries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ditto.replication.api.QueryService;
import org.codice.ditto.replication.api.data.Query;

/**
 * This is a graphql function field. A call to the graphql endpoint with the given field name will
 * trigger the performFunction method and present the returned data in the response.
 */
public class GetQueries extends BaseFunctionField<ListField<QueryField>> {

  private static final String FIELD_NAME = "queries";

  private static final String DESCRIPTION =
      "Retrieves all saved queries for the currently logged in user.";

  private static final ListField<QueryField> RETURN_TYPE = new QueryField.ListImpl();

  private final QueryService queryService;

  /**
   * Creates a new GetQueries function field.
   *
   * @param queryService A service which will fetch queries from the catalog framework for us
   */
  public GetQueries(QueryService queryService) {
    super(FIELD_NAME, DESCRIPTION);
    this.queryService = queryService;
  }

  /**
   * Contains all the functionality of the "queries" graphql query. The response of the query will
   * contain the information in the {@link QueryField.ListImpl}.
   *
   * @return the result of a query for saved user queries in the form of a {@link
   *     QueryField.ListImpl}
   * @throws org.codice.ditto.replication.api.QueryException if an exception is thrown when querying
   *     the catalog.
   */
  @Override
  public ListField<QueryField> performFunction() {
    ListField<QueryField> queryFields = new QueryField.ListImpl();
    queryService.queries().map(this::getFieldForQuery).forEach(queryFields::add);
    return queryFields;
  }

  private QueryField getFieldForQuery(Query query) {
    QueryField queryField = new QueryField();
    queryField.title(query.getTitle());
    queryField.cql(query.getCql());
    return queryField;
  }

  /**
   * Returns the error codes this function field can return
   *
   * @return the error codes this function field can return
   */
  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of();
  }

  /**
   * Returns the arguments to the {@link BaseFunctionField#performFunction()} method of this
   * function field
   *
   * @return arguments to {@link BaseFunctionField#performFunction()} for this function field
   */
  @Override
  public List<Field> getArguments() {
    return ImmutableList.of();
  }

  /**
   * Returns the return type of this function field
   *
   * @return the return type of this function field
   */
  @Override
  public ListField<QueryField> getReturnType() {
    return RETURN_TYPE;
  }

  /**
   * Returns a new instance of this function field
   *
   * @return a new instance of this function field
   */
  @Override
  public FunctionField<ListField<QueryField>> newInstance() {
    return new GetQueries(queryService);
  }
}
