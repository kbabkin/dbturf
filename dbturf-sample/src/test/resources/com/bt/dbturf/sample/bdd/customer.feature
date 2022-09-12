Feature: Customer

  Scenario: Import Customer
    # Take template TripleOne from json and save to database
    When customer
      | name      | country |
      | TripleOne | ECO     |
    # Check database content
    Then customer is
      | name      | cu_id | country | updated_by |
      | TripleOne | 111   | ECO     | DbTurf     |
    # Field value is sourced from:
    # | json      | json  | scenario | defaults   |
