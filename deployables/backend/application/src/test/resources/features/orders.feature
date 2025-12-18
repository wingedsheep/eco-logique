Feature: Place Order Journey
  As a customer
  I want to place an order for eco-friendly products
  So that I can receive sustainable goods

  Background:
    Given I am authenticated as a customer

  Scenario: Customer places an order and views order history
    Given the following products are available:
      | name                   | price | category      |
      | Organic Cotton T-Shirt | 29.99 | CLOTHING      |
      | Bamboo Toothbrush Set  | 12.50 | PERSONAL_CARE |
    When I place an order with:
      | product                | quantity |
      | Organic Cotton T-Shirt | 2        |
      | Bamboo Toothbrush Set  | 1        |
    Then the order should be created with status "CREATED"
    And the order total should be 72.48 EUR
    When I view my order history
    Then I should see 1 order in my history
