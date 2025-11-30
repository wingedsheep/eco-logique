Feature: Module Level Product Management
  I want to verify product management within the module boundary

  Scenario: Create and retrieve a product
    Given the module is running
    When I create a product "Module Product" with price 10.00 EUR
    Then the product should be retrievable by ID
    And the product name should be "Module Product"
