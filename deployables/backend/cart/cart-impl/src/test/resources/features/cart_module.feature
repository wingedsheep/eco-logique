Feature: Module Level Cart Management
  I want to verify cart management within the module boundary, ensuring all business rules and edge cases are handled correctly.

  Background:
    Given the cart module is running

  Scenario: View empty cart
    Given I am authenticated as cart user "empty-cart-user"
    And my cart is empty
    When I view my cart
    Then my cart should be empty

  Scenario: Add item to cart
    Given I am authenticated as cart user "add-item-user"
    And the following products are available for cart:
      | productId                            | productName          | price |
      | 00000000-0000-0000-0000-000000000001 | Organic Cotton Shirt | 29.99 |
    And my cart is empty
    When I add 2 of product "00000000-0000-0000-0000-000000000001" to my cart
    Then the item should be added successfully
    And my cart should have 1 item(s)
    And the cart subtotal should be 59.98
    And the cart total items should be 2

  Scenario: Add multiple items to cart
    Given I am authenticated as cart user "multi-item-user"
    And the following products are available for cart:
      | productId                            | productName     | price |
      | 00000000-0000-0000-0000-000000000001 | Product One     | 10.00 |
      | 00000000-0000-0000-0000-000000000002 | Product Two     | 15.50 |
    And my cart is empty
    When I add 2 of product "00000000-0000-0000-0000-000000000001" to my cart
    And I add 3 of product "00000000-0000-0000-0000-000000000002" to my cart
    Then my cart should have 2 item(s)
    And the cart subtotal should be 66.50
    And the cart total items should be 5

  Scenario: Add same product increases quantity
    Given I am authenticated as cart user "merge-item-user"
    And the following products are available for cart:
      | productId                            | productName | price |
      | 00000000-0000-0000-0000-000000000001 | Product One | 10.00 |
    And my cart is empty
    When I add 2 of product "00000000-0000-0000-0000-000000000001" to my cart
    And I add 3 of product "00000000-0000-0000-0000-000000000001" to my cart
    Then my cart should have 1 item(s)
    And the item "00000000-0000-0000-0000-000000000001" should have quantity 5
    And the cart subtotal should be 50.00

  Scenario: Update item quantity
    Given I am authenticated as cart user "update-qty-user"
    And the following products are available for cart:
      | productId                            | productName | price |
      | 00000000-0000-0000-0000-000000000001 | Product One | 10.00 |
    And my cart has the following items:
      | productId                            | productName | price | quantity |
      | 00000000-0000-0000-0000-000000000001 | Product One | 10.00 | 2        |
    When I update the quantity of "00000000-0000-0000-0000-000000000001" to 5
    Then the cart should be updated successfully
    And the item "00000000-0000-0000-0000-000000000001" should have quantity 5
    And the cart subtotal should be 50.00

  Scenario: Remove item from cart
    Given I am authenticated as cart user "remove-item-user"
    And the following products are available for cart:
      | productId                            | productName | price |
      | 00000000-0000-0000-0000-000000000001 | Product One | 10.00 |
      | 00000000-0000-0000-0000-000000000002 | Product Two | 20.00 |
    And my cart has the following items:
      | productId                            | productName | price | quantity |
      | 00000000-0000-0000-0000-000000000001 | Product One | 10.00 | 1        |
      | 00000000-0000-0000-0000-000000000002 | Product Two | 20.00 | 1        |
    When I remove "00000000-0000-0000-0000-000000000001" from my cart
    Then the cart should be updated successfully
    And my cart should have 1 item(s)
    And the cart subtotal should be 20.00

  Scenario: Clear cart
    Given I am authenticated as cart user "clear-cart-user"
    And the following products are available for cart:
      | productId                            | productName | price |
      | 00000000-0000-0000-0000-000000000001 | Product One | 10.00 |
    And my cart has the following items:
      | productId                            | productName | price | quantity |
      | 00000000-0000-0000-0000-000000000001 | Product One | 10.00 | 2        |
    When I clear my cart
    Then my cart should be empty

  Scenario: Cannot add non-existent product
    Given I am authenticated as cart user "invalid-product-user"
    And no products are available
    When I add 1 of product "00000000-0000-0000-0000-000000000999" to my cart
    Then I should receive a product not found error for cart

  Scenario: Cannot update non-existent item
    Given I am authenticated as cart user "update-nonexistent-user"
    And my cart is empty
    When I update the quantity of "00000000-0000-0000-0000-000000000999" to 5
    Then I should receive an item not found error

  Scenario: Cannot remove non-existent item
    Given I am authenticated as cart user "remove-nonexistent-user"
    And my cart is empty
    When I remove "00000000-0000-0000-0000-000000000999" from my cart
    Then I should receive an item not found error
