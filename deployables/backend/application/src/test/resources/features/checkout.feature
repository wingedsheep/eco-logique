Feature: Complete Checkout Flow
  As a customer
  I want to checkout my shopping cart
  So that I can purchase eco-friendly products with a single action

  Background:
    Given I am authenticated as a customer
    And the following products are available:
      | name                   | price | category      |
      | Organic Cotton T-Shirt | 29.99 | CLOTHING      |
      | Bamboo Toothbrush Set  | 12.50 | PERSONAL_CARE |

  Scenario: Successful checkout with items in cart
    Given I have the following items in my cart:
      | product                | quantity |
      | Organic Cotton T-Shirt | 2        |
      | Bamboo Toothbrush Set  | 1        |
    When I checkout with a valid Visa card
    Then the checkout should succeed
    And an order should be created with status "PAID"
    And the order total should be 72.48 EUR
    And my cart should be empty

  Scenario: Checkout fails with empty cart
    Given my cart is empty
    When I attempt to checkout with a valid Visa card
    Then the checkout should fail with "Empty Cart"

  Scenario: Checkout fails due to insufficient stock
    Given I have the following items in my cart:
      | product                | quantity |
      | Organic Cotton T-Shirt | 200      |
    And the stock level for "Organic Cotton T-Shirt" is 10
    When I attempt to checkout with a valid Visa card
    Then the checkout should fail with "Insufficient Stock"

  Scenario: Checkout fails due to declined card
    Given I have the following items in my cart:
      | product                | quantity |
      | Organic Cotton T-Shirt | 1        |
    When I checkout with a declined card
    Then the checkout should fail with "Payment Failed"
    And an order should exist with status "PAYMENT_PENDING"
