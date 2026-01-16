Feature: Complete Order Journey - End to End
  As a business
  I want to verify the complete order flow from product creation to payment
  So that I can ensure all systems work together correctly

  Scenario: Complete order flow from product creation to successful payment with stock verification
    # =========================================
    # STEP 1: Admin creates a new product
    # =========================================
    Given I am authenticated as an admin
    When as admin I create a product with:
      | name        | Eco-Friendly Water Bottle                       |
      | description | Reusable stainless steel bottle with bamboo lid |
      | category    | HOUSEHOLD                                       |
      | price       | 34.99                                           |
      | weight      | 450                                             |
      | carbon      | 2.5                                             |
    Then the product creation should succeed
    And I can retrieve the product by name "Eco-Friendly Water Bottle"

    # =========================================
    # STEP 2: Admin creates a warehouse
    # =========================================
    When I create a warehouse with the following details:
      | name        | Amsterdam Distribution Center |
      | countryCode | NL                            |
      | street      | Prinsengracht                 |
      | houseNumber | 263                           |
      | postalCode  | 1016 GV                       |
      | city        | Amsterdam                     |
    Then the warehouse should be created successfully
    And the warehouse "Amsterdam Distribution Center" should exist

    # =========================================
    # STEP 3: Admin adds stock for the product
    # =========================================
    When I add stock for product "Eco-Friendly Water Bottle" to warehouse "Amsterdam Distribution Center" with quantity 50
    Then the stock update should succeed
    And the stock level for "Eco-Friendly Water Bottle" in "Amsterdam Distribution Center" should be 50

    # =========================================
    # STEP 4: Admin verifies inventory check API
    # =========================================
    When I check the stock availability for "Eco-Friendly Water Bottle"
    Then the total available stock should be 50

    # =========================================
    # STEP 5: Customer adds product to cart
    # =========================================
    Given I am authenticated as a customer
    When I add 2 of "Eco-Friendly Water Bottle" to my cart
    Then my cart should contain 2 of "Eco-Friendly Water Bottle"
    And the cart total should be 69.98 EUR

    # =========================================
    # STEP 6: Customer checks out and pays
    # =========================================
    When I checkout with a valid Visa card
    Then the checkout should succeed
    And an order should be created with status "PAID"
    And the order total should be 69.98 EUR
    And my cart should be empty

    # =========================================
    # STEP 7: Verify stock was reduced after order
    # =========================================
    When I check the stock availability for "Eco-Friendly Water Bottle"
    Then the total available stock should be 48

    # =========================================
    # STEP 8: Customer views order in history
    # =========================================
    When I view my order history
    Then the order history should contain the recent order

    # =========================================
    # STEP 9: Verify order details
    # =========================================
    When I view the order details
    Then the order should contain "Eco-Friendly Water Bottle" with quantity 2
    And the order payment status should be "SUCCEEDED"
