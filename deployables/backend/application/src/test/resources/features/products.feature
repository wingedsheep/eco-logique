Feature: Manage Products Integration
  As an administrator
  I want to ensure the product module is correctly wired into the application
  So that I can perform basic product operations

  Background:
    Given the products database is empty

  Scenario: Create and Retrieve Product Integration
    When I create a product with the following details:
      | name        | Integration Test Product   |
      | description | Verified wiring            |
      | category    | ELECTRONICS                |
      | price       | 99.99 EUR                  |
      | weight      | 500 grams                  |
      | carbon      | 5.0 kg CO2                 |
    Then the product should be created successfully
    When I retrieve the product by ID
    Then I should receive the product details
    And the product name should be "Integration Test Product"
