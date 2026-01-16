Feature: Module Level Shipping Management
  I want to verify shipping management within the module boundary, ensuring all business rules and edge cases are handled correctly.

  Background:
    Given the module is running

  Scenario: Create a new shipment
    Given a warehouse exists for country "NL"
    When I create a shipment for order "00000000-0000-0000-0000-000000000001" with address:
      | recipientName | John Doe           |
      | street        | Main Street        |
      | houseNumber   | 123                |
      | postalCode    | 1234 AB            |
      | city          | Amsterdam          |
      | countryCode   | NL                 |
    Then the shipment should be created successfully
    And the shipment status should be "CREATED"
    And the shipment should have a tracking number

  Scenario: Retrieve shipment by ID
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000010"
    When I retrieve the shipment by ID
    Then I should receive the shipment details
    And the shipment should have a tracking number

  Scenario: Retrieve shipment by tracking number
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000020"
    When I retrieve the shipment by tracking number
    Then I should receive the shipment details

  Scenario: Retrieve shipment for order
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000030"
    When I retrieve the shipment for order "00000000-0000-0000-0000-000000000030"
    Then I should receive the shipment details

  Scenario: Shipment not found
    When I retrieve shipment with ID "00000000-0000-0000-0000-999999999999"
    Then I should receive a not found error

  Scenario: No shipment for order
    When I retrieve the shipment for order "00000000-0000-0000-0000-888888888888"
    Then I should receive a not found for order error

  Scenario: Cannot create duplicate shipment for order
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000040"
    When I create a shipment for order "00000000-0000-0000-0000-000000000040" with address:
      | recipientName | Jane Doe           |
      | street        | Second Street      |
      | houseNumber   | 456                |
      | postalCode    | 5678 CD            |
      | city          | Rotterdam          |
      | countryCode   | NL                 |
    Then I should receive a duplicate shipment error

  Scenario: Cannot create shipment without available warehouse
    Given no warehouses exist
    When I create a shipment for order "00000000-0000-0000-0000-000000000050" with address:
      | recipientName | Test User          |
      | street        | Test Street        |
      | houseNumber   | 1                  |
      | postalCode    | 12345              |
      | city          | Berlin             |
      | countryCode   | DE                 |
    Then I should receive a no warehouse error

  Scenario: Update shipment status from CREATED to PROCESSING
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000060" with status "CREATED"
    When I update the shipment status to "PROCESSING"
    Then the shipment status should be "PROCESSING"

  Scenario: Update shipment status from PROCESSING to SHIPPED
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000070" with status "PROCESSING"
    When I update the shipment status to "SHIPPED"
    Then the shipment status should be "SHIPPED"

  Scenario: Update shipment status from SHIPPED to IN_TRANSIT
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000080" with status "SHIPPED"
    When I update the shipment status to "IN_TRANSIT"
    Then the shipment status should be "IN_TRANSIT"

  Scenario: Update shipment status from IN_TRANSIT to DELIVERED
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000090" with status "IN_TRANSIT"
    When I update the shipment status to "DELIVERED"
    Then the shipment status should be "DELIVERED"

  Scenario: Invalid status transition
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000100" with status "DELIVERED"
    When I update the shipment status to "CREATED"
    Then I should receive an invalid status transition error

  Scenario: Cancel shipment from CREATED status
    Given a warehouse exists for country "NL"
    And a shipment exists for order "00000000-0000-0000-0000-000000000110" with status "CREATED"
    When I update the shipment status to "CANCELLED"
    Then the shipment status should be "CANCELLED"

  Scenario: Shipment with weight
    Given a warehouse exists for country "NL"
    When I create a shipment for order "00000000-0000-0000-0000-000000000120" with weight 2.5 kg and address:
      | recipientName | Heavy Order User   |
      | street        | Weight Street      |
      | houseNumber   | 100                |
      | postalCode    | 9999 XY            |
      | city          | Utrecht            |
      | countryCode   | NL                 |
    Then the shipment should be created successfully
    And the shipment weight should be 2.5 kg
