Feature: Module Level Payment Processing
  I want to verify payment processing within the module boundary, ensuring all payment scenarios are handled correctly.

  Background:
    Given the module is running

  Scenario: Process a successful Visa payment
    When I process a payment with a valid Visa card
    Then the payment should succeed
    And the payment status should be "SUCCEEDED"
    And the payment method summary should contain "Visa"

  Scenario: Process a successful Mastercard payment
    When I process a payment with a valid Mastercard
    Then the payment should succeed
    And the payment status should be "SUCCEEDED"
    And the payment method summary should contain "Mastercard"

  Scenario: Retrieve payment by ID
    Given a successful payment exists
    When I retrieve the payment by ID
    Then I should receive the payment details
    And the payment status should be "SUCCEEDED"

  Scenario: Retrieve non-existent payment
    When I retrieve a non-existent payment
    Then I should receive a not found error

  Scenario: Payment with declined card
    When I process a payment with a declined card
    Then the payment should fail with status code 402
    And the error title should be "Card Declined"

  Scenario: Payment with insufficient funds
    When I process a payment with insufficient funds
    Then the payment should fail with status code 402
    And the error title should be "Insufficient Funds"

  Scenario: Payment triggers fraud detection
    When I process a payment that triggers fraud detection
    Then the payment should fail with status code 403
    And the error title should be "Payment Rejected"

  Scenario: Payment with processing error
    When I process a payment that causes a processing error
    Then the payment should fail with status code 500
    And the error title should be "Processing Error"

  Scenario: Payment with expired card
    When I process a payment with an expired card
    Then the payment should fail with status code 400
    And the error title should be "Invalid Payment Method"

  Scenario: Multiple payments for the same order
    Given a successful payment exists
    When I process another payment for the same order
    Then the payment should succeed
    And both payments should be recorded

  Scenario: Process payment with specific amount
    When I process a payment with amount 149.99 EUR
    Then the payment should succeed
    And the payment amount should be 149.99
