Feature: User Registration
  As a new user
  I want to register for an account
  So that I can access the eco-nomique platform

  # Happy Path Scenarios

  Scenario: Successfully register a new user
    When I register with email "newuser@example.com" and password "SecurePass123!"
    Then the registration should be initiated successfully
    And a verification email should be sent to "newuser@example.com"

  Scenario: Verify email and complete registration
    Given I have registered with email "verify@example.com" and password "SecurePass123!"
    When I verify my email with the correct verification code
    Then my account should be verified

  # Email Validation Scenarios

  Scenario: Cannot register with invalid email format
    When I register with email "not-an-email" and password "SecurePass123!"
    Then the registration should fail with an invalid email error

  # Duplicate Email Scenarios

  Scenario: Cannot register with an email that already exists
    Given I have registered with email "existing@example.com" and password "SecurePass123!"
    And I have verified my email for "existing@example.com"
    When I register with email "existing@example.com" and password "SecurePass123!"
    Then the registration should fail with an email already exists error

  Scenario: Cannot register with an email that has a pending verification
    Given I have registered with email "pending@example.com" and password "SecurePass123!"
    When I register with email "pending@example.com" and password "AnotherPass123!"
    Then the registration should fail with an email already exists error

  # Password Validation Scenarios

  Scenario: Cannot register with a password that is too short
    When I register with email "shortpass@example.com" and password "Short1!"
    Then the registration should fail with an invalid password error
    And the error should mention "at least 8 characters"

  Scenario: Cannot register with a password without uppercase letter
    When I register with email "nouppercase@example.com" and password "lowercase123!"
    Then the registration should fail with an invalid password error
    And the error should mention "uppercase letter"

  Scenario: Cannot register with a password without lowercase letter
    When I register with email "nolowercase@example.com" and password "UPPERCASE123!"
    Then the registration should fail with an invalid password error
    And the error should mention "lowercase letter"

  Scenario: Cannot register with a password without a number
    When I register with email "nonumber@example.com" and password "NoNumberPass!"
    Then the registration should fail with an invalid password error
    And the error should mention "number"

  # Email Verification Scenarios

  Scenario: Cannot verify with incorrect verification code
    Given I have registered with email "wrongcode@example.com" and password "SecurePass123!"
    When I verify my email with an incorrect verification code for "wrongcode@example.com"
    Then the verification should fail with an invalid code error

  Scenario: Resend verification email
    Given I have registered with email "resend@example.com" and password "SecurePass123!"
    When I request to resend the verification email for "resend@example.com"
    Then a new verification email should be sent to "resend@example.com"
