Feature: Checkout and Payment Flow
  As a customer
  I want to pay for my order
  So that I can complete my purchase and receive my eco-friendly products

  Background:
    Given I am authenticated as a customer

  Scenario: Customer successfully pays for an order with Visa
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
    When the order is ready for payment
    And I pay for the order with a valid Visa card
    Then the payment should succeed
    And the payment method should show "Visa ending in 4242"
    And the order status should be updated to "PAID"

  Scenario: Customer successfully pays for an order with Mastercard
    Given the following products are available:
      | name                  | price  | category |
      | Recycled Denim Jeans  | 89.00  | CLOTHING |
    When I place an order with:
      | product              | quantity |
      | Recycled Denim Jeans | 1        |
    Then the order should be created with status "CREATED"
    When the order is ready for payment
    And I pay for the order with a valid Mastercard
    Then the payment should succeed
    And the payment method should show "Mastercard ending in 5555"
    And the order status should be updated to "PAID"

  Scenario: Payment fails due to declined card
    Given the following products are available:
      | name                   | price | category      |
      | Organic Cotton T-Shirt | 29.99 | CLOTHING      |
    When I place an order with:
      | product                | quantity |
      | Organic Cotton T-Shirt | 1        |
    Then the order should be created with status "CREATED"
    When the order is ready for payment
    And I pay for the order with a declined card
    Then the payment should fail with reason "declined"
    And the order status should be updated to "PAYMENT_PENDING"

  Scenario: Payment fails due to insufficient funds
    Given the following products are available:
      | name                  | price  | category |
      | Recycled Denim Jeans  | 89.00  | CLOTHING |
    When I place an order with:
      | product              | quantity |
      | Recycled Denim Jeans | 1        |
    Then the order should be created with status "CREATED"
    When the order is ready for payment
    And I pay for the order with a card with insufficient funds
    Then the payment should fail with reason "InsufficientFunds"
    And the order status should be updated to "PAYMENT_PENDING"

  Scenario: Payment fails due to fraud detection
    Given the following products are available:
      | name                   | price | category      |
      | Bamboo Toothbrush Set  | 12.50 | PERSONAL_CARE |
    When I place an order with:
      | product               | quantity |
      | Bamboo Toothbrush Set | 3        |
    Then the order should be created with status "CREATED"
    When the order is ready for payment
    And I pay for the order with a card flagged for fraud
    Then the payment should fail with reason "FraudDetected"
    And the order status should be updated to "PAYMENT_PENDING"
