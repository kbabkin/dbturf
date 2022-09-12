Feature: Product

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

  Scenario: product xref collection
    Given product
      | name |
      | IBM  |
    Then product is
      | name | pr_id | description |
      | IBM  | 1234  | IBM Corp    |
    And product xrefs are
      | product | pr_id | xref_type | xref_value | updated_by |
      | IBM     | 1234  | RIC       | IBM        | DbTurf     |
    Given product xrefs
      | product | xref_type | xref_value |
      | IBM     | ISIN      | HN12345678 |
    Then product xrefs are
      | product | xref_type | xref_value |
      | IBM     | RIC       | IBM        |
      | IBM     | ISIN      | HN12345678 |
    Given product
      | name |
      | IBM  |
    And product xrefs are
      | product | xref_type | xref_value |
      | IBM     | RIC       | IBM        |
