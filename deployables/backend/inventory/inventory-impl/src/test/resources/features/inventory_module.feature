Feature: Module Level Inventory Management
  I want to verify inventory management within the module boundary, ensuring all warehouse and stock operations work correctly.

  Background:
    Given the module is running

  Scenario: Create a new warehouse without address
    When I create a warehouse with the following details:
      | name        | Berlin Distribution Hub |
      | countryCode | DE                      |
    Then the warehouse should be created successfully
    And the warehouse name should be "Berlin Distribution Hub"
    And the warehouse country code should be "DE"

  Scenario: Create a warehouse with full address
    When I create a warehouse with the following details:
      | name        | Amsterdam Central Warehouse |
      | countryCode | NL                          |
      | street      | Prinsengracht               |
      | houseNumber | 263                         |
      | postalCode  | 1016 GV                     |
      | city        | Amsterdam                   |
    Then the warehouse should be created successfully
    And the warehouse should have an address

  Scenario: Retrieve warehouse by ID
    Given a warehouse "Paris Storage" exists in country "FR"
    When I retrieve the warehouse by ID
    Then I should receive the warehouse details
    And the warehouse name should be "Paris Storage"

  Scenario: Update warehouse name
    Given a warehouse "Old Name Warehouse" exists in country "BE"
    When I update the warehouse name to "New Name Warehouse"
    Then the warehouse update should succeed
    And the warehouse name should be "New Name Warehouse"

  Scenario: Delete a warehouse
    Given a warehouse "Temporary Warehouse" exists in country "NL"
    When I delete the warehouse
    Then the warehouse should be deleted successfully
    And the warehouse should no longer exist

  Scenario: List all warehouses
    Given a warehouse "List Test Warehouse" exists in country "DE"
    When I list all warehouses
    Then I should receive a list of warehouses
    And the list should contain at least 1 warehouse

  Scenario: Cannot create warehouse with duplicate name
    Given a warehouse "Unique Warehouse" exists in country "FR"
    When I try to create another warehouse with name "Unique Warehouse"
    Then I should receive a duplicate name error

  Scenario: Cannot create warehouse with invalid country code
    When I try to create a warehouse with invalid country code "XX"
    Then I should receive an invalid country code error

  Scenario: Add stock to warehouse
    Given a warehouse "Stock Test Warehouse" exists in country "NL"
    When I add stock for a product with quantity 100
    Then the stock update should succeed
    And the stock level should show quantity 100

  Scenario: Update stock quantity
    Given a warehouse "Stock Update Warehouse" exists in country "DE"
    When I add stock for a product with quantity 50
    Then the stock update should succeed
    When I update stock quantity to 75
    Then the stock update should succeed
    And the stock level should show quantity 75

  Scenario: Get warehouse stock levels
    Given a warehouse "Stock List Warehouse" exists in country "BE"
    When I add stock for a product with quantity 25
    Then the stock update should succeed
    When I get the warehouse stock
    Then I should receive a list of stock levels

  Scenario: Check stock availability via public API
    Given a warehouse "Public Stock Warehouse" exists in country "FR"
    When I add stock for a product with quantity 200
    Then the stock update should succeed
    When I check stock for the test product
    Then the total available stock should be 200
