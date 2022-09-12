Feature: Order

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
# TODO fix
#    # Check database content
#    Then order is
#      | or_id     | delivery_type | cu_id     | pr_id     | amount   |
#      | 1         | Delivery      | 1111      | 1234      | 200      |
#    # Field value is sourced from:
#    # | generated | template      | looked up | looked up | template |
