Feature: Module Level Order Management
  I want to verify order management within the module boundary, ensuring all business rules and edge cases are handled correctly.

  Background:
    Given the module is running

  Scenario: Create a new order
    Given I am authenticated as "order-user-subject"
    And the following products exist in the catalog:
      | productId | productName          |
      | PROD-001  | Organic Cotton Shirt |
    When I create an order with the following items:
      | productId | productName          | unitPrice | quantity |
      | PROD-001  | Organic Cotton Shirt | 29.99     | 2        |
    Then the order should be created successfully
    And the order status should be "CREATED"
    And the order grand total should be 59.98 EUR

  Scenario: Create order with multiple items
    Given I am authenticated as "multi-item-user"
    And the following products exist in the catalog:
      | productId | productName |
      | PROD-001  | Product One |
      | PROD-002  | Product Two |
    When I create an order with the following items:
      | productId | productName    | unitPrice | quantity |
      | PROD-001  | Product One    | 10.00     | 2        |
      | PROD-002  | Product Two    | 15.50     | 3        |
    Then the order should be created successfully
    And the order should have 2 lines

  Scenario: Retrieve order by ID
    Given I am authenticated as "retrieve-order-user"
    And the following products exist in the catalog:
      | productId | productName  |
      | PROD-TEST | Test Product |
    And an order exists for the current user
    When I retrieve the order by ID
    Then I should receive the order details
    And the order user should match

  Scenario: List orders for user
    Given I am authenticated as "list-orders-user"
    And the following products exist in the catalog:
      | productId | productName  |
      | PROD-TEST | Test Product |
    And an order exists for the current user
    And another order exists for the current user
    When I list my orders
    Then I should receive 2 orders

  Scenario: Cannot access another user's order
    Given I am authenticated as "owner-user"
    And the following products exist in the catalog:
      | productId | productName  |
      | PROD-TEST | Test Product |
    And an order exists for the current user
    When I am authenticated as "other-user"
    And I try to retrieve the order by ID
    Then I should receive an access denied error

  Scenario: Cannot create order with empty lines
    Given I am authenticated as "empty-order-user"
    When I try to create an order with no items
    Then I should receive a validation error

  Scenario: Cannot create order with non-existent product
    Given I am authenticated as "invalid-product-user"
    And no products exist in the catalog
    When I create an order with the following items:
      | productId       | productName     | unitPrice | quantity |
      | PROD-NONEXISTENT | Unknown Product | 10.00     | 1        |
    Then I should receive a product not found error

  Scenario: Order not found
    Given I am authenticated as "not-found-user"
    When I retrieve order with ID "ORD-nonexistent"
    Then I should receive a not found error

  Scenario: Status transition from CREATED to RESERVED
    Given I am authenticated as "status-user"
    And the following products exist in the catalog:
      | productId | productName  |
      | PROD-TEST | Test Product |
    And an order exists for the current user with status "CREATED"
    When the order status is updated to "RESERVED"
    Then the order status should be "RESERVED"

  Scenario: Invalid status transition
    Given I am authenticated as "invalid-status-user"
    And the following products exist in the catalog:
      | productId | productName  |
      | PROD-TEST | Test Product |
    And an order exists for the current user with status "DELIVERED"
    When the order status is updated to "CREATED"
    Then I should receive an invalid status error
