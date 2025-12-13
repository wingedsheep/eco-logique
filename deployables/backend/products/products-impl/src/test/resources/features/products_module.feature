Feature: Module Level Product Management
  I want to verify product management within the module boundary, ensuring all business rules and edge cases are handled correctly.

  Background:
    Given the module is running

  Scenario: Create a new eco-product
    When I create a product with the following details:
      | name        | Organic Cotton T-Shirt     |
      | description | Sustainable cotton t-shirt |
      | category    | CLOTHING                   |
      | price       | 29.99 EUR                  |
      | weight      | 150 grams                  |
      | carbon      | 2.1 kg CO2                 |
    Then the product should be created successfully
    And the product should have a sustainability rating

  Scenario: Retrieve product by ID
    Given a product "Bamboo Toothbrush Set" exists
    When I retrieve the product by ID
    Then I should receive the product details
    And the product name should be "Bamboo Toothbrush Set"

  Scenario: Update product price
    Given a product "Solar Charger" exists with price 45.00 EUR
    When I update the product price to 49.99 EUR
    Then the product price should be 49.99 EUR

  Scenario: List products by category
    Given the following products exist:
      | name                    | category   |
      | Organic Cotton T-Shirt  | CLOTHING   |
      | Bamboo Toothbrush       | PERSONAL_CARE |
      | Reusable Water Bottle   | HOUSEHOLD  |
    When I list products in category "CLOTHING"
    Then I should receive 1 product
    And the product should be "Organic Cotton T-Shirt"

  Scenario: Delete a product
    Given a product "Test Product" exists
    When I delete the product
    Then the product should no longer exist

  Scenario: Cannot create product with duplicate name
    Given a product "Unique Product" exists
    When I try to create another product with name "Unique Product"
    Then I should receive a duplicate name error
