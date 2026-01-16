Feature: Complete Order Journey - End to End
  As a business
  I want to verify the complete order flow from product creation to shipment
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
    # Using France (FR) as it doesn't have a worldview warehouse
    When I create a warehouse with the following details:
      | name        | Paris Distribution Center |
      | countryCode | FR                        |
      | street      | Avenue des Champs-Élysées |
      | houseNumber | 100                       |
      | postalCode  | 75008                     |
      | city        | Paris                     |
    Then the warehouse should be created successfully
    And the warehouse "Paris Distribution Center" should exist

    # =========================================
    # STEP 3: Admin adds stock for the product
    # =========================================
    When I add stock for product "Eco-Friendly Water Bottle" to warehouse "Paris Distribution Center" with quantity 50
    Then the stock update should succeed
    And the stock level for "Eco-Friendly Water Bottle" in "Paris Distribution Center" should be 50

    # =========================================
    # STEP 4: Admin verifies inventory check API
    # =========================================
    When I check the stock availability for "Eco-Friendly Water Bottle"
    Then the total available stock should be 50

    # =========================================
    # STEP 5: Customer sets up profile with shipping address
    # =========================================
    Given I am authenticated as a customer
    When I set up my profile with a shipping address in "France"

    # =========================================
    # STEP 6: Customer adds product to cart
    # =========================================
    When I add 2 of "Eco-Friendly Water Bottle" to my cart
    Then my cart should contain 2 of "Eco-Friendly Water Bottle"
    And the cart total should be 69.98 EUR

    # =========================================
    # STEP 7: Customer checks out and pays
    # =========================================
    # After checkout, order is PAID and shipment is CREATED
    # Order remains in PAID status until warehouse staff ships it
    When I checkout with a valid Visa card
    Then the checkout should succeed
    And an order should be created with status "PAID"
    And the order total should be 69.98 EUR
    And my cart should be empty

    # =========================================
    # STEP 8: Verify stock was reduced after order
    # =========================================
    When I check the stock availability for "Eco-Friendly Water Bottle"
    Then the total available stock should be 48

    # =========================================
    # STEP 9: Customer views order in history
    # =========================================
    When I view my order history
    Then the order history should contain the recent order

    # =========================================
    # STEP 10: Verify order details
    # =========================================
    When I view the order details
    Then the order should contain "Eco-Friendly Water Bottle" with quantity 2
    And the order payment status should be "SUCCEEDED"

    # =========================================
    # STEP 11: Verify shipment was created (awaiting warehouse processing)
    # =========================================
    Then a shipment should be created for the order
    And the shipment should have a tracking number starting with "ECO-"
    And the shipment status should be "CREATED"
    And the shipment should be assigned to warehouse "Paris Distribution Center"

    # =========================================
    # STEP 12: Warehouse staff processes and ships the order
    # =========================================
    Given I am authenticated as an admin
    When the warehouse marks the shipment as processing
    And the warehouse marks the shipment as shipped
    Then the shipment status should be "SHIPPED"

    # =========================================
    # STEP 13: Customer verifies order status updated to SHIPPED
    # =========================================
    Given I am authenticated as a customer
    Then the order status should now be "SHIPPED"

    # =========================================
    # STEP 14: Driver marks shipment as in transit via tablet
    # =========================================
    When the driver marks the shipment as "IN_TRANSIT"
    Then the shipment status should be "IN_TRANSIT"

    # =========================================
    # STEP 15: Driver marks shipment as delivered via tablet
    # =========================================
    When the driver marks the shipment as "DELIVERED"
    Then the shipment status should be "DELIVERED"
    And the order status should now be "DELIVERED"
