# dbTurf

**dbTurf** is an open-source Java library for testing database applications.
The library is designed for simplification of database setup and validation in test code.

Tests scenarios involving database usually are interested in few specific table rows or field values.
But to start action actually being tested application needs much more 
"Business as Usual" data prepared in database.
**dbTurf** tries to split test data in layers and to handle them properly. 

And term **dbTurf** pretends to be a plentiful ground for a yield of your database-related tests :smile:

## Features

1. Define only interesting data in test scenario, take the rest from preconfigured template(s).
   Use template/default values for fields mandatory in database but not affecting test.
   Technical details like insert/update syntax is localized in special reusable code.

    <details>
      <summary><span style="font-weight: bold;">Details</span></summary>

    Item data is combined from:
    - Scenario provided template name and field override values
    - Template data picked up by name
    - Default values filled (if configured)
    - Context variables substituted, e.g. ${Timestamp}
    - Foreign key technical values are looked up by natural keys (described in Features#2)
    
    Reusable parts are:
    - Template data in JSON format
    - Domain metadata code with primary keys, foreign keys, etc. 

   #### Example
   ###### Template data - customer.json
   ```json
   {
       "#defaults": {
           "updated_by": "DbTurf",
           "updated_at": "${Timestamp}"
       },
       "TripleOne": {
           "cu_id": 111,
           "name": "TripleOne",
           "country": "HND"
       },
       "QuadrupleOne": {  ...
   ```

   ###### BDD Scenario
   ```gherkin
   Scenario: Import Customer
     # Take template TripleOne from json and save to database
     When customer
       | name      | country |
       | TripleOne | ECO     |
     # Check database content
     Then customer is
       | name      | cu_id    | country  | updated_by | updated_at              |
       | TripleOne | 111      | ECO      | DbTurf     | 2022-09-09 14:14:21.713 |
     # Field value is sourced from:
     # | template  | template | scenario | defaults   | substituted             |
    ```

   #### Alternatives
    - Import prod/user dump, find matching data combination.
      Dump size can be large, but without required combination.
    - Use dump, modify some data. 
      Data not provided explicitly can be modified by other test.
    - [DbUnit](https://www.dbunit.org/) works good with one data set for all tests,
      but does not have native support for test-specific data customization.

    </details>


2. Use natural identifiers in test scenario, without adjusting technical ones manually.

    <details>
      <summary><span style="font-weight: bold;">Details</span></summary>

    In scenario use natural, human-readable keys. 
    Having domain metadata configured as reusable code, framework replaces
    fields containing natural keys with fields and values containing technical keys. 
    - Item referencing other table gets natural key replaced by technical key 
      looked up in database (see example below)
    - Template data with child items gets the relation's technical keys 
      filled automatically (see example in Feature#4)

   #### Example
   ###### Template data - order.json
   ```json
   {
     "Delivery": {
       "customer": "TripleOne",
       "product": "IBM",
       "delivery_type": "Delivery",
       "amount": 100
     }, ...
   ```

      ###### BDD Scenario
   ```gherkin
   Scenario: Import Order
     # Ensure referenced Customer and Product exists
     Given customer
       | name         |
       | QuadrupleOne |
     And product
       | name |
       | IBM  |
     # Take template Delivery from json and save to database
     When order
       | template | customer     | amount |
       | Delivery | QuadrupleOne | 200    |
     # Check database content
     Then order is
       | or_id     | delivery_type | cu_id     | pr_id     | amount   |
       | 1         | Delivery      | 1111      | 1234      | 200      |
     # Field value is sourced from:
     # | generated | template      | looked up | looked up | scenario |
   ```

   #### Alternatives
    - Inspired
      by [SAP Commerce ImpEx](https://help.sap.com/docs/SAP_COMMERCE/50c996852b32456c96d3161a95544cdb/028caa3ac0df45a89854976a44b3f78b.html?version=1808&locale=en-US)
    - Maintain technical Primary Keys and Foreign Keys in SQL or XML files manually.
      Sometimes it is possible use subqueries (select id from parent_table where name='abc').
    </details>


3. Every Item is represented as collection of **field=value** pairs (Java **Map** interface) in code.
   This allows to unify *transform* (*combine*, *retrieve*, etc) and *compare* operations over items from different sources
   (scenario, template json, database, cache) and going in different directions (read, write).

    <details>
      <summary><span style="font-weight: bold;">Details</span></summary>

    Operation over non-fixed Items:
    - Read - from scenario, template, database, cache, etc.
    - Write - to database, cache, etc.
    - Combine - compose data read from different sources before write or compare.
    - Retrieve - natural, technical keys to lookup in database.
    - Transform, normalize, denormalize - scenario does not have to have data
      exactly in same format as in database. 
    - Compare (diff) - expected and actual value, with all possible preceding transformations.
    - Non-key fields are not hardcoded in reusable code, 
      and used by name provided in scenario/template.

   #### Example
    ```java
    // Read template from JSON
    Map<String, Object> item = JsonItem.reader("customer").getRoot("TripleOne");
    // Customize for scenario
    item.put("country", "ECO");
    // Merge (insert/update as required) in database
    DbUpdater dbUpdater = new DbUpdater(new TableId("test", "Customer", setOf("cu_id")));
    dbUpdater.merge(item);
    // Load from database and compare
    List<Map<String, Object>> loaded = dbUpdater.findByEq(mapOf("cu_id", 111));
    DiffItem.diff(listOf(mapOf("name", "TripleOne", "country", "ECO")), loaded)
            .assertEquals("loaded as saved");
    ```

   #### Alternatives
    - Inspired by tests in Groovy.
    - Strict data model definition, like JPA.
      In some cases it cannot be used, e.g. to store natural keys.
    - [DbUnit](https://www.dbunit.org/) works with DataSet, looks optimized for performance.
    - [JDBDT (Java DataBase Delta Testing)](http://jdbdt.org/)

   </details>


4. Explicitly define all required preconditions.
   Test-specific data is taken from scenario, more generic from template.
   Conflicting data is removed, if this is part of preconditions.
   Data not defined in preconditions should not affect test, so no need to start every test from clean database.

    <details>
      <summary><span style="font-weight: bold;">Details</span></summary>

    - Scenario defines target database state.
      Framework performs insert/update/delete as required.
    - Different handling for different table types:
        - Static data - override, e.g. Customer and Product in sample project.
        - Dynamic data - generate new rows, e.g. Order in sample project.
        - Conflicts - remove from database. Performed for child collections (see example below),
          for non-primary key unique constraints.

   #### Example
   ###### Template data - product.json
   ```json
   {
     "IBM": {
       "pr_id": 1234,
       "name": "IBM",
       "description": "IBM Corp",
       "xrefs": [
         {
           "xref_type": "RIC",
           "xref_value": "IBM"
         }
       ]
     }, ...
   }
   ```

   ###### BDD Scenario
   ```gherkin
   Scenario: Import Product
     # Take template IBM with child xrefs from json and save to database
     Given product
       | name |
       | IBM  |
     # Check database content
     Then product is
       | name | pr_id | description |
       | IBM  | 1234  | IBM Corp    |
     # If there had been other xrefs in database, they were removed
     And product xrefs are
     # Field value usage:
     # | lookup by | looked up | loaded and compared                           |
       | product   | pr_id     | xref_type           | xref_value | updated_by |
       | IBM       | 1234      | RIC                 | IBM        | DbTurf     |
   ```

   #### Alternatives
    - Clean all data, import basic dump, insert test-specific data. Drawbacks:
        - Time-consuming.
        - Test verifies only that application works on empty database.
    - Flaky tests if not all preconditions are defined explicitly.
      In this case sequence of tests execution can affect the result.
    - [DbSetup](http://dbsetup.ninja-squad.com/user-guide.html)
   </details>

## Notes

- Sample project ``dbturf-sample``:
    - ``src/main`` - shared test code, templates.
      If in you project there is one database module and multiple code modules,
      this one is related to database module.
    - ``src/test`` - jdbc configuration example, tests for framework itself.
      Add "When" clause to sample scenarios to perform actual application action.

- Expected usage is to add scenarios with required templates from scratch, 
  requiring database to contain only technical reference data at start.

- Performance notes. Comparing to clean/insert-all before every test:
  - Insert of same amount of data (first run) is expected to be slower.
  - Following tests are expected to run faster since significantly less amount of db changes required.
